package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;

import java.util.List;
import java.util.function.Consumer;

import static org.meridor.perspective.config.CloudType.OPENSTACK;
import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;

@Operation(cloud = OPENSTACK, type = LIST_PROJECTS)
public class ListProjectsOperation {
    
    @EntryPoint
    public void listProjects(Consumer<List<Project>> consumer) {
        //TODO: to be implemented!
    }
    
}
