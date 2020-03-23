package org.apereo.cas.configuration.model.support.cognito;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
}
