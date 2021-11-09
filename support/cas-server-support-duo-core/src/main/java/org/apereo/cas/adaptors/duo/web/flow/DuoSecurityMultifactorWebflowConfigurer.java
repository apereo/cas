package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.trusted.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.configurer.DynamicFlowModelBuilder;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DuoSecurityMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SuppressWarnings("JdkObsolete")
@Slf4j
public class DuoSecurityMultifactorWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {

    private static final String VIEW_ID_REDIRECT_TO_DUO_REGISTRATION = "redirectToDuoRegistration";

    public DuoSecurityMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, loginFlowDefinitionRegistry,
            applicationContext, casProperties, Optional.empty(), mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        val duoConfig = casProperties.getAuthn().getMfa().getDuo();
        var flowRegistryBeans = duoConfig
            .stream()
            .map(duo -> {
                val duoFlowRegistry = buildDuoFlowRegistry(duo);
                val duoFlowRegistryInstance = ApplicationContextProvider.registerBeanIntoApplicationContext(
                    applicationContext, duoFlowRegistry, duo.getId());
                return Pair.of(duo.getId(), duoFlowRegistryInstance);
            })
            .collect(Collectors.toList());
        val flowRegistries = flowRegistryBeans.stream().map(Pair::getValue).collect(Collectors.toList());
        getMultifactorAuthenticationFlowDefinitionRegistries().addAll(flowRegistries);
        flowRegistryBeans.forEach(duo -> registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), duo.getKey(), duo.getKey()));

        duoConfig
            .stream()
            .filter(DuoSecurityMultifactorAuthenticationProperties::isTrustedDeviceEnabled)
            .forEach(duo -> {
                val id = duo.getId();
                LOGGER.debug("Activating multifactor trusted authentication for webflow [{}]", id);
                val registry = applicationContext.getBean(id, FlowDefinitionRegistry.class);
                registerMultifactorTrustedAuthentication(registry);
            });
    }

    private FlowDefinitionRegistry buildDuoFlowRegistry(final DuoSecurityMultifactorAuthenticationProperties properties) {
        val modelBuilder = new DynamicFlowModelBuilder();

        createDuoFlowVariables(modelBuilder);
        createDuoFlowStartActions(modelBuilder);
        createDuoFlowStates(modelBuilder, properties);

        if (StringUtils.isBlank(properties.getDuoApplicationKey())) {
            createDuoFlowUniversalPromptActions(getLoginFlow());
        }

        return createDuoFlowDefinitionRegistry(properties, modelBuilder);
    }

    private void createDuoFlowUniversalPromptActions(final Flow flow) {
        val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN,
            "duoUniversalPromptValidateLoginAction");

        val realSubmit = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        val targetSuccess = realSubmit.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccess);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SKIP, getStartState(flow).getId());
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE);
        setStartState(flow, actionState);
    }

    private FlowDefinitionRegistry createDuoFlowDefinitionRegistry(final DuoSecurityMultifactorAuthenticationProperties p,
        final DynamicFlowModelBuilder modelBuilder) {
        val holder = new DefaultFlowModelHolder(modelBuilder);
        val flowBuilder = new FlowModelFlowBuilder(holder);
        val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
        builder.addFlowBuilder(flowBuilder, p.getId());
        return builder.build();
    }

    private static void createDuoFlowStates(final DynamicFlowModelBuilder modelBuilder,
        final DuoSecurityMultifactorAuthenticationProperties properties) {
        val states = new ArrayList<AbstractStateModel>();

        createDuoInitializeLoginAction(states);
        createDuoDetermineUserAccountAction(states);
        createDuoDetermineRequestAction(states);
        createDuoDoNonWebAuthenticationAction(states);
        createDuoFinalizeAuthenticationAction(states);

        if (StringUtils.isBlank(properties.getDuoApplicationKey())) {
            createDuoUniversalPromptLoginViewState(states);
        } else {
            createDuoLoginViewState(states);
        }
        createDuoAuthenticationWebflowAction(states);
        createDuoRedirectToRegistrationAction(states);
        createDuoSuccessEndState(states);

        modelBuilder.setStates(states);
    }

    private static void createDuoUniversalPromptLoginViewState(final ArrayList<AbstractStateModel> states) {
        val viewState = new ViewStateModel(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM_DUO);
        val actions = new LinkedList<AbstractActionModel>();
        val action = new EvaluateModel(CasWebflowConstants.ACTION_ID_DUO_UNIVERSAL_PROMPT_PREPARE_LOGIN);
        actions.add(action);
        viewState.setOnEntryActions(actions);
        viewState.setView("externalRedirect:#{flowScope.duoUniversalPromptLoginUrl}");
        states.add(viewState);
    }

    private static void createDuoSuccessEndState(final List<AbstractStateModel> states) {
        states.add(new EndStateModel(CasWebflowConstants.STATE_ID_SUCCESS));
        states.add(new EndStateModel(CasWebflowConstants.STATE_ID_MFA_DENIED));
        states.add(new EndStateModel(CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE));
    }

    private static void createDuoRedirectToRegistrationAction(final List<AbstractStateModel> states) {
        val endModel = new ViewStateModel(VIEW_ID_REDIRECT_TO_DUO_REGISTRATION);
        endModel.setView("externalRedirect:#{flowScope.duoRegistrationUrl}");
        states.add(endModel);
    }

    private static void createDuoAuthenticationWebflowAction(final List<AbstractStateModel> states) {
        val actModel = new ActionStateModel(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel(CasWebflowConstants.ACTION_ID_DUO_AUTHENTICATION_WEBFLOW));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();

        var transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo(CasWebflowConstants.STATE_ID_SUCCESS);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_ERROR);
        transModel.setTo(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_DENY);
        transModel.setTo(CasWebflowConstants.STATE_ID_MFA_DENIED);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        transModel.setTo(CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE);
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private static void createDuoLoginViewState(final List<AbstractStateModel> states) {
        val viewState = new ViewStateModel(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM_DUO);
        viewState.setView("duo-security/casDuoLoginView");
        viewState.setModel(CasWebflowConstants.VAR_ID_CREDENTIAL);
        val bm = new BinderModel();
        val bindings = new LinkedList<BindingModel>();
        val bme = new BindingModel("signedDuoResponse", null, null);
        bindings.add(bme);
        bm.setBindings(bindings);
        viewState.setBinder(bm);

        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel(CasWebflowConstants.ACTION_ID_PREPARE_DUO_WEB_LOGIN_FORM_ACTION));
        viewState.setOnEntryActions(actions);

        val trans = new LinkedList<TransitionModel>();
        val transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUBMIT);
        transModel.setTo(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        transModel.setBind(Boolean.TRUE.toString());
        transModel.setValidate(Boolean.FALSE.toString());

        trans.add(transModel);
        viewState.setTransitions(trans);
        states.add(viewState);

    }

    private static void createDuoFinalizeAuthenticationAction(final List<AbstractStateModel> states) {
        val actModel = new ActionStateModel(CasWebflowConstants.STATE_ID_FINALIZE_AUTHENTICATION);
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel(CasWebflowConstants.ACTION_ID_DUO_AUTHENTICATION_WEBFLOW));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();
        val transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo(CasWebflowConstants.STATE_ID_SUCCESS);
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private static void createDuoDoNonWebAuthenticationAction(final List<AbstractStateModel> states) {
        val actModel = new ActionStateModel(CasWebflowConstants.STATE_ID_DUO_NON_WEB_AUTHENTICATION);
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel(CasWebflowConstants.ACTION_ID_DUO_NON_WEB_AUTHENTICATION_ACTION));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();

        val transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo(CasWebflowConstants.STATE_ID_FINALIZE_AUTHENTICATION);
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private static void createDuoDetermineRequestAction(final List<AbstractStateModel> states) {
        val actModel = new ActionStateModel(CasWebflowConstants.STATE_ID_DETERMINE_DUO_REQUEST);
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel(CasWebflowConstants.ACTION_ID_CHECK_WEB_AUTHENTICATION_REQUEST));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();

        var transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_YES);
        transModel.setTo(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM_DUO);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_NO);
        transModel.setTo(CasWebflowConstants.STATE_ID_DUO_NON_WEB_AUTHENTICATION);
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private static void createDuoDetermineUserAccountAction(final List<AbstractStateModel> states) {
        val actModel = new ActionStateModel(CasWebflowConstants.STATE_ID_MFA_PRE_AUTH);
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel(CasWebflowConstants.ACTION_ID_DETERMINE_DUO_USER_ACCOUNT));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();
        var transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo(CasWebflowConstants.STATE_ID_DETERMINE_DUO_REQUEST);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_ENROLL);
        transModel.setTo(VIEW_ID_REDIRECT_TO_DUO_REGISTRATION);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_BYPASS);
        transModel.setTo(CasWebflowConstants.STATE_ID_MFA_CHECK_BYPASS);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        transModel.setTo(CasWebflowConstants.STATE_ID_MFA_FAILURE);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_DENY);
        transModel.setTo(CasWebflowConstants.STATE_ID_MFA_DENIED);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_ERROR);
        transModel.setTo(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private static LinkedList<AbstractActionModel> createDuoInitializeLoginAction(final List<AbstractStateModel> states) {
        val actModel = new ActionStateModel(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();
        val transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo(CasWebflowConstants.STATE_ID_DETERMINE_DUO_USER_ACCOUNT);
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
        return actions;
    }

    private static void createDuoFlowStartActions(final DynamicFlowModelBuilder modelBuilder) {
        val starts = new ArrayList<AbstractActionModel>(1);
        starts.add(new EvaluateModel(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
        modelBuilder.setOnStartActions(starts);
    }

    private static void createDuoFlowVariables(final DynamicFlowModelBuilder modelBuilder) {
        val vars = new ArrayList<VarModel>(1);
        vars.add(new VarModel(CasWebflowConstants.VAR_ID_CREDENTIAL, DuoSecurityCredential.class.getName()));
        modelBuilder.setVars(vars);
    }
}
