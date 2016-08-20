package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.PasswordChangeBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowVariable;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.support.BeanFactoryVariableValueFactory;
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
            final TransitionableState submit = flow.getTransitionableState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            submit.getTransitionSet().add(createTransition("forgotPassword", CAS_MUST_CHANGE_PASS_VIEW));
        } else {
            createViewState(flow, CAS_MUST_CHANGE_PASS_VIEW, CAS_MUST_CHANGE_PASS_VIEW);
            createViewState(flow, CAS_EXPIRED_PASS_VIEW, CAS_EXPIRED_PASS_VIEW);
        }
    }
    
    private void configure(final String id) {
        final Flow flow = getLoginFlow();
        flow.addVariable(new FlowVariable("password", new BeanFactoryVariableValueFactory(PasswordChangeBean.class,
                            applicationContext.getAutowireCapableBeanFactory())));
        
        final BinderConfiguration binder = new BinderConfiguration();
        binder.addBinding(new BinderConfiguration.Binding("password", null, true));
        binder.addBinding(new BinderConfiguration.Binding("confirmedPassword", null, true));
        final ViewState viewState = createViewState(flow, id, id, binder);
        viewState.getAttributes().put("model", createExpression("password", PasswordChangeBean.class));
        
        viewState.getEntryActionList().add(this.passwordChangeAction);
        final Transition transition = createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, PASSWORD_CHANGE_ACTION);
        transition.getAttributes().put("bind", Boolean.TRUE);
        transition.getAttributes().put("validate", Boolean.TRUE);
        
        createStateDefaultTransition(viewState, id);
        
        final ActionState aupAcceptedAction = createActionState(flow, PASSWORD_CHANGE_ACTION, createEvaluateAction(PASSWORD_CHANGE_ACTION));
        aupAcceptedAction.getTransitionSet().add(createTransition("passwordUpdateSuccess", "casPasswordUpdateSuccess"));
        aupAcceptedAction.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, id));
    }
}
