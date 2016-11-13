package org.meridor.perspective.worker.messaging;

import org.meridor.perspective.beans.DestinationName;
import org.meridor.perspective.framework.messaging.Dispatcher;
import org.meridor.perspective.framework.messaging.impl.BaseConsumer;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.framework.messaging.MessageUtils.getRealQueueName;

@Component
public class WriteTasksConsumer extends BaseConsumer {

    @Value("${perspective.messaging.write.consumers:2}")
    private int parallelConsumers;

    private final Dispatcher dispatcher;
    
    private final WorkerMetadata workerMetadata;

    @Autowired
    public WriteTasksConsumer(WorkerMetadata workerMetadata, Dispatcher dispatcher) {
        this.workerMetadata = workerMetadata;
        this.dispatcher = dispatcher;
    }

    @Override
    protected int getParallelConsumers() {
        return parallelConsumers;
    }

    @Override
    protected Dispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    protected String getStorageKey() {
        return getRealQueueName(DestinationName.WRITE_TASKS.value(), workerMetadata.getCloudType());
    }

}
