package org.apereo.cas.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.idp.slo.SamlIdPProfileSingleLogoutRequestProcessor;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlIdPResponseCustomizer;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.actions.logout.DelegatedSaml2ClientIdPRestoreSloRequestAction;
import org.apereo.cas.web.saml2.DelegatedAuthenticationSamlIdPResponseCustomizer;
import org.apereo.cas.web.saml2.DelegatedAuthenticationSamlIdPSingleLogoutRequestProcessor;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.mgmr.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.mgmr.DefaultCookieSameSitePolicy;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link DelegatedAuthenticationSaml2IdPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ConditionalOnClass({
    SamlIdPResponseCustomizer.class,
    SamlIdPProfileSingleLogoutRequestProcessor.class
})
@Configuration(value = "DelegatedAuthenticationSaml2IdPConfiguration", proxyBeanMethods = false)
class DelegatedAuthenticationSaml2IdPConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedSaml2IdPResponseCustomizer")
    public SamlIdPResponseCustomizer delegatedSaml2IdPResponseCustomizer(
        @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
        final DelegatedIdentityProviders identityProviders) {
        return new DelegatedAuthenticationSamlIdPResponseCustomizer(identityProviders);
    }
    
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_IDP_RESTORE_SLO_REQUEST)
    public Action delegatedSaml2ClientIdPRestoreSloRequestAction(
        @Qualifier("delegatedSaml2IdPSloRequestProcessor")
        final SamlIdPProfileSingleLogoutRequestProcessor delegatedSaml2IdPSloRequestProcessor,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new DelegatedSaml2ClientIdPRestoreSloRequestAction(delegatedSaml2IdPSloRequestProcessor))
            .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_IDP_RESTORE_SLO_REQUEST)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedSaml2IdPSloRequestProcessor")
    public SamlIdPProfileSingleLogoutRequestProcessor delegatedSaml2IdPSloRequestProcessor(
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory serviceFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier("delegatedSaml2IdPSloRequestCookieGenerator")
        final CasCookieBuilder delegatedSaml2IdPSloRequestCookieGenerator) {
        return new DelegatedAuthenticationSamlIdPSingleLogoutRequestProcessor(
            delegatedSaml2IdPSloRequestCookieGenerator, serviceFactory, servicesManager, casProperties);
    }

    @ConditionalOnMissingBean(name = "delegatedSaml2IdPSloRequestCookieGenerator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasCookieBuilder delegatedSaml2IdPSloRequestCookieGenerator(
        @Qualifier(TenantExtractor.BEAN_NAME)
        final TenantExtractor tenantExtractor,
        @Qualifier(GeoLocationService.BEAN_NAME)
        final ObjectProvider<GeoLocationService> geoLocationService,
        @Qualifier("delegatedClientDistributedSessionCookieCipherExecutor")
        final CipherExecutor delegatedClientDistributedSessionCookieCipherExecutor,
        final CasConfigurationProperties casProperties) {

        val sessionReplicationCookieProps = casProperties.getAuthn().getPac4j()
            .getCore()
            .getSessionReplication()
            .getCookie();
        val logoutRequestCookie = sessionReplicationCookieProps.withPinToSession(true);
        logoutRequestCookie.setName("Saml2LogoutRequest");
        
        val cookieValueManager = new DefaultCasCookieValueManager(
            delegatedClientDistributedSessionCookieCipherExecutor,
            tenantExtractor, geoLocationService,
            DefaultCookieSameSitePolicy.INSTANCE, logoutRequestCookie);
        return CookieUtils.buildCookieRetrievingGenerator(logoutRequestCookie, cookieValueManager);
    }
}

