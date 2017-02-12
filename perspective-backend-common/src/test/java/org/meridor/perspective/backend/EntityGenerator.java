package org.meridor.perspective.backend;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;

import java.time.ZonedDateTime;
import java.util.Collections;

import static org.meridor.perspective.events.EventFactory.now;

public class EntityGenerator {

    public static Cloud getCloud() {
        return new Cloud() {
            {
                setId("test-id");
                setName("test-name");
                setEndpoint("endpoint");
                setIdentity("identity");
                setCredential("credential");
                setEnabled(true);
            }
        };
    }
    
    public static Project getProject() {
        Project project = new Project();
        project.setName("test-project - test-region");
        project.setId("test-project");
        project.setCloudId(CloudType.MOCK.value());
        project.setCloudType(CloudType.MOCK);
        project.getFlavors().add(getFlavor());
        project.getNetworks().add(getNetwork());
        project.getKeypairs().add(getKeypair());
        project.getAvailabilityZones().add(getAvailabilityZone());
        project.setTimestamp(ZonedDateTime.now());
        MetadataMap metadataMap = new MetadataMap();
        metadataMap.put(MetadataKey.REGION, "test-region");
        project.setMetadata(metadataMap);
        project.setQuota(getQuota());
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
        network.getSubnets().add(getSubnet());
        network.setState("ACTIVE");
        return network;
    }

    public static Subnet getSubnet() {
        Subnet subnet = new Subnet();
        subnet.setId("test-subnet");
        subnet.setName("test-subnet");
        subnet.setCidr("5.255.210.0/24");
        return subnet;
    }
    
    public static Keypair getKeypair() {
        Keypair keypair = new Keypair();
        keypair.setName("test-keypair");
        keypair.setFingerprint("key-fingerprint");
        keypair.setPublicKey("test-public-key");
        return keypair;
    }

    public static Quota getQuota() {
        Quota quota = new Quota();
        quota.setInstances("instances-quota");
        quota.setVcpus("vcpus-quota");
        quota.setRam("ram-quota");
        quota.setDisk("disk-quota");
        quota.setIps("ips-quota");
        quota.setSecurityGroups("security-groups-quota");
        quota.setVolumes("volumes-quota");
        quota.setKeypairs("keypairs-quota");
        return quota;
    }
    
    public static Image getImage() {
        Image image = new Image();
        image.setId("test-image");
        image.setRealId("test-image");
        image.setRealId("test-image");
        image.setProjectIds(Collections.singletonList(getProject().getId()));
        image.setName("test-image");
        image.setInstanceId("test-instance");
        image.setCloudType(CloudType.MOCK);
        image.setState(ImageState.SAVED);
        image.setCreated(now().minusDays(2));
        image.setTimestamp(now().minusHours(4));
        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.ARCHITECTURE, "x86");
        image.setMetadata(metadata);
        return image;
    }

    public static Instance getInstance() {
        Instance instance = new Instance();
        instance.setId("test-instance");
        instance.setRealId("test-instance");
        instance.setTimestamp(now());
        instance.setCreated(now());
        instance.setCloudId(CloudType.MOCK.value());
        instance.setCloudType(CloudType.MOCK);
        instance.setProjectId("test-project");
        instance.setName("test-instance");
        instance.setFlavor(getFlavor());
        instance.getNetworks().add(getNetwork());
        instance.getAddresses().add("5.255.210.3");
        instance.setFqdn("test-instance.example.com");
        instance.setImage(getImage());
        instance.setState(InstanceState.LAUNCHED);
        instance.setAvailabilityZone(getAvailabilityZone());
        MetadataMap metadataMap = new MetadataMap();
        metadataMap.put(MetadataKey.REGION, "test-region");
        instance.setMetadata(metadataMap);
        return instance;
    }

    public static Instance getErrorInstance() {
        Instance instance = getInstance();
        instance.setCreated(now().minusDays(5));
        instance.setTimestamp(now().minusDays(3));
        instance.setId("error-instance");
        instance.setName("error-instance");
        instance.setState(InstanceState.ERROR);
        return instance;
    }

    public static Letter getLetter() {
        Letter letter = new Letter();
        letter.setId("test-letter");
        letter.setText("test-message");
        letter.setTimestamp(ZonedDateTime.now());
        return letter;
    }
    
}
