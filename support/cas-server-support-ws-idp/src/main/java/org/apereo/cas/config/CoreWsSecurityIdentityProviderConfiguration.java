package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ws.idp.DefaultFederationClaim;
import org.apereo.cas.ws.idp.DefaultFederationRelyingParty;
import org.apereo.cas.ws.idp.DefaultIdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.DefaultRealmAwareIdentityProvider;
import org.apereo.cas.ws.idp.FederationMetadataServlet;
import org.apereo.cas.ws.idp.api.FederationRelyingParty;
import org.apereo.cas.ws.idp.api.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.api.RealmAwareIdentityProvider;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This is {@link CoreWsSecurityIdentityProviderConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("coreWsSecurityIdentityProviderConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportResource(locations = {"classpath:META-INF/cxf/cxf.xml"})
public class CoreWsSecurityIdentityProviderConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    @Lazy
    @Bean
    public ServletRegistrationBean wsIdpMetadataServlet() {
        final ServletRegistrationBean bean = new ServletRegistrationBean();
        bean.setEnabled(true);
        bean.setName("federationServletIdentityProvider");
        bean.setServlet(new FederationMetadataServlet("urn:org:apache:cxf:fediz:idp:realm-A"));
        bean.setUrlMappings(Collections.singleton("/ws/idp/metadata"));
        bean.setAsyncSupported(true);
        return bean;
    }

    @Bean
    public IdentityProviderConfigurationService idpConfigService() {
        return new DefaultIdentityProviderConfigurationService(identityProviders(), relyingParties());
    }

    @Bean
    public List<RealmAwareIdentityProvider> identityProviders() {
        try {
            final DefaultRealmAwareIdentityProvider idp = new DefaultRealmAwareIdentityProvider();

            idp.setRealm("urn:org:apache:cxf:fediz:idp:realm-A");
            idp.setUri("realma");
            idp.setStsUrl(new URL("https://mmoayyed.unicon.net:8443/ws/sts/REALMA"));
            idp.setIdpUrl(new URL("https://mmoayyed.unicon.net:8443/ws/idp/federation"));
            idp.setCertificate(resourceLoader.getResource("classpath:stsKeystoreA.properties"));
            idp.setCertificatePassword("realma");
            idp.setSupportedProtocols(Arrays.asList("http://docs.oasis-open.org/wsfed/federation/200706",
                    "http://docs.oasis-open.org/ws-sx/ws-trust/200512"));
            idp.setRelyingParties(Collections.singletonMap("urn:org:apache:cxf:fediz:fedizhelloworld", relyingParty()));
            idp.setAuthenticationURIs(Collections.singletonMap("default", "federation/up"));
            idp.setDescription("Identity Provider for Realm A");
            idp.setDisplayName("Realm A");
            return Arrays.asList(idp);
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public List<FederationRelyingParty> relyingParties() {
        try {
            return Arrays.asList(relyingParty());
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    private FederationRelyingParty relyingParty() {
        final DefaultFederationRelyingParty rp = new DefaultFederationRelyingParty();

        rp.setRealm("urn:org:apache:cxf:fediz:fedizhelloworld");
        rp.setProtocol("http://docs.oasis-open.org/wsfed/federation/200706");
        rp.setDescription("Fediz Hello World");
        rp.setDescription("This is the hello-world application");
        rp.setTokenType("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0");
        rp.setRole("ApplicationServiceType");

        final List claims = Arrays.asList(
                new DefaultFederationClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname"),
                new DefaultFederationClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress"),
                new DefaultFederationClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname"),
                new DefaultFederationClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role")
        );
        rp.setClaims(claims);
        return rp;
    }
}
