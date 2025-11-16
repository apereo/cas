package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.util.Saml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate.SamlObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.sso.request.SSOSamlHttpRequestExtractor;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.TicketValidator;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;
import org.opensaml.core.xml.XMLObject;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.context.ApplicationContext;

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

    @NonNull
    private final SamlIdPObjectSigner samlObjectSigner;

    @NonNull
    private final SamlIdPObjectEncrypter samlObjectEncrypter;

    @NonNull
    private final AuthenticationSystemSupport authenticationSystemSupport;

    @NonNull
    private final ServicesManager servicesManager;

    @NonNull
    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @NonNull
    private final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    @NonNull
    private final OpenSamlConfigBean openSamlConfigBean;

    @NonNull
    private SamlProfileObjectBuilder<? extends XMLObject> responseBuilder;

    @NonNull
    private final Saml20ObjectBuilder logoutResponseBuilder;

    @NonNull
    private final CasConfigurationProperties casProperties;

    @NonNull
    private SamlObjectSignatureValidator samlObjectSignatureValidator;

    @NonNull
    private final Service callbackService;

    @NonNull
    @Deprecated(since = "7.3.0", forRemoval = true)
    private final CasCookieBuilder samlDistributedSessionCookieGenerator;

    @NonNull
    private SSOSamlHttpRequestExtractor samlHttpRequestExtractor;

    @NonNull
    private XMLMessageDecodersMap samlMessageDecoders;

    @NonNull
    private SamlProfileObjectBuilder<? extends XMLObject> samlFaultResponseBuilder;

    @NonNull
    private final TicketValidator ticketValidator;

    @NonNull
    private final TicketRegistry ticketRegistry;

    @NonNull
    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @NonNull
    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    @NonNull
    private final SessionStore sessionStore;

    @NonNull
    private final TicketRegistrySupport ticketRegistrySupport;

    @NonNull
    private final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    @NonNull
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    @NonNull
    private final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    @NonNull
    private final TicketFactory ticketFactory;

    @NonNull
    private final PersonAttributeDao attributeRepository;

    @NonNull
    private final ApplicationContext applicationContext;

    @NonNull
    private final PersistentIdGenerator persistentIdGenerator;
}
