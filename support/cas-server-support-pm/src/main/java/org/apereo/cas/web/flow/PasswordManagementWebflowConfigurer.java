package org.apereo.cas.web.flow;

import com.google.common.collect.Lists;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.PasswordChangeBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link PasswordManagementWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PasswordManagementWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String CAS_MUST_CHANGE_PASS_VIEW = "casMustChangePassView";
    private static final String CAS_EXPIRED_PASS_VIEW = "casExpiredPassView";
    private static final String PASSWORD_CHANGE_ACTION = "passwordChangeAction";

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("initPasswordChangeAction")
    private Action passwordChangeAction;

    @Autowired
    private ApplicationContext applicationContext;
    
    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        createViewState(flow, "casAuthenticationBlockedView", "casAuthenticationBlockedView");
        createViewState(flow, "casBadWorkstationView", "casBadWorkstationView");
        createViewState(flow, "casBadHoursView", "casBadHoursView");
        createViewState(flow, "casAccountLockedView", "casAccountLockedView");
        createViewState(flow, "casAccountDisabledView", "casAccountDisabledView");
        createViewState(flow, "casAccountDisabledView", "casAccountDisabledView");
        createEndState(flow, "casPasswordUpdateSuccess", "casPasswordUpdateSuccessView");
        
        if (casProperties.getAuthn().getPm().isEnabled()) {
            configure(CAS_MUST_CHANGE_PASS_VIEW);
            configure(CAS_EXPIRED_PASS_VIEW);
        } else {
            createViewState(flow, CAS_MUST_CHANGE_PASS_VIEW, CAS_MUST_CHANGE_PASS_VIEW);
            createViewState(flow, CAS_EXPIRED_PASS_VIEW, CAS_EXPIRED_PASS_VIEW);
        }
    }
    
    private void configure(final String id) {
        final Flow flow = getLoginFlow();
        createFlowVariable(flow, "password", PasswordChangeBean.class);
        
        final BinderConfiguration binder = createStateBinderConfiguration(Lists.newArrayList("password", "confirmedPassword"));
        final ViewState viewState = createViewState(flow, id, id, binder);
        createStateModelBinding(viewState, "password", PasswordChangeBean.class);
        
        viewState.getEntryActionList().add(this.passwordChangeAction);
        final Transition transition = createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, PASSWORD_CHANGE_ACTION);
        transition.getAttributes().put("bind", Boolean.TRUE);
        transition.getAttributes().put("validate", Boolean.TRUE);
        
        createStateDefaultTransition(viewState, id);
        
        final ActionState pswChangeAction = createActionState(flow, PASSWORD_CHANGE_ACTION, createEvaluateAction(PASSWORD_CHANGE_ACTION));
        pswChangeAction.getTransitionSet().add(
                createTransition(PasswordChangeAction.PASSWORD_UPDATE_SUCCESS, "casPasswordUpdateSuccess"));
        pswChangeAction.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, id));
    }
}
