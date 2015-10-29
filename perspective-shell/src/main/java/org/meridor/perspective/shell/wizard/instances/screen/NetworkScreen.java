package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.FlavorStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class NetworkScreen implements WizardScreen {
    
    @Autowired
    private FlavorStep flavorStep;
    
    @Autowired
    private KeypairScreen keypairScreen;
    
    @Override
    public Step getStep() {
        return flavorStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(keypairScreen);
    }
}
