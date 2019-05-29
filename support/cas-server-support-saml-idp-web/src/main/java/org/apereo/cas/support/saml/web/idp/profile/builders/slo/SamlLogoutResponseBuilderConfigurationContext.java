package org.apereo.cas.support.saml.web.idp.profile.builders.slo;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.velocity.app.VelocityEngine;

/**
 * This is {@link SamlLogoutResponseBuilderConfigurationContext}.
 *
 * @author Krzysztof Zych
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@Builder
public class SamlLogoutResponseBuilderConfigurationContext {

    private final transient VelocityEngine velocityEngineFactory;

    private final transient SamlIdPObjectSigner samlObjectSigner;

    private final CasConfigurationProperties casProperties;

    private final transient SamlIdPObjectEncrypter samlObjectEncrypter;

    private final transient OpenSamlConfigBean openSamlConfigBean;
}
