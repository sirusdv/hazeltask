package com.hazeltask.clustertasks;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.hazeltask.executor.LocalTaskExecutorService;

public class GetLocalQueueSizesTask extends AbstractCallable<Long> {
    private static final long serialVersionUID = 1L;

    public GetLocalQueueSizesTask(String topology) {
        super(topology);
    }

    //fixme: svc is null for local operations! wtf!  grrrrrr!  lame!
    public Long call() throws Exception {
        LocalTaskExecutorService localSvc = getDistributedExecutorService().getLocalTaskExecutorService();
        
        return localSvc.getQueueSize();
    }

    @Override
    protected void readChildData(DataInput in) throws IOException {   
    }

    @Override
    protected void writChildData(DataOutput out) throws IOException {    
    }       
}