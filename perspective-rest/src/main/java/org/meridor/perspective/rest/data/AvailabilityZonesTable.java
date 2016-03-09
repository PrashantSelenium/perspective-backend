package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.sql.impl.table.TableName.AVAILABILITY_ZONES;

@Component
public class AvailabilityZonesTable implements Table {
    
    public String name;
    public String project_id;
    
    @Override
    public TableName getName() {
        return AVAILABILITY_ZONES;
    }
    
}
