package org.meridor.perspective.rest.storage;


import com.hazelcast.core.HazelcastInstance;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Storage {

    private static final String INSTANCES = "instances";
    private static final String LIST = "list";
    
    @Autowired
    private HazelcastInstance hazelcastClient;
    
    public void saveProjects(CloudType cloudType, List<Project> projects) {
        Map<String, Object> projectsMap = new HashMap<>();
        projectsMap.put(LIST, projects);
        projects.stream().forEach(p -> projectsMap.put(p.getId(), p));
        hazelcastClient.getMap(getProjectsKey(cloudType)).putAll(projectsMap);
    }
    
    @SuppressWarnings("unchecked")
    public List<Project> getProjects(CloudType cloudType) {
        List<Project> projects = (List<Project>) hazelcastClient.getMap(getProjectsKey(cloudType)).get(LIST);
        return (projects != null) ? projects : Collections.emptyList();
    }

    public void saveInstances(CloudType cloudType, List<Instance> instances) {
        Map<String, Object> instancesMap = new HashMap<>();
        instancesMap.put(LIST, instances);
        Map<String, List<Instance>> instancesByProjectAndRegion = new HashMap<>();
        instances.stream().forEach(i -> {
            String regionKey = getRegionKey(i.getProjectId(), i.getRegionId());
            String instanceKey = getInstanceKey(i.getProjectId(), i.getRegionId(), i.getId());
            instancesMap.put(instanceKey, i);
            if (instancesByProjectAndRegion.containsKey(regionKey)) {
                instancesByProjectAndRegion.get(regionKey).add(i);
            } else {
                instancesByProjectAndRegion.put(regionKey, new ArrayList<Instance>() {
                    {
                        add(i);
                    }
                });
            }
        });
        instancesMap.putAll(instancesByProjectAndRegion);
        hazelcastClient.getMap(getCloudInstancesKey(cloudType)).putAll(instancesMap);
    }
    
    public void saveInstance(CloudType cloudType, Instance instance) {
        saveInstances(cloudType, new ArrayList<Instance>() {
            {
                add(instance);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public void deleteInstances(CloudType cloudType, List<Instance> instances) {
        Map<String, Object> instancesMap = hazelcastClient.getMap(getCloudInstancesKey(cloudType));
        instances.stream().forEach(
                i -> {
                    String instanceKey = getInstanceKey(i.getProjectId(), i.getRegionId(), i.getId());
                    String regionKey = getRegionKey(i.getProjectId(), i.getRegionId());
                    instancesMap.remove(instanceKey);
                    instancesMap.remove(regionKey);
                    ((List<Instance>) instancesMap.get(LIST)).remove(i);
                }
        );
        hazelcastClient.getMap(getCloudInstancesKey(cloudType)).putAll(instancesMap);
    }
    
    @SuppressWarnings("unchecked")
    public List<Instance> getInstances(CloudType cloudType, String projectId, String regionId) {
        String cloudInstancesKey = getCloudInstancesKey(cloudType);
        String regionKey = getRegionKey(projectId, regionId);
        List<Instance> instances = (List<Instance>) hazelcastClient.getMap(cloudInstancesKey).get(regionKey);
        return (instances != null) ? instances : Collections.emptyList();
    }
    
    @SuppressWarnings("unchecked")
    public Optional<Instance> getInstance(CloudType cloudType, String projectId, String regionId, String instanceId) {
        String cloudInstancesKey = getCloudInstancesKey(cloudType);
        String instanceKey = getInstanceKey(projectId, regionId, instanceId);
        return Optional.ofNullable((Instance) hazelcastClient.getMap(cloudInstancesKey).get(instanceKey));
    }
    
    private static String getProjectsKey(CloudType cloudType) {
        return "projects_" + cloudType;
    }
    
    private static String getRegionKey(String projectId, String regionId) {
        return "project_" + projectId + "_region_" + regionId; 
    }
    
    private static String getCloudInstancesKey(CloudType cloudType){
        return "cloud_" + cloudType.value() + "_instances";
    }
    
    private static String getInstanceKey(String projectId, String regionId, String instanceId) {
        return getRegionKey(projectId, regionId) + "_instance_" + instanceId; 
    }
}
