package org.apereo.cas.authentication;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PersonDirectoryPrincipalResolverActiveDirectoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(
    classes = BaseLdapAuthenticationHandlerTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.attribute-repository.ldap[0].bind-dn=Administrator@cas.example.org",
        "cas.authn.attribute-repository.ldap[0].bind-credential=" + PersonDirectoryPrincipalResolverActiveDirectoryTests.AD_ADMIN_PASSWORD,
        "cas.authn.attribute-repository.ldap[0].ldap-url=" + PersonDirectoryPrincipalResolverActiveDirectoryTests.AD_LDAP_URL,
        "cas.authn.attribute-repository.ldap[0].use-start-tls=true",
        "cas.authn.attribute-repository.ldap[0].base-dn=dc=cas,dc=example,dc=org",
        "cas.authn.attribute-repository.ldap[0].search-filter=(sAMAccountName={username})",
        "cas.authn.attribute-repository.ldap[0].trust-store=" + PersonDirectoryPrincipalResolverActiveDirectoryTests.AD_TRUST_STORE,
        "cas.authn.attribute-repository.ldap[0].trust-store-type=JKS",
        "cas.authn.attribute-repository.ldap[0].trust-manager=ANY",
        "cas.authn.attribute-repository.ldap[0].hostname-verifier=ANY",
        "cas.authn.attribute-repository.ldap[0].trust-store-password=changeit",
        "cas.authn.attribute-repository.ldap[0].attributes.displayName=description",
        "cas.authn.attribute-repository.ldap[0].attributes.objectGUID=objectGUID",
        "cas.authn.attribute-repository.ldap[0].attributes.objectSid=objectSid"
    })
@EnabledIfListeningOnPort(port = 10390)
@Tag("ActiveDirectory")
@ExtendWith(CasTestExtension.class)
class PersonDirectoryPrincipalResolverActiveDirectoryTests {
    public static final String AD_TRUST_STORE = "file:${#systemProperties['java.io.tmpdir']}/adcacerts.jks";

    public static final String AD_ADMIN_PASSWORD = "M3110nM3110n#1";

    public static final String AD_LDAP_URL = "ldap://localhost:10390";

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    private PersonAttributeDao attributeRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(AttributeDefinitionStore.BEAN_NAME)
    private AttributeDefinitionStore attributeDefinitionStore;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
    private AttributeRepositoryResolver attributeRepositoryResolver;

    @Test
    void verifyResolver() throws Throwable {
        val attributeMerger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(
            applicationContext, PrincipalFactoryUtils.newPrincipalFactory(),
            attributeRepository, attributeMerger,
            servicesManager, attributeDefinitionStore,
            attributeRepositoryResolver, casProperties.getPersonDirectory());
        val principal = resolver.resolve(new UsernamePasswordCredential("admin", "P@ssw0rd"),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
        assertTrue(principal.getAttributes().containsKey("description"));
        assertTrue(principal.getAttributes().containsKey("objectGUID"));
        assertTrue(principal.getAttributes().containsKey("objectSid"));
        CollectionUtils.firstElement(principal.getAttributes().get("objectGUID"))
            .ifPresent(value -> assertNotNull(EncodingUtils.decodeBase64(value.toString())));
        CollectionUtils.firstElement(principal.getAttributes().get("objectSid"))
            .ifPresent(value -> assertNotNull(EncodingUtils.decodeBase64(value.toString())));
    }
}
