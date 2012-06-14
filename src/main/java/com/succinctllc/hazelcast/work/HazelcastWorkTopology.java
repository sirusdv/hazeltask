package com.succinctllc.hazelcast.work;

import java.util.Collection;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.succinctllc.core.concurrent.collections.CopyOnWriteArrayListSet;
import com.succinctllc.hazelcast.work.executor.tasks.IsMemberReadyTimerTask;

/**
 * The topology of the works system describes the different services and
 * properties that are used to connect, and distribute work throughout the
 * cluster. It keeps track of - topology name - the hazelcast instance -
 * communication executor service - local topology index (for naming threads)
 * 
 * TODO: move stuff from DistributedExecutorServiceManager to here.
 * 
 * @author Jason Clawson
 * 
 */
public class HazelcastWorkTopology {
	public static AtomicInteger atomicIndex = new AtomicInteger(1);
	public static ConcurrentMap<String, HazelcastWorkTopology> instances = new ConcurrentHashMap<String, HazelcastWorkTopology>();

	public static long READY_MEMBER_PING_PERIOD = 30000; //every 30 seconds
	
	public static HazelcastWorkTopology getOrCreate(String topologyName, HazelcastInstance hazelcast) {
		HazelcastWorkTopology t = instances.get(topologyName);
		if (t == null) {
			t = new HazelcastWorkTopology(topologyName, hazelcast);
			if (instances.putIfAbsent(topologyName, t) != null) {
				t = instances.get(topologyName);
			} else {
				//start looking for members indicating they are ready to recieve work
				t.startReadyMemberPing();
			}
		}
		return t;
	}
	
	public static HazelcastWorkTopology getDefault(HazelcastInstance hazelcast){
		return getOrCreate("default", hazelcast);
	}
	
	public static HazelcastWorkTopology get(String name) {
		return instances.get(name);
	}

	private final String name;
	private final HazelcastInstance hazelcast;
	private final ExecutorService communicationExecutorService;
	private final int localTopologyId;
	private final ExecutorService workDistributor;
	private final CopyOnWriteArrayListSet<Member> readyMembers;

	private HazelcastWorkTopology(String topologyName, HazelcastInstance hazelcast) {
		this.name = topologyName;
		this.hazelcast = hazelcast;
		this.localTopologyId = atomicIndex.getAndIncrement();
		communicationExecutorService = hazelcast.getExecutorService(createName("com"));
		workDistributor =  hazelcast.getExecutorService(createName("work-distributor"));
		readyMembers = new CopyOnWriteArrayListSet<Member>();
	}
	
	private void startReadyMemberPing() {
		Timer timer = new Timer(createName("ready-member-ping"), true);
        timer.schedule(new IsMemberReadyTimerTask(this), READY_MEMBER_PING_PERIOD, READY_MEMBER_PING_PERIOD);
	}
	
	public String createName(String name) {
        return this.name + "-" + name;
    }

	public String getName() {
		return name;
	}

	public HazelcastInstance getHazelcast() {
		return hazelcast;
	}

	public ExecutorService getCommunicationExecutorService() {
		return communicationExecutorService;
	}

	public int getLocalTopologyId() {
		return localTopologyId;
	}
	
	public ExecutorService getWorkDistributor() {
		return workDistributor;
	}

	public CopyOnWriteArrayListSet<Member> getReadyMembers() {
		return readyMembers;
	}

	public String toString() {
		return "Topology["+localTopologyId+"]: "+name;
	}
	
	/**
	 * Used by the IsMemberReadyTask to set the hazelcast cluster members that are 
	 * ready to do work
	 * 
	 * @param members
	 */
	public void setReadyMembers(Collection<Member> members) {
		readyMembers.addAll(members);
	}
}