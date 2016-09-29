package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

import static org.meridor.perspective.config.OperationType.START_INSTANCE;

@Component
public class StartInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiConsumer<Api, Instance> getAction() {
        return (api, instance) -> api.startInstance(instance.getRealId());
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Started instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    protected String getErrorMessage() {
        return "Failed to start instance";
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{START_INSTANCE};
    }
    
}
