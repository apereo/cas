package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link AbstractMultifactorTrustedDeviceWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public abstract class AbstractMultifactorTrustedDeviceWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {
    /**
     * Trusted authentication scope attribute.
     **/
    public static final String MFA_TRUSTED_AUTHN_SCOPE_ATTR = "mfaTrustedAuthentication";

    protected AbstractMultifactorTrustedDeviceWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                                final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                                final ConfigurableApplicationContext applicationContext,
                                                                final CasConfigurationProperties casProperties,
                                                                final Optional<FlowDefinitionRegistry> mfaFlowDefinitionRegistry,
                                                                final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties, mfaFlowDefinitionRegistry, mfaFlowCustomizers);
    }

    /**
     * Register multifactor trusted authentication into webflow.
     */
    protected void registerMultifactorTrustedAuthentication() {
        this.multifactorAuthenticationFlowDefinitionRegistries.forEach(this::registerMultifactorTrustedAuthentication);
    }

    /**
     * Register multifactor trusted authentication.
     *
     * @param registry the registry
     */
    protected void registerMultifactorTrustedAuthentication(final FlowDefinitionRegistry registry) {
        validateFlowDefinitionConfiguration();

        LOGGER.trace("Flow definitions found in the registry are [{}]", (Object[]) registry.getFlowDefinitionIds());
        val flowId = Arrays.stream(registry.getFlowDefinitionIds()).findFirst().orElseThrow();
        LOGGER.trace("Processing flow definition [{}]", flowId);

        val flow = (Flow) registry.getFlowDefinition(flowId);
        Assert.notNull(flow, String.format("%s flow cannot be null or undefined", flowId));

        val state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
        Assert.notNull(state, String.format("%s state cannot be null or undefined", CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));

        val transition = (Transition) state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        Assert.notNull(state, String.format("%s transition cannot be null or undefined", CasWebflowConstants.TRANSITION_ID_SUCCESS));

        val targetStateId = transition.getTargetStateId();
        transition.setTargetStateResolver(new DefaultTargetStateResolver(CasWebflowConstants.STATE_ID_VERIFY_TRUSTED_DEVICE));
        val verifyAction = createActionState(flow, CasWebflowConstants.STATE_ID_VERIFY_TRUSTED_DEVICE,
            CasWebflowConstants.ACTION_ID_MFA_VERIFY_TRUST_ACTION);

        val enableDeviceRegistration = casProperties.getAuthn().getMfa().getTrusted().getCore().isDeviceRegistrationEnabled();
        if (enableDeviceRegistration) {
            LOGGER.trace("Device registration is turned on for multifactor flow [{}]", flowId);
            createTransitionForState(verifyAction, CasWebflowConstants.TRANSITION_ID_YES,
                CasWebflowConstants.STATE_ID_FINISH_MFA_TRUSTED_AUTH);
        } else {
            createTransitionForState(verifyAction, CasWebflowConstants.TRANSITION_ID_YES, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        }
        createTransitionForState(verifyAction, CasWebflowConstants.TRANSITION_ID_NO, targetStateId);
        createTransitionForState(verifyAction, CasWebflowConstants.TRANSITION_ID_SKIP, targetStateId);

        val submit = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);

        val success = (Transition) submit.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        Assert.notNull(state, String.format("%s transition cannot be null or undefined", CasWebflowConstants.TRANSITION_ID_SUCCESS));
        if (enableDeviceRegistration) {
            success.setTargetStateResolver(new DefaultTargetStateResolver(CasWebflowConstants.STATE_ID_PREPARE_REGISTER_TRUSTED_DEVICE));
        } else {
            success.setTargetStateResolver(new DefaultTargetStateResolver(CasWebflowConstants.STATE_ID_REGISTER_TRUSTED_DEVICE));
        }

        createRegisterDeviceView(flow);

        val registerAction = createActionState(flow, CasWebflowConstants.STATE_ID_REGISTER_TRUSTED_DEVICE,
            CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION);
        createStateDefaultTransition(registerAction, CasWebflowConstants.STATE_ID_SUCCESS);
        Assert.isTrue(submit.getActionList().size() > 0, "There are no actions defined for " + flowId);
        val act = submit.getActionList().iterator().next();
        val finishMfaTrustedAuth = createActionState(flow, CasWebflowConstants.STATE_ID_FINISH_MFA_TRUSTED_AUTH, act);
        val finishedTransition = createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
        finishMfaTrustedAuth.getTransitionSet().add(finishedTransition);
        createStateDefaultTransition(finishMfaTrustedAuth, CasWebflowConstants.STATE_ID_SUCCESS);
    }

    private void createRegisterDeviceView(final Flow flow) {
        val prepareAction = createActionState(flow, CasWebflowConstants.STATE_ID_PREPARE_REGISTER_TRUSTED_DEVICE,
            CasWebflowConstants.ACTION_ID_MFA_PREPARE_TRUST_DEVICE_VIEW_ACTION);
        createTransitionForState(prepareAction, CasWebflowConstants.TRANSITION_ID_SKIP, CasWebflowConstants.STATE_ID_SUCCESS);
        createTransitionForState(prepareAction, CasWebflowConstants.TRANSITION_ID_REGISTER, CasWebflowConstants.STATE_ID_REGISTER_DEVICE_VIEW);
        createTransitionForState(prepareAction, CasWebflowConstants.TRANSITION_ID_STORE, CasWebflowConstants.STATE_ID_REGISTER_TRUSTED_DEVICE);

        createFlowVariable(flow, CasWebflowConstants.VAR_ID_MFA_TRUST_RECORD, MultifactorAuthenticationTrustBean.class);
        val fields = Arrays.stream(MultifactorAuthenticationTrustBean.class.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toList());
        val binder = createStateBinderConfiguration(fields);
        val viewRegister = createViewState(flow, CasWebflowConstants.STATE_ID_REGISTER_DEVICE_VIEW, "mfa-trusted-devices/casMfaRegisterDeviceView", binder);
        createStateModelBinding(viewRegister, CasWebflowConstants.VAR_ID_MFA_TRUST_RECORD, MultifactorAuthenticationTrustBean.class);
        createTransitionForState(viewRegister, CasWebflowConstants.TRANSITION_ID_SUBMIT,
            CasWebflowConstants.STATE_ID_REGISTER_TRUSTED_DEVICE, Map.of("bind", Boolean.TRUE, "validate", Boolean.TRUE));
        createTransitionForState(viewRegister, CasWebflowConstants.TRANSITION_ID_SKIP, CasWebflowConstants.STATE_ID_SUCCESS,
            Map.of("bind", Boolean.FALSE, "validate", Boolean.FALSE));
    }

    private void validateFlowDefinitionConfiguration() {
        this.multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {
            val msg = "CAS application context cannot find bean [%s] or [%s]. "
                + "This typically indicates that configuration is attempting to activate trusted-devices functionality for "
                + "multifactor authentication, yet the configuration modules that auto-configure the webflow are absent "
                + "from the CAS application runtime. If you have no need for trusted-devices functionality and wish to let the "
                + "multifactor authentication provider (and not CAS) remember and record trusted devices for you, you need to "
                + "turn this behavior off.";

            if (!applicationContext.containsBean(CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION)
                || !applicationContext.containsBean(CasWebflowConstants.ACTION_ID_MFA_VERIFY_TRUST_ACTION)) {
                throw new IllegalArgumentException(String.format(msg, CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION,
                    CasWebflowConstants.ACTION_ID_MFA_VERIFY_TRUST_ACTION));
            }
        });
    }
}
