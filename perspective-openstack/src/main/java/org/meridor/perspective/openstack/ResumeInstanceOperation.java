package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

import static org.meridor.perspective.config.OperationType.RESUME_INSTANCE;

@Component
public class ResumeInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiConsumer<Api, Instance> getAction() {
        return (api, instance) -> {
            switch (instance.getState()) {
                case PAUSED: api.unpauseInstance(instance.getRealId());
                default:
                case SUSPENDED: api.resumeInstance(instance.getRealId()); 
            }
        };
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Resumed instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    protected String getErrorMessage(Instance instance) {
        return String.format("Failed to resume instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{RESUME_INSTANCE};
    }
    
}
