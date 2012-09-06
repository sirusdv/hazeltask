package com.hazeltask.batch;

import java.util.Collection;

import com.hazeltask.executor.ExecutorListener;
import com.succinctllc.hazelcast.work.HazelcastWork;

public class PreventDuplicatesListener<I> implements BatchExecutorListener<I>, ExecutorListener {
    private final IBatchClusterService<I> svc;
    private final BatchKeyAdapter<I> batchKeyAdapter;
    
    public PreventDuplicatesListener(IBatchClusterService<I> topologyService, BatchKeyAdapter<I> batchKeyAdapter ) {
        this.svc = topologyService;
        this.batchKeyAdapter = batchKeyAdapter;
    }

    public boolean beforeExecute(HazelcastWork task) {return true;}

    public void afterExecute(HazelcastWork task, Throwable exception) {
        Runnable runnable = task.getInnerRunnable();
        if(runnable instanceof WorkBundle) {
            @SuppressWarnings("unchecked")
            Collection<I> items = ((WorkBundle<I>) runnable).getItems();
            //TODO: is it more efficient with hazelcast to put this in a transaction?
            //      ie: will it batch these updates up somehow?
           for(I item : items) { 
               svc.removePreventDuplicateItem(batchKeyAdapter.getItemId(item));
           }
        } else {
            //TODO: log this error condition... just use slf4j
        }
    }

    public boolean beforeAdd(Object item) {
        return svc.isInPreventDuplicateSet(batchKeyAdapter.getItemId((I)item));
    }

    public void afterAdd(Object item, boolean added) {
        if(added) {
            svc.addToPreventDuplicateSet(batchKeyAdapter.getItemId((I)item));
        }
    }
    
}