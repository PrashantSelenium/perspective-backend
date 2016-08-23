package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.InstanceNetwork;
import org.meridor.perspective.rest.data.converters.InstanceConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

@Component
public class InstanceNetworksTableFetcher extends InstancesBasedTableFetcher<InstanceNetwork> {

    @Override
    protected Class<InstanceNetwork> getBeanClass() {
        return InstanceNetwork.class;
    }

    @Override
    public String getTableName() {
        return TableName.INSTANCE_NETWORKS.getTableName();
    }

    @Override
    protected Predicate<Instance> getPredicate(String id) {
        String[] pieces = parseCompositeId(id, 2);
        String instanceId = pieces[0];
        return i -> instanceId.equals(i.getId());
    }

    @Override
    protected Function<Instance, Stream<InstanceNetwork>> getConverter() {
        return InstanceConverters::instanceToNetworks;
    }
}
