package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.core.authentication.policy.AllCredentialsAuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.policy.AllHandlersAuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.policy.AnyCredentialAuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.policy.NotPreventedAuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.policy.RequiredAttributesAuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.policy.RequiredAuthenticationHandlerAuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.policy.UniquePrincipalAuthenticationPolicyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties class for cas.authn.policy.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class AuthenticationPolicyProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 2039700004862120066L;

    /**
     * Global authentication policy that is applied when CAS attempts to vend and validate tickets.
     * Checks to make sure a particular authentication handler has successfully executed and validated credentials.
     * Required handlers are defined per registered service.
     */
    private boolean requiredHandlerAuthenticationPolicyEnabled;

    /**
     * If true, allows CAS to select authentication handlers based on the credential source.
     * This allows the authentication engine to restrict the task of validating credentials
     * to the selected source or account repository, as opposed to every authentication handler
     * registered with CAS at runtime.
     */
    private boolean sourceSelectionEnabled;

    /**
     * Satisfied if any authentication handler succeeds.
     * Allows options to avoid short circuiting and try every handler even if one prior succeeded.
     */
    @NestedConfigurationProperty
    private AnyCredentialAuthenticationPolicyProperties any = new AnyCredentialAuthenticationPolicyProperties();

    /**
     * Satisfied if an only if a specified handler successfully authenticates its credential.
     */
    @NestedConfigurationProperty
    private RequiredAuthenticationHandlerAuthenticationPolicyProperties req = new RequiredAuthenticationHandlerAuthenticationPolicyProperties();

    /**
     * Satisfied if and only if all given credentials are successfully authenticated.
     * Support for multiple credentials is new in CAS and this handler would
     * only be acceptable in a multi-factor authentication situation.
     */
    @NestedConfigurationProperty
    private AllCredentialsAuthenticationPolicyProperties all = new AllCredentialsAuthenticationPolicyProperties();

    /**
     * Satisfied if and only if all given authn handlers are successfully authenticated.
     */
    @NestedConfigurationProperty
    private AllHandlersAuthenticationPolicyProperties allHandlers = new AllHandlersAuthenticationPolicyProperties();

    /**
     * Execute a groovy script to detect authentication policy.
     */
    private List<GroovyAuthenticationPolicyProperties> groovy = new ArrayList<>();

    /**
     * Execute a rest endpoint to detect authentication policy.
     */
    private List<RestAuthenticationPolicyProperties> rest = new ArrayList<>();

    /**
     * Satisfied if an only if the authentication event is not blocked by a {@code PreventedException}.
     */
    @NestedConfigurationProperty
    private NotPreventedAuthenticationPolicyProperties notPrevented = new NotPreventedAuthenticationPolicyProperties();

    /**
     * Satisfied if an only if the principal has not already authenticated
     * and does not have an sso session with CAS. Otherwise, prevents
     * the user from logging in more than once. Note that this policy
     * adds an extra burden to the ticket store/registry as CAS needs
     * to query all relevant tickets found in the registry to cross-check
     * the requesting username with existing tickets.
     */
    @NestedConfigurationProperty
    private UniquePrincipalAuthenticationPolicyProperties uniquePrincipal = new UniquePrincipalAuthenticationPolicyProperties();

    /**
     * Satisfied if an only if the authentication contains the required attributes.
     */
    @NestedConfigurationProperty
    private RequiredAttributesAuthenticationPolicyProperties requiredAttributes = new RequiredAttributesAuthenticationPolicyProperties();
}
