package org.apereo.cas.configuration.model.support.radius;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link RadiusProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-radius")
@Slf4j
@Getter
@Setter
public class RadiusProperties implements Serializable {

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
     * Principal transoformation settings.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * The name of the authentication handler.
     */
    private String name;
}
