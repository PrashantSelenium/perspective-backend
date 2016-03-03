package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

import static org.meridor.perspective.sql.impl.table.TableName.INSTANCES;

@Component
public class InstancesTable implements Table {
    
    public String id;
    public String real_id;
    public String name;
    public String cloud_id;
    public String cloud_type;
    public String project_id;
    public String flavor_id;
    public String image_id;
    public String state;
    public String last_updated;
    public String created;
    public String availability_zone;
    public String addresses; //Networks are provided via instance_networks table
    
    @Override
    public TableName getName() {
        return INSTANCES;
    }
    
}
