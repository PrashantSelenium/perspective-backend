package org.meridor.perspective.rest.storage;


import com.hazelcast.core.HazelcastInstance;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;

import static org.meridor.perspective.rest.storage.StorageKey.*;

@Component
public class Storage implements ApplicationListener<ContextClosedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(Storage.class);
    
    @Autowired
    private HazelcastInstance hazelcastInstance;
    
    private volatile boolean isAvailable = true;
    
    public boolean isAvailable() {
        return isAvailable && hazelcastInstance.getLifecycleService().isRunning();
    }
    
    public BlockingQueue<Object> getQueue(String name) {
        return isAvailable() ? 
                hazelcastInstance.getQueue(name) :
                new LinkedBlockingQueue<>();
    }
    
    public Lock getLock(String name) {
        return hazelcastInstance.getLock(name);
    }
    
    public <T> Map<String, T> getMap(String name) {
        return isAvailable() ? 
                hazelcastInstance.getMap(name) : Collections.emptyMap();
    }
    
    public void saveProject(Project project) {
        CloudType cloudType = project.getCloudType();
        getProjectsByIdMap(cloudType).put(project.getId(), project);
    }
    
    public Collection<Project> getProjects(CloudType cloudType) {
        return getProjectsByIdMap(cloudType).values();
    }

    public Optional<Project> getProject(CloudType cloudType, String projectId) {
        return Optional.ofNullable(getProjectsByIdMap(cloudType).get(projectId));
    }

    public void saveInstance(Instance instance) {
        CloudType cloudType = instance.getCloudType();
        getInstancesByIdMap(cloudType).put(instance.getId(), instance);
        getInstancesByProjectAndRegionMap(cloudType, instance.getProjectId(), instance.getRegionId()).put(instance.getId(), instance);
    }
    
    public void deleteInstance(Instance instance) {
        CloudType cloudType = instance.getCloudType();
        getInstancesByIdMap(cloudType).remove(instance.getId());
        getInstancesByProjectAndRegionMap(cloudType, instance.getProjectId(), instance.getRegionId()).remove(instance.getId());
    }
    
    public boolean instanceExists(Instance instance) {
        String instanceId = instance.getId();
        CloudType cloudType = instance.getCloudType();
        return getInstancesByIdMap(cloudType).containsKey(instanceId);
    }
    
    public Collection<Instance> getInstances(CloudType cloudType, String projectId, String regionId) {
        return getInstancesByProjectAndRegionMap(cloudType, projectId, regionId).values();
    }
    
    public Optional<Instance> getInstance(CloudType cloudType, String instanceId) {
        return Optional.ofNullable(getInstancesByIdMap(cloudType).get(instanceId));
    }
    
    private Map<String, Project> getProjectsByIdMap(CloudType cloudType) {
        return getMap(projectsByCloud(cloudType));
    }
    
    private Map<String, Instance> getInstancesByProjectAndRegionMap(CloudType cloudType, String projectId, String regionId) {
        return getMap(instancesSetByProjectAndRegion(cloudType, projectId, regionId));
    }

    private Map<String, Instance> getInstancesByIdMap(CloudType cloudType) {
        return getMap(instancesByCloud(cloudType));
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        LOG.debug("Marking storage as not available because application context is stopping");
        isAvailable = false;
    }
}
