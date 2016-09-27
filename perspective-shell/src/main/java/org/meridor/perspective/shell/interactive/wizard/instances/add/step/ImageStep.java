package org.meridor.perspective.shell.interactive.wizard.instances.add.step;

import org.meridor.perspective.shell.interactive.wizard.common.step.AbstractImageStep;
import org.springframework.stereotype.Component;

@Component("addInstancesImageStep")
public class ImageStep extends AbstractImageStep {
    
    @Override
    public String getMessage() {
        return "Select image to launch instances from:";
    }
    
}
