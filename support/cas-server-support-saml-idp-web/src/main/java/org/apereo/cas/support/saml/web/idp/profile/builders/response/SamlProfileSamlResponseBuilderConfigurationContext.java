package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.pac4j.core.context.session.SessionStore;

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
public class SamlProfileSamlResponseBuilderConfigurationContext {

    private final transient VelocityEngine velocityEngineFactory;

    private final transient SamlIdPObjectSigner samlObjectSigner;

    private final CasConfigurationProperties casProperties;

    private final transient SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder;

    private final transient SamlIdPObjectEncrypter samlObjectEncrypter;

    private final transient OpenSamlConfigBean openSamlConfigBean;

    private final transient TicketRegistry ticketRegistry;

    private final transient CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final transient SAMLArtifactMap samlArtifactMap;

    private final transient SamlProfileObjectBuilder<? extends SAMLObject> samlSoapResponseBuilder;

    private final transient SessionStore sessionStore;

    private final transient CentralAuthenticationService centralAuthenticationService;

    private final MetadataResolver samlIdPMetadataResolver;

    private final TicketFactory ticketFactory;
}
