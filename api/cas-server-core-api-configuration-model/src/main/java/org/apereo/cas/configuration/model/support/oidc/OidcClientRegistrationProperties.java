package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OidcClientRegistrationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OidcClientRegistrationProperties")
public class OidcClientRegistrationProperties implements Serializable {

    private static final long serialVersionUID = 123128615694269276L;

    /**
     * Whether dynamic registration operates in {@code OPEN} or {@code PROTECTED} mode.
     */
    private String dynamicClientRegistrationMode;

    /**
     * When client secret is issued by CAS, this is the time at which the client secret
     * will expire or 0 (blank or undefined work just as wel) if
     * it will not expire. The time is represented as the
     * number of seconds from 1970-01-01T0:0:0Z as measured in UTC until the date/time.
     */
    @DurationCapable
    private String clientSecretExpiration = "0";
}
