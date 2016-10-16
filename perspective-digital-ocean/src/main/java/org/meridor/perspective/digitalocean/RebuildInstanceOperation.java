package org.meridor.perspective.digitalocean;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

import static org.meridor.perspective.config.OperationType.REBUILD_INSTANCE;

@Component
public class RebuildInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiFunction<Api, Instance, Boolean> getAction() {
        return (api, instance) -> {
            try {
                api.rebuildDroplet(Integer.valueOf(instance.getRealId()), Integer.valueOf(instance.getImage().getId()));
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Started instance %s (%s) rebuild to image %s", instance.getName(), instance.getId(), instance.getImage().getName());
    }

    @Override
    protected String getErrorMessage() {
        return "Failed to rebuild instance";
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{REBUILD_INSTANCE};
    }

}
