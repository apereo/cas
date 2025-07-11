package org.apereo.cas.configuration.model.support.syncope;

import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link SyncopeAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-syncope-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class SyncopeAuthenticationProperties extends BaseSyncopeProperties {

    @Serial
    private static final long serialVersionUID = -2446926316502297496L;

    /**
     * Define the scope and state of this authentication handler
     * and the lifecycle in which it can be invoked or activated.
     */
    private AuthenticationHandlerStates state = AuthenticationHandlerStates.ACTIVE;

    /**
     * Name of the authentication handler.
     */
    private String name;

    /**
     * The order of this authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * Max retry attempts for the authentication.
     */
    private int maxRetryAttempts;

    /**
     * Password encoder settings for the authentication handler.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * A number of authentication handlers are allowed to determine whether they can operate on the provided credential
     * and as such lend themselves to be tried and tested during the authentication handler selection phase.
     * The credential criteria may be one of the following options:<ul>
     * <li>1) A regular expression pattern that is tested against the credential identifier.</li>
     * <li>2) A fully qualified class name of your own design that implements {@code Predicate}.</li>
     * <li>3) Path to an external Groovy script that implements the same interface.</li>
     * </ul>
     */
    @RegularExpressionCapable
    private String credentialCriteria;

    /**
     * Map of attributes that optionally may be used to control the names
     * of the collected attributes from Syncope. If an attribute is provided by Syncope,
     * it can be listed here as the key of the map with a value that should be the name
     * of that attribute as collected and recorded by CAS.
     * For example, the convention {@code lastLoginDate->lastDate} will process the
     * Syncope attribute {@code lastLoginDate} and will internally rename that to {@code lastDate}.
     * If no mapping is specified, CAS defaults will be used instead.
     * In other words, this settings allows one to virtually rename and remap Syncope attributes
     * during the authentication event.
     */
    private Map<String, String> attributeMappings = new LinkedHashMap<>();

    /**
     * This is principal transformation properties.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Handling just-in-time provisioning settings.
     */
    @NestedConfigurationProperty
    private SyncopePrincipalProvisioningProperties provisioning = new SyncopePrincipalProvisioningProperties();
}
