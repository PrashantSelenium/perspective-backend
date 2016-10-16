package org.meridor.perspective.shell.interactive.wizard.common.step;

import org.meridor.perspective.shell.common.repository.InstancesRepository;
import org.meridor.perspective.shell.common.request.FindInstancesRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindInstancesResult;
import org.meridor.perspective.shell.interactive.wizard.MultipleChoicesStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public abstract class AbstractMultipleInstancesStep extends MultipleChoicesStep {

    @Autowired
    private InstancesRepository instancesRepository;

    @Autowired
    private RequestProvider requestProvider;

    private String projectName;

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    protected List<String> getPossibleChoices() {
        FindInstancesRequest findInstancesRequest = requestProvider.get(FindInstancesRequest.class)
                .withProjectNames(projectName);
        return instancesRepository.findInstances(findInstancesRequest).stream()
                .map(FindInstancesResult::getName)
                .collect(Collectors.toList());
    }

}
