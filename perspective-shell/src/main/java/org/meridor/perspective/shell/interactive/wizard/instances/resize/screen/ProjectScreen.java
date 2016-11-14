package org.meridor.perspective.shell.interactive.wizard.instances.resize.screen;

import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.resize.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("resizeInstancesProjectScreen")
public class ProjectScreen implements WizardScreen {
    
    private final ProjectStep projectStep;
    
    private final InstanceScreen instanceScreen;

    @Autowired
    public ProjectScreen(ProjectStep projectStep, InstanceScreen instanceScreen) {
        this.projectStep = projectStep;
        this.instanceScreen = instanceScreen;
    }

    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        return projectStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        return Optional.of(instanceScreen);
    }
}
