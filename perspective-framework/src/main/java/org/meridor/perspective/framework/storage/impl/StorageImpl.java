package org.meridor.perspective.framework.storage.impl;


import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.*;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.meridor.perspective.framework.storage.impl.StorageKey.*;

@Component
public class StorageImpl implements ApplicationListener<ContextClosedEvent>, InstancesAware, ProjectsAware, ImagesAware, Storage {

    private static final Logger LOG = LoggerFactory.getLogger(StorageImpl.class);

    private final HazelcastInstance hazelcastInstance;
    
    @Value("${perspective.storage.lock.timeout:5000}")
    private long lockTimeout;

    private volatile boolean isAvailable = true;

    @Autowired
    public StorageImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public boolean isAvailable() {
        return isAvailable && hazelcastInstance.getLifecycleService().isRunning();
    }

    @Override
    public <T> BlockingQueue<T> getQueue(String name) {
        return isAvailable() ?
                hazelcastInstance.getQueue(name) :
                new LinkedBlockingQueue<>();
    }

    @Override
    public Lock getLock(String name) {
        return hazelcastInstance.getLock(name);
    }

    @Override
    public <T> T executeSynchronized(String lockName, long timeout, Supplier<T> action) {
        LOG.trace("Trying to obtain lock {}", lockName);
        Lock lock = getLock(lockName);
        try {
            if (lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                try {
                    return action.get();
                } finally {
                    LOG.trace("Releasing the lock {}", lockName);
                    lock.unlock();
                }
            } else {
                LOG.trace("Failed to obtain lock {}. Will do nothing.", lockName);
                return null;
            }
        } catch (InterruptedException e) {
            LOG.trace("Lock {} thread interrupted", lock);
            return null;
        }
    }

    private <K, T> IMap<K, T> getMap(String name) {
        return hazelcastInstance.getMap(name);
    }

    @Override
    public <K, T> void modifyMap(String mapId, K key, Consumer<Map<K, T>> action) {
        IMap<K, T> map = getMap(mapId);
        map.lock(key, lockTimeout, TimeUnit.MILLISECONDS);
        try {
            action.accept(map);
        } finally {
            map.unlock(key);
        }
    }
    
    @Override
    public <K, I, O> O readFromMap(String mapId, K key, Function<Map<K, I>, O> function) {
        IMap<K, I> map = getMap(mapId);
        map.lock(key, lockTimeout, TimeUnit.MILLISECONDS);
        try {
            return function.apply(map);
        } finally {
            map.unlock(key);
        }
    }

    private void modifyProject(String projectId, Consumer<Map<String, Project>> action) {
        modifyMap(PROJECTS_BY_ID, projectId, action);
    }

    private <T> T readProject(String projectId, Function<Map<String, Project>, T> function) {
        return readFromMap(PROJECTS_BY_ID, projectId, function);
    }

    @Override
    public void saveProject(Project project) {
        modifyProject(project.getId(), map -> map.put(project.getId(), project));
    }

    @Override
    public void addProjectListener(EntityListener<Project> listener) {
        MapListener entryListener = new EntryListenerImpl<>(listener);
        getMap(PROJECTS_BY_ID).addEntryListener(entryListener, true);
    }

    @Override
    public boolean projectExists(String projectId) {
        return readProject(projectId, map -> getProjectsByIdMap().containsKey(projectId));
    }

    @Override
    public Collection<Project> getProjects() {
        return getProjectsByIdMap().values();
    }

    @Override
    public Collection<Project> getProjects(Set<String> ids) {
        return getProjectsByIdMap().getAll(ids).values();
    }

    @Override
    public Optional<Project> getProject(String projectId) {
        return readProject(projectId, map -> Optional.ofNullable(map.get(projectId)));
    }

    @Override
    public void saveInstance(Instance instance) {
        modifyInstance(instance.getId(), map -> map.put(instance.getId(), instance));
    }

    private void modifyInstance(String instanceId, Consumer<Map<String, Instance>> action) {
        modifyMap(INSTANCES_BY_ID, instanceId, action);
    }

    private <T> T readInstance(String instanceId, Function<Map<String, Instance>, T> function) {
        return readFromMap(INSTANCES_BY_ID, instanceId, function);
    }

    //TODO: add @Transactional annotation and respective aspect for Hazelcast transactions
    @Override
    public void deleteInstance(String instanceId) {
        modifyInstance(instanceId, map -> {
            Instance deletedInstance = map.remove(instanceId);
            getDeletedInstancesByIdMap().put(deletedInstance.getId(), deletedInstance);
        });
    }

