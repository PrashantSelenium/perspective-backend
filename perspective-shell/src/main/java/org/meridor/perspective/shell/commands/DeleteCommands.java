package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.query.DeleteImagesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class DeleteCommands extends BaseCommands {

    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;
    
    @CliCommand(value = "delete instances", help = "Completely delete (terminate) instances")
    public void deleteInstances(
            @CliOption(key = "", mandatory = true, help = "Space separated instances names, ID or patterns to match against instance name") String names,
            @CliOption(key = "projectName", help = "Project name") String projectName,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        
    }
    
    @CliCommand(value = "delete images", help = "Delete images")
    public void set(
            @CliOption(key = "", mandatory = true, help = "Space separated instances names, ID or patterns to match against instance name") String patterns,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        DeleteImagesQuery deleteImagesQuery = new DeleteImagesQuery(patterns, cloud, imagesRepository);
        validateExecuteShowStatus(deleteImagesQuery, imagesRepository::deleteImages);
    }


}
