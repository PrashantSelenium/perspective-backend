package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.KeypairStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class KeypairScreen implements WizardScreen {
    
    @Autowired
    private KeypairStep keypairStep;
    
    @Override
    public Step getStep() {
        return keypairStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.empty();
    }
}
