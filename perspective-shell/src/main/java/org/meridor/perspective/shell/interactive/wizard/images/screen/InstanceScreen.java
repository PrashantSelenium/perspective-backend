package org.meridor.perspective.shell.interactive.wizard.images.screen;

import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.images.step.InstanceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("addImagesInstanceScreen")
public class InstanceScreen implements WizardScreen {
    
    @Autowired
    private InstanceStep instanceStep;
    
    @Autowired
    private NameScreen nameScreen;
    
    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        return instanceStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        return Optional.of(nameScreen);
    }
    
}
