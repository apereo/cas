package org.apereo.cas.web.flow;

import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.SamlIdPAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.SamlIdPConfiguration;
import org.apereo.cas.config.SamlIdPEndpointsConfiguration;
import org.apereo.cas.config.SamlIdPMetadataConfiguration;
import org.apereo.cas.config.SamlIdPThrottleConfiguration;
import org.apereo.cas.config.SamlIdPTicketSerializationConfiguration;
import org.apereo.cas.config.SamlIdPWebflowConfiguration;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.locator.FileSystemSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * This is {@link BaseSamlIdPWebflowTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    BaseSamlIdPWebflowTests.SamlIdPMetadataTestConfiguration.class,
    CoreSamlConfiguration.class,
    SamlIdPConfiguration.class,
    SamlIdPThrottleConfiguration.class,
    SamlIdPTicketSerializationConfiguration.class,
    SamlIdPAuthenticationServiceSelectionStrategyConfiguration.class,
    SamlIdPMetadataConfiguration.class,
    SamlIdPEndpointsConfiguration.class,
    SamlIdPWebflowConfiguration.class
})
public abstract class BaseSamlIdPWebflowTests extends BaseWebflowConfigurerTests {

    @Autowired
    @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
    protected SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    protected OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier(SamlIdPObjectSigner.DEFAULT_BEAN_NAME)
    protected SamlIdPObjectSigner samlIdPObjectSigner;

    @Autowired
    @Qualifier("samlIdPDistributedSessionStore")
    protected SessionStore samlIdPDistributedSessionStore;

    protected AuthnRequest signAuthnRequest(final HttpServletRequest request, final HttpServletResponse response,
                                            final AuthnRequest authnRequest, final SamlRegisteredService samlRegisteredService,
                                            final MessageContext messageContext) throws Exception {
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            samlRegisteredService, samlRegisteredService.getServiceId()).get();
        return samlIdPObjectSigner.encode(authnRequest, samlRegisteredService,
            adaptor, response, request, SAMLConstants.SAML2_POST_BINDING_URI,
            authnRequest, messageContext);
    }
    
    protected static AuthnRequest getAuthnRequestFor(final String service) {
        val authnRequest = mock(AuthnRequest.class);
        when(authnRequest.getID()).thenReturn(UUID.randomUUID().toString());
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service);
        when(authnRequest.getIssuer()).thenReturn(issuer);
        return authnRequest;
    }

    @TestConfiguration(value = "SamlIdPMetadataTestConfiguration", proxyBeanMethods = false)
    static class SamlIdPMetadataTestConfiguration {
        @Autowired
        @Qualifier("samlIdPMetadataCache")
        private Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache;

        @Bean
        public SamlIdPMetadataLocator samlIdPMetadataLocator() throws Exception {
            return new FileSystemSamlIdPMetadataLocator(
                new FileSystemResource(FileUtils.getTempDirectory()),
                samlIdPMetadataCache);
        }
    }
}


