package org.apereo.cas.configuration.model.support.radius;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link RadiusProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-radius")
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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public PrincipalTransformationProperties getPrincipalTransformation() {
        return principalTransformation;
    }

    public void setPrincipalTransformation(final PrincipalTransformationProperties principalTransformation) {
        this.principalTransformation = principalTransformation;
    }

    public PasswordEncoderProperties getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isFailoverOnException() {
        return failoverOnException;
    }

    public void setFailoverOnException(final boolean failoverOnException) {
        this.failoverOnException = failoverOnException;
    }

    public boolean isFailoverOnAuthenticationFailure() {
        return failoverOnAuthenticationFailure;
    }

    public void setFailoverOnAuthenticationFailure(final boolean failoverOnAuthenticationFailure) {
        this.failoverOnAuthenticationFailure = failoverOnAuthenticationFailure;
    }

    public RadiusServerProperties getServer() {
        return server;
    }

    public void setServer(final RadiusServerProperties server) {
        this.server = server;
    }

    public RadiusClientProperties getClient() {
        return client;
    }

    public void setClient(final RadiusClientProperties client) {
        this.client = client;
    }
}
