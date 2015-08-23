package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceState;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.*;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.annotations.*;

import static org.meridor.perspective.events.EventFactory.instanceEvent;

@Component
@FSM(start = InstanceNotAvailableEvent.class)
@Transitions({
        //Instance sync
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceQueuedEvent.class, to = InstanceQueuedEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceLaunchingEvent.class, to = InstanceLaunchingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceRebootingEvent.class, to = InstanceRebootingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceHardRebootingEvent.class, to = InstanceHardRebootingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceShuttingDownEvent.class, to = InstanceShuttingDownEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceShutOffEvent.class, to = InstanceShutOffEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstancePausingEvent.class, to = InstancePausingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstancePausedEvent.class, to = InstancePausedEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceResumingEvent.class, to = InstanceResumingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceSnapshottingEvent.class, to = InstanceSnapshottingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceRebuildingEvent.class, to = InstanceRebuildingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceResizingEvent.class, to = InstanceResizingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceMigratingEvent.class, to = InstanceMigratingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceDeletingEvent.class, to = InstanceDeletingEvent.class),

        //Instance launch
        @Transit(from = InstanceQueuedEvent.class, on = InstanceLaunchingEvent.class, to = InstanceLaunchingEvent.class),
        @Transit(from = InstanceLaunchingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        @Transit(from = InstanceLaunchingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceDeletingEvent.class, stop = true),

        //Instance soft reboot
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceRebootingEvent.class, to = InstanceRebootingEvent.class),
        @Transit(from = InstanceRebootingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceRebootingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),

        //Instance hard reboot
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceHardRebootingEvent.class, to = InstanceHardRebootingEvent.class),
        @Transit(from = InstanceHardRebootingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceHardRebootingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),

        //Instance shut off
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceShuttingDownEvent.class, to = InstanceShuttingDownEvent.class),
        @Transit(from = InstanceShuttingDownEvent.class, on = InstanceShutOffEvent.class, to = InstanceShutOffEvent.class),
        @Transit(from = InstanceShuttingDownEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceShutOffEvent.class, on = InstanceLaunchingEvent.class, to = InstanceLaunchingEvent.class),
        @Transit(from = InstanceShutOffEvent.class, on = InstanceDeletingEvent.class, stop = true),

        //Instance suspend
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceSuspendingEvent.class, to = InstanceSuspendingEvent.class),
        @Transit(from = InstanceSuspendingEvent.class, on = InstanceSuspendedEvent.class, to = InstanceSuspendedEvent.class),
        @Transit(from = InstanceSuspendingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceSuspendedEvent.class, on = InstanceLaunchingEvent.class, to = InstanceLaunchingEvent.class),
        @Transit(from = InstanceSuspendedEvent.class, on = InstanceDeletingEvent.class, stop = true),

        //Instance pause
        @Transit(from = InstanceLaunchedEvent.class, on = InstancePausingEvent.class, to = InstancePausingEvent.class),
        @Transit(from = InstancePausingEvent.class, on = InstancePausedEvent.class, to = InstancePausedEvent.class),
        @Transit(from = InstancePausingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstancePausedEvent.class, on = InstanceResumingEvent.class, to = InstanceResumingEvent.class),
        @Transit(from = InstanceResumingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        @Transit(from = InstancePausedEvent.class, on = InstanceDeletingEvent.class, stop = true),

        //Instance snapshot
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceSnapshottingEvent.class, to = InstanceSnapshottingEvent.class),
        @Transit(from = InstanceSnapshottingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceSnapshottingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),

        //Instance rebuild
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceRebuildingEvent.class, to = InstanceRebuildingEvent.class),
        @Transit(from = InstanceRebuildingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceRebuildingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),

        //Instance resize
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceResizingEvent.class, to = InstanceResizingEvent.class),
        @Transit(from = InstanceResizingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceResizingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),

        //Instance migrate
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceMigratingEvent.class, to = InstanceMigratingEvent.class),
        @Transit(from = InstanceMigratingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceMigratingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),

        //Instance removal
        @Transit(from = InstanceErrorEvent.class, on = InstanceDeletingEvent.class, stop = true)
})
public class InstanceFSM {

    private static final Logger LOG = LoggerFactory.getLogger(InstanceFSM.class);

    @Autowired
    private OperationProcessor operationProcessor;

    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;

    @Autowired
    private InstancesAware storage;

    @BeforeTransit
    public void beforeTransit(InstanceEvent instanceEvent) {
        LOG.trace("Doing transition for event {}", instanceEvent);
    }

