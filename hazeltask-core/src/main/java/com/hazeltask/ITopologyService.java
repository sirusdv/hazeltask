package com.hazeltask;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.hazelcast.core.Member;
import com.hazeltask.executor.task.HazeltaskTask;

/**
 * Methods here that act on multiple members will query for ready members
 * to ensure they have the most up to date data.
 * 
 * TODO: move this to the ClusterService
 * 
 * 
 * @author jclawson
 *
 */
public interface ITopologyService<GROUP extends Serializable> {
    public Set<Member> getReadyMembers();
    public long pingMember(Member member);
    public void shutdown();
    public List<HazeltaskTask<GROUP>> shutdownNow();
}
