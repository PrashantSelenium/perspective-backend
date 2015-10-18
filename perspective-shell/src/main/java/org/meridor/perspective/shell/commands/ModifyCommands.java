package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.query.ModifyInstancesQuery;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ModifyCommands extends BaseCommands {

    @Autowired
    private InstancesRepository instancesRepository;

    @CliCommand(value = "reboot", help = "Reboot instances")
    public void rebootInstances(
            @CliOption(key = "", mandatory = true, help = "Space separated instances names, ID or patterns to match against instance name") String names,
            @CliOption(key = "cloud", help = "Cloud types") String cloud,
            @CliOption(key = "hard", help = "Whether to hard reboot instance") boolean hard
    ) {
        ModifyInstancesQuery modifyInstancesQuery = new ModifyInstancesQuery(names, cloud, instancesRepository);
        validateConfirmExecuteShowStatus(
                modifyInstancesQuery,
                instances -> hard ?
                        String.format("Going to hard reboot %d instances.", instances.size()):
                        String.format("Going to reboot %d instances.", instances.size()),
                instances -> new String[]{"Name", "Image", "Flavor", "State", "Last modified"},
                instances -> instances.stream().map(TextUtils::instanceToRow).collect(Collectors.toList()),
                hard ? instancesRepository::hardRebootInstances : instancesRepository::rebootInstances
        );
    }


}
