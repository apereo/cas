package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate.SamlObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPLogoutResponseObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.sso.request.SSOSamlHttpRequestExtractor;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.jasig.cas.client.validation.TicketValidator;
import org.opensaml.saml.common.SAMLObject;
import org.pac4j.core.context.session.SessionStore;

/**
 * This is {@link SamlProfileHandlerConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@SuperBuilder
public class SamlProfileHandlerConfigurationContext {

    private final SamlIdPObjectSigner samlObjectSigner;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    private final OpenSamlConfigBean openSamlConfigBean;

    private final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder;

    private final SamlIdPLogoutResponseObjectBuilder logoutResponseBuilder;

    private final CasConfigurationProperties casProperties;

    private final SamlObjectSignatureValidator samlObjectSignatureValidator;

    private final Service callbackService;

    private final CasCookieBuilder samlDistributedSessionCookieGenerator;

    private final SSOSamlHttpRequestExtractor samlHttpRequestExtractor;

    private final HttpServletRequestXMLMessageDecodersMap samlMessageDecoders;

    private final SamlProfileObjectBuilder<? extends SAMLObject> samlFaultResponseBuilder;

    private final TicketValidator ticketValidator;

    private final TicketRegistry ticketRegistry;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;

    private final SamlArtifactTicketFactory artifactTicketFactory;

    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    private final SessionStore sessionStore;

    private final TicketRegistrySupport ticketRegistrySupport;

    private final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
}
