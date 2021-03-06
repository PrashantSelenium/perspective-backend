package org.meridor.perspective.docker;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.ProcessingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.ADD_INSTANCE;

@Component
public class AddInstanceOperation implements ProcessingOperation<Instance, Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(AddInstanceOperation.class);

    private final ApiProvider apiProvider;

    private final IdGenerator idGenerator;

    @Autowired
    public AddInstanceOperation(ApiProvider apiProvider, IdGenerator idGenerator) {
        this.apiProvider = apiProvider;
        this.idGenerator = idGenerator;
    }

    @Override
    public Instance perform(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        try {
            Api api = apiProvider.getApi(cloud);
            String command = instance.getMetadata().get(MetadataKey.COMMAND);
            String imageId = instance.getImage().getRealId();
            String realId = api.addContainer(imageId, instance.getName(), command);
            instance.setRealId(realId);
            String instanceId = idGenerator.getInstanceId(cloud, realId);
            instance.setId(instanceId);
            LOG.debug("Added instance {} ({})", instance.getName(), instance.getId());
            return instance;
        } catch (Exception e) {
            LOG.error("Failed to add instance " + instance.getName(), e);
            return null;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_INSTANCE};
    }
}
