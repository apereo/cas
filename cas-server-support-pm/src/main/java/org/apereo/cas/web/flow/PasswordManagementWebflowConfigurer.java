package org.apereo.cas.web.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link PasswordManagementWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PasswordManagementWebflowConfigurer extends AbstractCasWebflowConfigurer {
    
    @Autowired
    @Qualifier("passwordChangeAction")
    private Action passwordChangeAction;
    
    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        flow.getStateInstance("casMustChangePassView").getEntryActionList().add(this.passwordChangeAction);
        flow.getStateInstance("casExpiredPassView").getEntryActionList().add(this.passwordChangeAction);
    }
}
