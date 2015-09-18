package org.meridor.perspective.mock;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.CloudType;

import static org.meridor.perspective.events.EventFactory.now;

public class EntityGenerator {

    public static Project getProject() {
        Project project = new Project();
        project.setName("test-project - test-region");
        project.setId("test-project");
        project.setCloudType(CloudType.MOCK);
        project.getFlavors().add(getFlavor());
        project.getNetworks().add(getNetwork());
        project.getAvailabilityZones().add(getAvailabilityZone());
        return project;
    }

    public static Flavor getFlavor() {
        Flavor flavor = new Flavor();
        flavor.setId("test-flavor");
        flavor.setName("test-flavor");
        flavor.setVcpus(2);
        flavor.setRam(2048);
        return flavor;
    }

    public static AvailabilityZone getAvailabilityZone() {
        AvailabilityZone availabilityZone = new AvailabilityZone();
        availabilityZone.setName("test-zone");
        return availabilityZone;
    }

    public static Network getNetwork() {
        Network network = new Network();
        network.setId("test-network");
        network.setName("test-network");
        network.getSubnets().add("5.255.210.0/24");
        return network;
    }

    public static Image getImage() {
        Image image = new Image();
        image.setId("test-image");
        image.setName("test-image");
        image.setState(ImageState.SAVED);
        image.setIsProtected(true);
        return image;
    }

    public static Instance getInstance() {
        Instance instance = new Instance();
        instance.setId("test-instance");
        instance.setTimestamp(now());
        instance.setCloudType(CloudType.MOCK);
        instance.setProjectId("test-project");
        instance.setName("test-instance");
        instance.setFlavor(getFlavor());
        instance.getNetworks().add(getNetwork());
        instance.setImage(getImage());
        instance.setState(InstanceState.LAUNCHED);
        instance.setAvailabilityZone(getAvailabilityZone());
        return instance;
    }

    public static Instance getErrorInstance() {
        Instance instance = getInstance();
        instance.setTimestamp(now().minusDays(3));
        instance.setId("error-instance");
        instance.setName("error-instance");
        instance.setState(InstanceState.ERROR);
        return instance;
    }

}
