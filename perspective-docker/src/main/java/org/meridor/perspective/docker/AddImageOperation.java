package org.meridor.perspective.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.ProcessingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.ADD_IMAGE;

@Component
public class AddImageOperation implements ProcessingOperation<Image, Image> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AddImageOperation.class);
    
    @Autowired
    private DockerApiProvider apiProvider;
    
    @Override
    public Image perform(Cloud cloud, Supplier<Image> supplier) {
        Image image = supplier.get();
        try {
            DockerClient dockerApi = apiProvider.getApi(cloud);
            String instanceId = image.getMetadata().get(MetadataKey.INSTANCE_ID);
            ContainerConfig containerConfig = ContainerConfig.builder().build();
            ContainerCreation createdImage = dockerApi.commitContainer(
                    instanceId,
                    "perspective",
                    null,
                    containerConfig,
                    null,
                    null
            );
            String imageId = createdImage.id();
            image.getMetadata().put(MetadataKey.ID, imageId);
            LOG.debug("Added image {} ({})", image.getName(), image.getId());
            return image;
        } catch (Exception e) {
            LOG.error("Failed to add image " + image.getName(), e);
            return null;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_IMAGE};
    }
}
