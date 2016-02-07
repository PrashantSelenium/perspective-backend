package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.query.AddInstancesQuery;
import org.meridor.perspective.shell.query.ModifyInstancesQuery;
import org.meridor.perspective.shell.query.ShowInstancesQuery;
import org.meridor.perspective.shell.repository.ApiProvider;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InstancesRepositoryImpl implements InstancesRepository {

    @Autowired
    private ApiProvider apiProvider;

    @Override public List<Instance> showInstances(ShowInstancesQuery showInstancesQuery) {
        GenericType<ArrayList<Instance>> instanceListType = new GenericType<ArrayList<Instance>>() {};
        List<Instance> instances = apiProvider.getInstancesApi().getAsXml(instanceListType);
        return instances.stream()
                .filter(showInstancesQuery.getPayload())
                .sorted((i1, i2) -> Comparator.<String>naturalOrder().compare(i1.getName(), i2.getName()))
                .collect(Collectors.toList());
    }

    @Override public Set<String> addInstances(AddInstancesQuery addInstancesQuery) {
        List<Instance> instances = addInstancesQuery.getPayload();
        GenericEntity<List<Instance>> data = new GenericEntity<List<Instance>>(instances) {
        };
        apiProvider.getInstancesApi().postXmlAs(data, String.class);
        return Collections.emptySet();
    }
    
    @Override public Set<String> deleteInstances(ModifyInstancesQuery modifyInstancesQuery) {
        GenericEntity<List<Instance>> data = new GenericEntity<List<Instance>>(modifyInstancesQuery.getPayload()) {
        };
        apiProvider.getInstancesApi().delete().postXmlAs(data, String.class);
        return Collections.emptySet();
    }
    
    @Override public Set<String> rebootInstances(ModifyInstancesQuery modifyInstancesQuery) {
        GenericEntity<List<Instance>> data = new GenericEntity<List<Instance>>(modifyInstancesQuery.getPayload()) {
        };
        apiProvider.getInstancesApi().reboot().putXmlAs(data, String.class);
        return Collections.emptySet();
    }
    
    @Override public Set<String> hardRebootInstances(ModifyInstancesQuery modifyInstancesQuery) {
        GenericEntity<List<Instance>> data = new GenericEntity<List<Instance>>(modifyInstancesQuery.getPayload()) {
        };
        apiProvider.getInstancesApi().hardReboot().putXmlAs(data, String.class);
        return Collections.emptySet();
    }
    
}
