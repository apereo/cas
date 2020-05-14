package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JsonGoogleAuthenticatorMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-gauth")
@Getter
@Setter
@Accessors(chain = true)
public class JsonGoogleAuthenticatorMultifactorProperties extends SpringResourceProperties {
    private static final long serialVersionUID = 4303355159388663888L;
}
