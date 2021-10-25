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
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate.SamlObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPLogoutResponseObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.sso.request.SSOSamlHttpRequestExtractor;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.jasig.cas.client.validation.TicketValidator;
import org.opensaml.saml.common.SAMLObject;
import org.pac4j.core.context.session.SessionStore;

import javax.annotation.Nonnull;

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

    @Nonnull
    private final SamlIdPObjectSigner samlObjectSigner;

    @Nonnull
    private final SamlIdPObjectEncrypter samlObjectEncrypter;

    @Nonnull
    private final AuthenticationSystemSupport authenticationSystemSupport;

    @Nonnull
    private final ServicesManager servicesManager;

    @Nonnull
    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Nonnull
    private final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    @Nonnull
    private final OpenSamlConfigBean openSamlConfigBean;

    @Nonnull
    private SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder;

    @Nonnull
    private final SamlIdPLogoutResponseObjectBuilder logoutResponseBuilder;

    @Nonnull
    private final CasConfigurationProperties casProperties;

    @Nonnull
    private final SamlObjectSignatureValidator samlObjectSignatureValidator;

    @Nonnull
    private final Service callbackService;

    @Nonnull
    private final CasCookieBuilder samlDistributedSessionCookieGenerator;

    @Nonnull
    private SSOSamlHttpRequestExtractor samlHttpRequestExtractor;

    @Nonnull
    private HttpServletRequestXMLMessageDecodersMap samlMessageDecoders;

    @Nonnull
    private SamlProfileObjectBuilder<? extends SAMLObject> samlFaultResponseBuilder;

    @Nonnull
    private final TicketValidator ticketValidator;

    @Nonnull
    private final TicketRegistry ticketRegistry;

    @Nonnull
    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Nonnull
    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    @Nonnull
    private final SessionStore sessionStore;

    @Nonnull
    private final TicketRegistrySupport ticketRegistrySupport;

    @Nonnull
    private final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    @Nonnull
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Nonnull
    private final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    @Nonnull
    private final TicketFactory ticketFactory;
}
