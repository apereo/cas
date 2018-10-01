package org.apereo.cas.adaptors.duo.web.flow.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.duo.authn.DuoCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.web.flow.configurer.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.DynamicFlowModelBuilder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.model.FlowModelFlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.model.AbstractActionModel;
import org.springframework.webflow.engine.model.AbstractStateModel;
import org.springframework.webflow.engine.model.ActionStateModel;
import org.springframework.webflow.engine.model.BinderModel;
import org.springframework.webflow.engine.model.BindingModel;
import org.springframework.webflow.engine.model.EndStateModel;
import org.springframework.webflow.engine.model.EvaluateModel;
import org.springframework.webflow.engine.model.TransitionModel;
import org.springframework.webflow.engine.model.VarModel;
import org.springframework.webflow.engine.model.ViewStateModel;
import org.springframework.webflow.engine.model.builder.DefaultFlowModelHolder;
import org.springframework.webflow.engine.model.registry.FlowModelHolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This is {@link DuoMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SuppressWarnings("JdkObsolete")
@Slf4j
public class DuoMultifactorWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {

    private static final String STATE_ID_VIEW_LOGIN_FORM_DUO = "viewLoginFormDuo";


    public DuoMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices, 
                                           final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                           final boolean enableDeviceRegistration, 
                                           final ApplicationContext applicationContext,
                                           final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, enableDeviceRegistration, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        casProperties.getAuthn().getMfa().getDuo().forEach(duo -> {
            final FlowDefinitionRegistry duoFlowRegistry = buildDuoFlowRegistry(duo);
            applicationContext.getAutowireCapableBeanFactory().initializeBean(duoFlowRegistry, duo.getId());
            final ConfigurableListableBeanFactory cfg = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
            cfg.registerSingleton(duo.getId(), duoFlowRegistry);
            registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), duo.getId(), duoFlowRegistry, duo.getId());
        });

        casProperties.getAuthn().getMfa().getDuo()
                .stream()
                .filter(DuoSecurityMultifactorProperties::isTrustedDeviceEnabled)
                .forEach(duo -> {
                    final String id = duo.getId();
                    try {
                        LOGGER.debug("Activating multifactor trusted authentication for webflow [{}]", id);
                        final FlowDefinitionRegistry registry = applicationContext.getBean(id, FlowDefinitionRegistry.class);
                        registerMultifactorTrustedAuthentication(registry);
                    } catch (final Exception e) {
                        LOGGER.error("Failed to register multifactor trusted authentication for " + id, e);
                    }
                });
    }

    private FlowDefinitionRegistry buildDuoFlowRegistry(final DuoSecurityMultifactorProperties duo) {
        final DynamicFlowModelBuilder modelBuilder = new DynamicFlowModelBuilder();
        
        createDuoFlowVariables(modelBuilder);
        createDuoFlowStartActions(modelBuilder);
        createDuoFlowStates(modelBuilder);

        return createDuoFlowDefinitionRegistry(duo, modelBuilder);
    }

    private FlowDefinitionRegistry createDuoFlowDefinitionRegistry(final DuoSecurityMultifactorProperties duo, final DynamicFlowModelBuilder modelBuilder) {
        final FlowModelHolder holder = new DefaultFlowModelHolder(modelBuilder);
        final FlowBuilder flowBuilder = new FlowModelFlowBuilder(holder);
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilderServices);
        builder.addFlowBuilder(flowBuilder, duo.getId());
        return builder.build();
    }

    private void createDuoFlowStates(final DynamicFlowModelBuilder modelBuilder) {
        final List<AbstractStateModel> states = new ArrayList<>();

        createDuoInitializeLoginAction(states);
        createDuoDetermineUserAccountAction(states);
        createDuoDetermineRequestAction(states);
        createDuoDoNonWebAuthenticationAction(states);
        createDuoFinalizeAuthenticationAction(states);
        createDuoLoginViewState(states);
        createDuoAuthenticationWebflowAction(states);
        createDuoRedirectToRegistrationAction(states);
        createDuoSuccessEndState(states);

        modelBuilder.setStates(states);
    }

    private void createDuoSuccessEndState(final List<AbstractStateModel> states) {
        states.add(new EndStateModel(CasWebflowConstants.TRANSITION_ID_SUCCESS));
        states.add(new EndStateModel(CasWebflowConstants.TRANSITION_ID_DENY));
        states.add(new EndStateModel(CasWebflowConstants.TRANSITION_ID_UNAVAILABLE));

    }

    private void createDuoRedirectToRegistrationAction(final List<AbstractStateModel> states) {
        final ViewStateModel endModel = new ViewStateModel("redirectToDuoRegistration");
        endModel.setView("externalRedirect:#{flowScope.duoRegistrationUrl}");
        states.add(endModel);
    }

    private void createDuoAuthenticationWebflowAction(final List<AbstractStateModel> states) {
        final ActionStateModel actModel = new ActionStateModel(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        final LinkedList<AbstractActionModel> actions = new LinkedList<>();
        actions.add(new EvaluateModel("duoAuthenticationWebflowAction"));
        actModel.setActions(actions);

        final LinkedList<TransitionModel> trans = new LinkedList<>();

        TransitionModel transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_ERROR);
        transModel.setTo(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_DENY);
        transModel.setTo(CasWebflowConstants.TRANSITION_ID_DENY);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        transModel.setTo(CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private void createDuoLoginViewState(final List<AbstractStateModel> states) {
        final ViewStateModel viewState = new ViewStateModel(STATE_ID_VIEW_LOGIN_FORM_DUO);
        viewState.setView("casDuoLoginView");
        viewState.setModel(CasWebflowConstants.VAR_ID_CREDENTIAL);
        final BinderModel bm = new BinderModel();
        final LinkedList<BindingModel> bindings = new LinkedList<>();
        final BindingModel bme = new BindingModel("signedDuoResponse", null, null);
        bindings.add(bme);
        bm.setBindings(bindings);
        viewState.setBinder(bm);

        final LinkedList<AbstractActionModel> actions = new LinkedList<>();
        actions.add(new EvaluateModel("prepareDuoWebLoginFormAction"));
        viewState.setOnEntryActions(actions);

        final LinkedList<TransitionModel> trans = new LinkedList<>();
        final TransitionModel transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUBMIT);
        transModel.setTo(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        transModel.setBind(Boolean.TRUE.toString());
        transModel.setValidate(Boolean.FALSE.toString());

        trans.add(transModel);
        viewState.setTransitions(trans);
        states.add(viewState);

    }

    private void createDuoFinalizeAuthenticationAction(final List<AbstractStateModel> states) {
        final ActionStateModel actModel = new ActionStateModel("finalizeAuthentication");
        final LinkedList<AbstractActionModel> actions = new LinkedList<>();
        actions.add(new EvaluateModel("duoAuthenticationWebflowAction"));
        actModel.setActions(actions);

        final LinkedList<TransitionModel> trans = new LinkedList<>();
        final TransitionModel transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private void createDuoDoNonWebAuthenticationAction(final List<AbstractStateModel> states) {
        final ActionStateModel actModel = new ActionStateModel("doNonWebAuthentication");
        final LinkedList<AbstractActionModel> actions = new LinkedList<>();
        actions.add(new EvaluateModel("duoNonWebAuthenticationAction"));
        actModel.setActions(actions);

        final LinkedList<TransitionModel> trans = new LinkedList<>();

        final TransitionModel transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo("finalizeAuthentication");
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private void createDuoDetermineRequestAction(final List<AbstractStateModel> states) {
        final ActionStateModel actModel = new ActionStateModel("attemptAuth");
        final LinkedList<AbstractActionModel> actions = new LinkedList<>();
        actions.add(new EvaluateModel("checkWebAuthenticationRequestAction"));
        actModel.setActions(actions);

        final LinkedList<TransitionModel> trans = new LinkedList<>();

        TransitionModel transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_YES);
        transModel.setTo(STATE_ID_VIEW_LOGIN_FORM_DUO);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_NO);
        transModel.setTo("doNonWebAuthentication");
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private void createDuoDetermineUserAccountAction(final List<AbstractStateModel> states) {
        final ActionStateModel actModel = new ActionStateModel(CasWebflowConstants.STATE_ID_MFA_PRE_AUTH);
        final LinkedList<AbstractActionModel> actions = new LinkedList<>();
        actions.add(new EvaluateModel("determineDuoUserAccountAction"));
        actModel.setActions(actions);

        final LinkedList<TransitionModel> trans = new LinkedList<>();
        TransitionModel transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo("attemptAuth");
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_ENROLL);
        transModel.setTo("redirectToDuoRegistration");
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_BYPASS);
        transModel.setTo(CasWebflowConstants.STATE_ID_CHECK_BYPASS);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        transModel.setTo(CasWebflowConstants.TRANSITION_ID_FAILURE);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_DENY);
        transModel.setTo(CasWebflowConstants.TRANSITION_ID_DENY);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_ERROR);
        transModel.setTo(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private LinkedList<AbstractActionModel> createDuoInitializeLoginAction(final List<AbstractStateModel> states) {
        final ActionStateModel actModel = new ActionStateModel(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        final LinkedList<AbstractActionModel> actions = new LinkedList<>();
        actions.add(new EvaluateModel("initializeLoginAction"));
        actModel.setActions(actions);


        final LinkedList<TransitionModel> trans = new LinkedList<>();
        final TransitionModel transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo("attemptAuth");
        trans.add(transModel);

        actModel.setTransitions(trans);

        states.add(actModel);
        return actions;
    }

    private void createDuoFlowStartActions(final DynamicFlowModelBuilder modelBuilder) {
        final List<AbstractActionModel> starts = new ArrayList<>();
        starts.add(new EvaluateModel("initialFlowSetupAction"));
        modelBuilder.setOnStartActions(starts);
    }

    private void createDuoFlowVariables(final DynamicFlowModelBuilder modelBuilder) {
        final List<VarModel> vars = new ArrayList<>();
        vars.add(new VarModel(CasWebflowConstants.VAR_ID_CREDENTIAL, DuoCredential.class.getName()));
        modelBuilder.setVars(vars);
    }
}
