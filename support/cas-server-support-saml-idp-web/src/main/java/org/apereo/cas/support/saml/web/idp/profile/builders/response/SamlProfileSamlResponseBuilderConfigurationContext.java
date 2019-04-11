package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml.saml2.core.Assertion;

/**
 * This is {@link SamlProfileSamlResponseBuilderConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@Builder
public class SamlProfileSamlResponseBuilderConfigurationContext {

    private final transient VelocityEngine velocityEngineFactory;

    private final transient SamlIdPObjectSigner samlObjectSigner;

    private final CasConfigurationProperties casProperties;

    private final transient SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder;

    private final transient SamlIdPObjectEncrypter samlObjectEncrypter;

    private final transient OpenSamlConfigBean openSamlConfigBean;

    private final transient TicketRegistry ticketRegistry;

    private final transient SamlArtifactTicketFactory samlArtifactTicketFactory;

    private final transient CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final transient SAMLArtifactMap samlArtifactMap;

    private final transient SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;

    private final transient SamlProfileObjectBuilder<? extends SAMLObject> samlSoapResponseBuilder;
}
