package org.apereo.cas.support.pac4j.authentication.clients;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.val;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.profile.converter.AttributeConverter;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import javax.net.ssl.SSLContext;
import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreHttpConfiguration.class
}, properties = {
        "cas.authn.pac4j.saml[0].clientName=foo_client",
        "cas.authn.pac4j.saml[0].identityProviderMetadataPath=file:/idp-metadata.xml",
        "cas.authn.pac4j.saml[0].samlAttributeConverter=org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryTests.FooConverter",
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("SAML")
public class DelegatedClientFactoryTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyBuildSaml2IdentityProviders() throws Exception {

        val mockSslContext = mock(CasSSLContext.class);
        when(mockSslContext.getSslContext()).thenReturn(SSLContext.getDefault());

        val mockCustomizers = (Collection<DelegatedClientFactoryCustomizer>) mock(Collection.class);
        val samlMessageStoreFactory = (ObjectProvider<SAMLMessageStoreFactory> )  mock(ObjectProvider.class);
        val mockCache = (Cache<String, Collection<IndirectClient>>)mock(Cache.class);

        val factory = new DefaultDelegatedClientFactory(casProperties, mockCustomizers, mockSslContext, samlMessageStoreFactory, mockCache);
        Assertions.assertNotNull(factory);

        val saml2clients = factory.buildSaml2IdentityProviders(casProperties);
        Assertions.assertNotNull(saml2clients);
        Assertions.assertEquals(1, saml2clients.size());

        val client = (SAML2Client) saml2clients.stream().findFirst().get();
        Assertions.assertTrue(client.getConfiguration().getSamlAttributeConverter() instanceof FooConverter);
    }

    static class FooConverter implements AttributeConverter {

        @Override
        public Object convert(Object o) {
            return null;
        }
    }
}
