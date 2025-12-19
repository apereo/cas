package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link SamlProfileSamlResponseBuilderConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@SuperBuilder
@With
@AllArgsConstructor
public class SamlProfileSamlResponseBuilderConfigurationContext {
    private final ConfigurableApplicationContext applicationContext;

    private final VelocityEngine velocityEngineFactory;

    private final SamlIdPObjectSigner samlObjectSigner;

    private final CasConfigurationProperties casProperties;

    private final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder;

    private final SamlIdPObjectEncrypter samlObjectEncrypter;

    private final OpenSamlConfigBean openSamlConfigBean;

    private final TicketRegistry ticketRegistry;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final SAMLArtifactMap samlArtifactMap;

    private final SamlProfileObjectBuilder<? extends SAMLObject> samlSoapResponseBuilder;

    private final SessionStore sessionStore;

    private final CentralAuthenticationService centralAuthenticationService;

    private final MetadataResolver samlIdPMetadataResolver;

    private final TicketFactory ticketFactory;
}
