package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Action;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

import static org.meridor.perspective.config.OperationType.RESUME_INSTANCE;

@Component
public class ResumeInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiConsumer<OSClient, Instance> getAction() {
        return (api, instance) -> api.compute().servers().action(instance.getRealId(), Action.RESUME);
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Resumed instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    protected String getErrorMessage() {
        return "Failed to resume instance";
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{RESUME_INSTANCE};
    }
    
}