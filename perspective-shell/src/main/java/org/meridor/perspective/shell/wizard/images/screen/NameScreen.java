package org.meridor.perspective.shell.wizard.images.screen;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.images.step.NameStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component("imageNameScreen")
public class NameScreen implements WizardScreen {
    
    @Autowired
    private NameStep nameStep;

    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        return nameStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.empty();
    }
    
}
