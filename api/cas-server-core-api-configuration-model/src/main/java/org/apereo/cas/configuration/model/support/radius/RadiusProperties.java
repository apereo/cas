package org.apereo.cas.configuration.model.support.radius;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link RadiusProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-radius")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("RadiusProperties")
public class RadiusProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = 5244307919878753714L;

    /**
     * Whether catastrophic errors should be skipped
     * and fail over to the next server.
     */
    private boolean failoverOnException;

    /**
     * Whether authentication errors should be skipped
     * and fail over to the next server.
     */
    private boolean failoverOnAuthenticationFailure;

    /**
     * RADIUS server settings.
     */
    @NestedConfigurationProperty
    private RadiusServerProperties server = new RadiusServerProperties();

    /**
     * RADIUS client settings.
     */
    @NestedConfigurationProperty
    private RadiusClientProperties client = new RadiusClientProperties();

    /**
     * Password encoder settings.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Principal transformation settings.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * The name of the authentication handler.
     */
    private String name;

    /**
     * Define the scope and state of this authentication handler
     * and the lifecycle in which it can be invoked or activated.
     */
    private AuthenticationHandlerStates state = AuthenticationHandlerStates.ACTIVE;
}
