package org.apereo.cas.configuration.model.support.azuread;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link AzureActiveDirectoryAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-azuread-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class AzureActiveDirectoryAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = -21355975558426360L;

    /**
     * The name of the authentication handler.
     */
    private String name;

    /**
     * The order of this authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * Password encoding properties.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Principal transformation properties.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Client id of the application.
     */
    @RequiredProperty
    private String clientId;

    /**
     * Login url including the tenant id.
     */
    private String loginUrl = "https://login.microsoftonline.com/common/";

    /**
     * Resource url for the graph API to fetch attributes.
     */
    private String resource = "https://graph.microsoft.com/";

    /**
     * A number of authentication handlers are allowed to determine whether they can operate on the provided credential
     * and as such lend themselves to be tried and tested during the authentication handler selection phase.
     * The credential criteria may be one of the following options:<ul>
     * <li>1) A regular expression pattern that is tested against the credential identifier.</li>
     * <li>2) A fully qualified class name of your own design that implements {@code Predicate<Credential>}.</li>
     * <li>3) Path to an external Groovy script that implements the same interface.</li>
     * </ul>
     */
    private String credentialCriteria;
}
