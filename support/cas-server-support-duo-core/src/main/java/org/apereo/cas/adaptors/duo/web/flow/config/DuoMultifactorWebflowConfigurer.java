package org.apereo.cas.adaptors.duo.web.flow.config;

import org.apereo.cas.adaptors.duo.authn.DuoCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.DynamicFlowModelBuilder;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
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

    private final VariegatedMultifactorAuthenticationProvider provider;

    public DuoMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                           final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                           final boolean enableDeviceRegistration,
                                           final VariegatedMultifactorAuthenticationProvider provider,
                                           final ApplicationContext applicationContext,
                                           final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, enableDeviceRegistration, applicationContext, casProperties);
        this.provider = provider;
    }

    @Override
    protected void doInitialize() {
        provider.getProviders().forEach(p -> {
            val duoFlowRegistry = buildDuoFlowRegistry(p);
            applicationContext.getAutowireCapableBeanFactory().initializeBean(duoFlowRegistry, p.getId());
            val cfg = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
            cfg.registerSingleton(p.getId(), duoFlowRegistry);
            registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), p.getId(), duoFlowRegistry);
        });

        casProperties.getAuthn().getMfa().getDuo()
            .stream()
            .filter(DuoSecurityMultifactorProperties::isTrustedDeviceEnabled)
            .forEach(duo -> {
                val id = duo.getId();
                try {
                    LOGGER.debug("Activating multifactor trusted authentication for webflow [{}]", id);
                    val registry = applicationContext.getBean(id, FlowDefinitionRegistry.class);
                    registerMultifactorTrustedAuthentication(registry);
                } catch (final Exception e) {
                    LOGGER.error("Failed to register multifactor trusted authentication for " + id, e);
                }
            });
    }

    private FlowDefinitionRegistry buildDuoFlowRegistry(final MultifactorAuthenticationProvider p) {
        val modelBuilder = new DynamicFlowModelBuilder();

        createDuoFlowVariables(modelBuilder);
        createDuoFlowStartActions(modelBuilder);
        createDuoFlowStates(modelBuilder);

        return createDuoFlowDefinitionRegistry(p, modelBuilder);
    }

    private FlowDefinitionRegistry createDuoFlowDefinitionRegistry(final MultifactorAuthenticationProvider p, final DynamicFlowModelBuilder modelBuilder) {
        val holder = new DefaultFlowModelHolder(modelBuilder);
        val flowBuilder = new FlowModelFlowBuilder(holder);
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilderServices);
        builder.addFlowBuilder(flowBuilder, p.getId());
        return builder.build();
    }

    private void createDuoFlowStates(final DynamicFlowModelBuilder modelBuilder) {
        val states = new ArrayList<AbstractStateModel>();

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
    }

    private void createDuoRedirectToRegistrationAction(final List<AbstractStateModel> states) {
        val endModel = new ViewStateModel("redirectToDuoRegistration");
        endModel.setView("externalRedirect:#{flowScope.duoRegistrationUrl}");
        states.add(endModel);
    }

    private void createDuoAuthenticationWebflowAction(final List<AbstractStateModel> states) {
        val actModel = new ActionStateModel(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel("duoAuthenticationWebflowAction"));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();

        var transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_ERROR);
        transModel.setTo(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private void createDuoLoginViewState(final List<AbstractStateModel> states) {
        val viewState = new ViewStateModel(STATE_ID_VIEW_LOGIN_FORM_DUO);
        viewState.setView("casDuoLoginView");
        viewState.setModel(CasWebflowConstants.VAR_ID_CREDENTIAL);
        val bm = new BinderModel();
        val bindings = new LinkedList<BindingModel>();
        val bme = new BindingModel("signedDuoResponse", null, null);
        bindings.add(bme);
        bm.setBindings(bindings);
        viewState.setBinder(bm);

        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel("prepareDuoWebLoginFormAction"));
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

    private void createDuoFinalizeAuthenticationAction(final List<AbstractStateModel> states) {
        val actModel = new ActionStateModel("finalizeAuthentication");
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel("duoAuthenticationWebflowAction"));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();
        val transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private void createDuoDoNonWebAuthenticationAction(final List<AbstractStateModel> states) {
        val actModel = new ActionStateModel("doNonWebAuthentication");
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel("duoNonWebAuthenticationAction"));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();

        val transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo("finalizeAuthentication");
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private void createDuoDetermineRequestAction(final List<AbstractStateModel> states) {
        val actModel = new ActionStateModel("determineDuoRequest");
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel("checkWebAuthenticationRequestAction"));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();

        var transModel = new TransitionModel();
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
        val actModel = new ActionStateModel("determineDuoUserAccount");
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel("determineDuoUserAccountAction"));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();
        var transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo("determineDuoRequest");
        trans.add(transModel);

        transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_ENROLL);
        transModel.setTo("redirectToDuoRegistration");
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
    }

    private LinkedList<AbstractActionModel> createDuoInitializeLoginAction(final List<AbstractStateModel> states) {
        val actModel = new ActionStateModel(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        val actions = new LinkedList<AbstractActionModel>();
        actions.add(new EvaluateModel("initializeLoginAction"));
        actModel.setActions(actions);

        val trans = new LinkedList<TransitionModel>();
        val transModel = new TransitionModel();
        transModel.setOn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        transModel.setTo("determineDuoUserAccount");
        trans.add(transModel);

        actModel.setTransitions(trans);
        states.add(actModel);
        return actions;
    }

    private void createDuoFlowStartActions(final DynamicFlowModelBuilder modelBuilder) {
        val starts = new ArrayList<AbstractActionModel>();
        starts.add(new EvaluateModel("initialFlowSetupAction"));
        modelBuilder.setOnStartActions(starts);
    }

    private void createDuoFlowVariables(final DynamicFlowModelBuilder modelBuilder) {
        val vars = new ArrayList<VarModel>();
        vars.add(new VarModel(CasWebflowConstants.VAR_ID_CREDENTIAL, DuoCredential.class.getName()));
        modelBuilder.setVars(vars);
    }
}