    @OnTransit
    public void onInstanceQueued(InstanceQueuedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.debug("Marking cloud {} instance {} as queued", instance.getCloudType(), instance.getId());
            instance.setState(InstanceState.QUEUED);
            storage.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceLaunching(InstanceLaunchingEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Launching cloud {} instance {}", cloudId, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.LAUNCH_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.LAUNCHING);
            storage.saveInstance(instance);
        } else {
            throw new InstanceException("Failed to launch", instance);
        }
    }

    @OnTransit
    public void onInstanceLaunched(InstanceLaunchedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.debug("Marking cloud {} instance {} as launched", instance.getCloudType(), instance.getId());
            instance.setState(InstanceState.LAUNCHED);
            storage.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceRebooting(InstanceRebootingEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Rebooting cloud {} instance {}", cloudId, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.REBOOT_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.REBOOTING);
        } else {
            instance.setErrorReason("Failed to reboot");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceHardRebooting(InstanceHardRebootingEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Hard rebooting cloud {} instance {}", cloudId, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.HARD_REBOOT_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.HARD_REBOOTING);
        } else {
            instance.setErrorReason("Failed to hard reboot");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceShuttingDown(InstanceShuttingDownEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Shutting down cloud {} instance {}", cloud.getName(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.SHUTDOWN_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.SHUTTING_DOWN);
        } else {
            instance.setErrorReason("Failed to shut down");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceShutoff(InstanceShutOffEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.debug("Marking cloud {} instance {} as shutoff", instance.getCloudId(), instance.getId());
            instance.setState(InstanceState.SHUTOFF);
            storage.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstancePausing(InstancePausingEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Pausing cloud {} instance {}", cloudId, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.PAUSE_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.PAUSING);
        } else {
            instance.setErrorReason("Failed to pause");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstancePaused(InstancePausedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.debug("Marking cloud {} instance {} as paused", instance.getCloudId(), instance.getId());
            instance.setState(InstanceState.PAUSED);
            storage.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceResuming(InstanceResumingEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Resuming cloud {} instance {}", cloudId, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.RESUME_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.RESUMING);
        } else {
            instance.setErrorReason("Failed to resume");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceRebuilding(InstanceRebuildingEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Rebuilding cloud {} instance {}", cloudId, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.REBUILD_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.REBUILDING);
        } else {
            instance.setErrorReason("Failed to rebuild");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceResizing(InstanceResizingEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Resizing cloud {} instance {}", cloudId, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.RESIZE_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.RESIZING);
        } else {
            instance.setErrorReason("Failed to resize");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceSuspending(InstanceSuspendingEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Suspending cloud {} instance {}", cloudId, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.SUSPEND_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.SUSPENDING);
        } else {
            instance.setErrorReason("Failed to suspend");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceSuspended(InstanceSuspendedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.debug("Marking cloud {} instance {} as suspended", instance.getCloudType(), instance.getId());
            instance.setState(InstanceState.SUSPENDED);
            storage.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceMigrating(InstanceMigratingEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Migrating cloud {} instance {}", cloudId, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.MIGRATE_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.MIGRATING);
        } else {
            instance.setErrorReason("Failed to migrate");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceDeleting(InstanceDeletingEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        if (storage.instanceExists(instance)) {
            LOG.info("Deleting cloud {} instance {}", cloudId, instance.getId());
            if (event.isSync() || operationProcessor.supply(cloud, OperationType.DELETE_INSTANCE, () -> instance)) {
                storage.deleteInstance(instance);
            } else {
                throw new InstanceException("Failed to delete", instance);
            }
        } else {
            LOG.error("Can't delete instance {} - not exists", instance.getId());
        }
    }

    @OnTransit
    public void onInstanceSnapshotting(InstanceSnapshottingEvent event) throws Exception {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Taking cloud {} instance {} snapshot", cloudId, instance.getId());
        if (operationProcessor.supply(cloud, OperationType.SNAPSHOT_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.SNAPSHOTTING);
        } else {
            instance.setErrorReason("Failed to take snapshot");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceError(InstanceErrorEvent event) {
        Instance instance = event.getInstance();
        LOG.info("Changing cloud {} instance {} status to error", instance.getCloudId(), instance.getId());
        instance.setState(InstanceState.ERROR);
        instance.setErrorReason(event.getErrorReason());
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onUnknownEvent(InstanceEvent event) {
        LOG.warn("Skipping unknown event {}", event);
    }

    @OnException
    public void onInstanceException(InstanceException e) throws Exception {
        InstanceErrorEvent event = instanceEvent(InstanceErrorEvent.class, e.getInstance());
        event.setErrorReason(e.getMessage());
        onInstanceError(event);
    }

    @OnException
    public void onUnsupportedOperationException(UnsupportedOperationException e) {
        LOG.error("Trying to do an unsupported operation", e);
    }

    @OnException
    public void onException(Exception e) {
        LOG.error("An uncaught exception discovered", e);
    }

}
