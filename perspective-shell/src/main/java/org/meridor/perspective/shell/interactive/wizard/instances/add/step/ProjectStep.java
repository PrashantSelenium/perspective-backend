package org.meridor.perspective.shell.interactive.wizard.instances.add.step;

import org.meridor.perspective.shell.interactive.wizard.common.step.AbstractProjectStep;
import org.springframework.stereotype.Component;

@Component("addInstancesProjectStep")
public class ProjectStep extends AbstractProjectStep {
    
    @Override
    public String getMessage() {
        return "Select project to launch instances in:";
    }
    
}
