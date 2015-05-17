package org.meridor.perspective.framework;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.Clouds;
import org.meridor.perspective.config.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class CloudConfigurationProvider {
    
    private static final Logger LOG = LoggerFactory.getLogger(CloudConfigurationProvider.class);

    private static final String CONFIGURATION_FILE_NAME = "clouds.xml";

    @Value("${perspective.mock.enabled}")
    private boolean mockCloudEnabled;
    
    @Value("${perspective.configuration.dir}")
    private Resource configurationDirResource;
    
    private Map<CloudType, Cloud> cloudsMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        try {
            Path configurationDirPath = Paths.get(configurationDirResource.getURI());
            Path configurationFilePath = configurationDirPath.resolve(CONFIGURATION_FILE_NAME);
            if (Files.exists(configurationFilePath)) {
                LOG.info("Loading cloud configuration from {}", configurationFilePath.toString());
                load(configurationFilePath);
            } else {
                LOG.error("Configuration file {} does not exist", configurationFilePath.toString());
            }
            
            if (mockCloudEnabled) {
                LOG.info("Mock cloud is enabled");
                addMockCloud();
            }
        } catch (Exception e) {
            LOG.error("Failed to load clouds configuration", e);
        }
    }
    
    private void load(Path configurationFilePath) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Clouds clouds = (Clouds) unmarshaller.unmarshal(Files.newInputStream(configurationFilePath));
        clouds.getClouds().stream()
                .filter(Cloud::isEnabled)
                .forEach(c -> cloudsMap.put(c.getType(), c));
    }
    
    private void addMockCloud() {
        CloudType cloudType = CloudType.MOCK;
        Cloud cloud = new Cloud();
        cloud.setType(cloudType);
        cloud.setName(cloudType.name());
        cloud.setEnabled(true);
        cloud.setEndpoint("useless");
        cloud.setIdentity("useless");
        cloud.setCredential("useless");
        cloudsMap.put(cloudType, cloud);
    }
    
    public Optional<Cloud> getConfiguration(CloudType cloudType) {
        return Optional.ofNullable(cloudsMap.get(cloudType));
    }
    
    public Set<CloudType> getSupportedClouds() {
        return cloudsMap.keySet();
    }
    
}