    @Override
    public void addInstanceListener(EntityListener<Instance> listener) {
        MapListener entryListener = new EntryListenerImpl<>(listener);
        getInstancesByIdMap().addEntryListener(entryListener, true);
    }

    @Override
    public boolean isInstanceDeleted(String instanceId) {
        return readInstance(instanceId, map -> getDeletedInstancesByIdMap().containsKey(instanceId));
    }

    @Override
    public boolean instanceExists(String instanceId) {
        return readInstance(instanceId, map -> map.containsKey(instanceId));
    }

    @Override
    public Collection<Instance> getInstances() {
        return getInstancesByIdMap().values();
    }

    @Override
    public Collection<Instance> getInstances(Set<String> ids) {
        return getInstancesByIdMap().getAll(ids).values();
    }

    @Override
    public Optional<Instance> getInstance(String instanceId) {
        return readInstance(instanceId, map -> Optional.ofNullable(map.get(instanceId)));
    }

    private void modifyImage(String imageId, Consumer<Map<String, Image>> action) {
        modifyMap(IMAGES_BY_ID, imageId, action);
    }

    private <T> T readImage(String imageId, Function<Map<String, Image>, T> function) {
        return readFromMap(IMAGES_BY_ID, imageId, function);
    }

    @Override
    public boolean imageExists(String imageId) {
        return readImage(imageId, map -> getImagesByIdMap().containsKey(imageId));
    }

    @Override
    public Collection<Image> getImages() {
        return getImagesByIdMap().values();
    }

    @Override
    public Collection<Image> getImages(Set<String> ids) {
        return getImagesByIdMap().getAll(ids).values();
    }

    @Override
    public Optional<Image> getImage(String imageId) {
        return readImage(imageId, map -> Optional.ofNullable(map.get(imageId)));
    }

    @Override
    public void saveImage(Image image) {
        modifyImage(image.getId(), map -> map.put(image.getId(), image));
    }

    @Override
    public boolean isImageDeleted(String imageId) {
        return readImage(imageId, map -> getDeletedImagesByIdMap().containsKey(imageId));
    }

    @Override
    public void deleteImage(String imageId) {
        modifyImage(imageId, map -> {
            Image deletedImage = map.remove(imageId);
            getDeletedImagesByIdMap().put(deletedImage.getId(), deletedImage);
        });
    }

    @Override
    public void addImageListener(EntityListener<Image> listener) {
        MapListener entryListener = new EntryListenerImpl<>(listener);
        getImagesByIdMap().addEntryListener(entryListener, true);
    }

    private IMap<String, Project> getProjectsByIdMap() {
        return getMap(PROJECTS_BY_ID);
    }

    private IMap<String, Instance> getInstancesByIdMap() {
        return getMap(INSTANCES_BY_ID);
    }

    private IMap<String, Image> getImagesByIdMap() {
        return getMap(IMAGES_BY_ID);
    }

    private Map<String, Instance> getDeletedInstancesByIdMap() {
        return getMap(DELETED_INSTANCES);
    }

    private Map<String, Image> getDeletedImagesByIdMap() {
        return getMap(DELETED_IMAGES);
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        LOG.debug("Marking storage as not available because application context is stopping");
        isAvailable = false;
    }

    private static class EntryListenerImpl<T> implements 
            EntryAddedListener<String, T>,
            EntryUpdatedListener<String, T>,
            EntryEvictedListener<String, T>,
            EntryRemovedListener<String, T> {

        private final EntityListener<T> listener;

        EntryListenerImpl(EntityListener<T> listener) {
            this.listener = listener;
        }

        @Override
        public void entryAdded(EntryEvent<String, T> event) {
            T entity = event.getValue();
            LOG.trace("Added entity {} to map {}", entity, event.getName());
            listener.onEvent(entity, null, StorageEvent.ADDED);
        }

        @Override
        public void entryEvicted(EntryEvent<String, T> event) {
            T entity = event.getValue();
            LOG.trace("Evicted entity {} from map {}", entity, event.getName());
            listener.onEvent(entity, null, StorageEvent.EVICTED);
        }

        @Override
        public void entryRemoved(EntryEvent<String, T> event) {
            T oldEntity = event.getOldValue();
            LOG.trace("Deleted entry {} from map {}", oldEntity, event.getName());
            listener.onEvent(null, oldEntity, StorageEvent.DELETED);
        }

        @Override
        public void entryUpdated(EntryEvent<String, T> event) {
            T entity = event.getValue();
            T oldEntity = event.getOldValue();
            LOG.trace("Modified entity {} in map {}", entity, event.getName());
            listener.onEvent(entity, oldEntity, StorageEvent.MODIFIED);
        }
    
    }
}
