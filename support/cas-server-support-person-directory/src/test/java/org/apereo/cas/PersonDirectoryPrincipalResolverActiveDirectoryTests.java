package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PersonDirectoryPrincipalResolverActiveDirectoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class, properties = {
    "cas.authn.attribute-repository.ldap[0].bind-dn=Administrator@cas.example.org",
    "cas.authn.attribute-repository.ldap[0].bind-credential=" + PersonDirectoryPrincipalResolverActiveDirectoryTests.AD_ADMIN_PASSWORD,
    "cas.authn.attribute-repository.ldap[0].ldap-url=" + PersonDirectoryPrincipalResolverActiveDirectoryTests.AD_LDAP_URL,
    "cas.authn.attribute-repository.ldap[0].use-start-tls=true",
    "cas.authn.attribute-repository.ldap[0].base-dn=dc=cas,dc=example,dc=org",
    "cas.authn.attribute-repository.ldap[0].search-filter=(sAMAccountName={username})",
    "cas.authn.attribute-repository.ldap[0].trust-store=" + PersonDirectoryPrincipalResolverActiveDirectoryTests.AD_TRUST_STORE,
    "cas.authn.attribute-repository.ldap[0].trust-store-type=JKS",
    "cas.authn.attribute-repository.ldap[0].trust-manager=ANY",
    "cas.authn.attribute-repository.ldap[0].trust-store-password=changeit",
    "cas.authn.attribute-repository.ldap[0].attributes.displayName=description",
    "cas.authn.attribute-repository.ldap[0].attributes.objectGUID=objectGUID",
    "cas.authn.attribute-repository.ldap[0].attributes.objectSid=objectSid"
})
@EnabledIfListeningOnPort(port = 10390)
@Tag("ActiveDirectory")
public class PersonDirectoryPrincipalResolverActiveDirectoryTests {
    public static final String AD_TRUST_STORE = "file:/tmp/adcacerts.jks";

    public static final String AD_ADMIN_PASSWORD = "M3110nM3110n#1";

    public static final String AD_LDAP_URL = "ldap://localhost:10390";

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    private IPersonAttributeDao attributeRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(AttributeDefinitionStore.BEAN_NAME)
    private AttributeDefinitionStore attributeDefinitionStore;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    public void verifyResolver() {
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PrincipalFactoryUtils.newPrincipalFactory(),
            this.attributeRepository,
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()),
            servicesManager, attributeDefinitionStore,
            casProperties.getPersonDirectory());
        val p = resolver.resolve(new UsernamePasswordCredential("admin", "P@ssw0rd"),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(p);
        assertTrue(p.getAttributes().containsKey("description"));
        assertTrue(p.getAttributes().containsKey("objectGUID"));
        assertTrue(p.getAttributes().containsKey("objectSid"));
        CollectionUtils.firstElement(p.getAttributes().get("objectGUID"))
            .ifPresent(value -> assertNotNull(EncodingUtils.decodeBase64(value.toString())));
        CollectionUtils.firstElement(p.getAttributes().get("objectSid"))
            .ifPresent(value -> assertNotNull(EncodingUtils.decodeBase64(value.toString())));
    }
}
