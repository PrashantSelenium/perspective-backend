package org.meridor.perspective.worker.fetcher.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.beans.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/mocked-storage-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class InstanceModificationListenerTest {

    @Autowired
    private InstancesAware instancesAware;
    
    @Autowired
    private InstanceModificationListener instanceModificationListener;

    private static final String CLOUD_ID = EntityGenerator.getCloud().getId();
    
    @Test
    public void testListen() {
        final String ID = "new-id";
        Instance instance = EntityGenerator.getInstance();
        instance.setId(ID);
        instance.setCloudId(CLOUD_ID);
        instance.setTimestamp(ZonedDateTime.now().minus(1, ChronoUnit.DAYS));
        instancesAware.saveInstance(instance);
        Set<String> ids = instanceModificationListener.getIds(CLOUD_ID, LastModified.LONG_AGO);
        assertThat(ids, contains(ID));
    }
    
}