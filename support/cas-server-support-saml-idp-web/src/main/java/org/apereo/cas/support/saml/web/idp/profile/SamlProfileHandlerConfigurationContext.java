package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate.SamlObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.sso.request.SSOSamlHttpRequestExtractor;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.opensaml.saml.common.SAMLObject;

/**
 * This is {@link SamlProfileHandlerConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@Builder
public class SamlProfileHandlerConfigurationContext {

    private final SamlIdPObjectSigner samlObjectSigner;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    private final OpenSamlConfigBean openSamlConfigBean;

    private final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder;

    private final CasConfigurationProperties casProperties;

    private final SamlObjectSignatureValidator samlObjectSignatureValidator;

    private final Service callbackService;

    private final SSOSamlHttpRequestExtractor samlHttpRequestExtractor;

    private final HttpServletRequestXMLMessageDecodersMap samlMessageDecoders;

    private final SamlProfileObjectBuilder<? extends SAMLObject> samlFaultResponseBuilder;

    private final AbstractUrlBasedTicketValidator ticketValidator;

    private final TicketRegistry ticketRegistry;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;

    private final SamlArtifactTicketFactory artifactTicketFactory;
}
