package org.apereo.cas.configuration.model.core.sso;

import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * Configuration properties class for SSO settings.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class SingleSignOnProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -8777647966370741733L;

    /**
     * Indicate whether single sign-on should be turned on
     * and supported globally for the server.
     */
    private boolean ssoEnabled = true;

    /**
     * Flag that indicates whether to create SSO session on re-newed authentication event.
     */
    private boolean createSsoCookieOnRenewAuthn = true;

    /**
     * Indicates whether CAS proxy authentication/tickets
     * are supported by this server implementation.
     */
    private boolean proxyAuthnEnabled = true;

    /**
     * Indicates whether this server implementation should globally
     * support CAS protocol authentication requests that are tagged with "renew=true".
     */
    private boolean renewAuthnEnabled = true;

    /**
     * SSO behavior and settings, defined globally, that affects application treatment.
     */
    @NestedConfigurationProperty
    private SingleSignOnServicesProperties services = new SingleSignOnServicesProperties();

    @NestedConfigurationProperty
    private EmailProperties mail = new EmailProperties();

    @NestedConfigurationProperty
    private SmsProperties sms = new SmsProperties();
}
