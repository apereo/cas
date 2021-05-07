package org.apereo.cas.configuration.model.support.cognito;

import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link AmazonCognitoAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-aws-cognito-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class AmazonCognitoAuthenticationProperties extends BaseAmazonWebServicesProperties {
    private static final long serialVersionUID = -4748558614314096213L;
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
     * The application client id, created in Cognito without a secret key.
     */
    @RequiredProperty
    private String clientId;

    /**
     * The user pool identifiers where accounts may be located.
     */
    @RequiredProperty
    private String userPoolId;

    /**
     * Map of attributes to rename after fetching from the user pool. Mapped attributes are defined using a key-value
     * structure where CAS allows the attribute name/key to be renamed virtually to a different attribute.
     * The key is the attribute fetched from the user pool and the value is the attribute name CAS should
     * use for virtual renames.
     */
    private Map<String, String> mappedAttributes = new LinkedHashMap<>();

    /**
     * Define the scope and state of this authentication handler
     * and the lifecycle in which it can be invoked or activated.
     */
    private AuthenticationHandlerStates state = AuthenticationHandlerStates.ACTIVE;
}
