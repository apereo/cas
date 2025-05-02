package org.apereo.cas.authentication;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PersonDirectoryPrincipalResolverLdapTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = BaseLdapAuthenticationHandlerTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.attribute-repository.ldap[0].base-dn=dc=example,dc=org",
        "cas.authn.attribute-repository.ldap[0].ldap-url=ldap://localhost:10389",
        "cas.authn.attribute-repository.ldap[0].search-filter=cn={cnuser}",
        "cas.authn.attribute-repository.ldap[0].attributes.cn=cn",
        "cas.authn.attribute-repository.ldap[0].attributes.description=description",
        "cas.authn.attribute-repository.ldap[0].attributes.entryDN=entryDN",
        "cas.authn.attribute-repository.ldap[0].bind-dn=cn=Directory Manager",
        "cas.authn.attribute-repository.ldap[0].bind-credential=password",
        "cas.authn.attribute-repository.ldap[0].use-all-query-attributes=false",
        "cas.authn.attribute-repository.ldap[0].query-attributes.principal=cnuser",
        "cas.authn.attribute-repository.ldap[0].search-entry-handlers[0].type=DN_ATTRIBUTE_ENTRY",
        "cas.authn.attribute-repository.ldap[0].search-entry-handlers[1].type=MERGE_ENTRIES",
        "cas.authn.attribute-repository.ldap[0].search-entry-handlers[2].type=FOLLOW_SEARCH_REFERRAL",
        "cas.authn.attribute-repository.ldap[0].search-entry-handlers[3].type=FOLLOW_SEARCH_RESULT_REFERENCE",
        "cas.authn.attribute-repository.ldap[0].search-entry-handlers[4].type=ACTIVE_DIRECTORY",
        "cas.authn.attribute-repository.ldap[0].search-entry-handlers[5].type=MERGE_ENTRIES",
        "cas.authn.attribute-repository.ldap[0].search-entry-handlers[6].type=RECURSIVE_ENTRY",
        "cas.authn.attribute-repository.ldap[0].search-entry-handlers[7].type=RANGE_ENTRY",
        "cas.authn.attribute-repository.ldap[0].search-entry-handlers[8].type=PRIMARY_GROUP"
    })
@Tag("LdapAttributes")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 10389)
class PersonDirectoryPrincipalResolverLdapTests {
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

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Test
    void verifyResolver() throws Throwable {
        val attributeMerger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(
            applicationContext, PrincipalFactoryUtils.newPrincipalFactory(),
            this.attributeRepository, attributeMerger, servicesManager,
            attributeDefinitionStore, attributeRepositoryResolver, casProperties.getPersonDirectory());
        val principal = resolver.resolve(new UsernamePasswordCredential("admin", "password"),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("admin")),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
        assertTrue(principal.getAttributes().containsKey("description"));
        assertTrue(principal.getAttributes().containsKey("entryDN"));
    }

    @Test
    void verifyChainedResolver() throws Throwable {
        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(
            applicationContext, PrincipalFactoryUtils.newPrincipalFactory(),
            attributeRepository,
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()),
            servicesManager, attributeDefinitionStore,
            attributeRepositoryResolver, casProperties.getPersonDirectory());
        val chain = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), tenantExtractor,
            List.of(new EchoingPrincipalResolver(), resolver), casProperties);
        val attributes = new HashMap<String, List<Object>>(2);
        attributes.put("a1", List.of("v1"));
        attributes.put("a2", List.of("v2"));
        val resolve = chain.resolve(new UsernamePasswordCredential("admin", "password"),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("admin", attributes)),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(resolve);
        assertTrue(resolve.getAttributes().containsKey("cn"));
        assertTrue(resolve.getAttributes().containsKey("a1"));
        assertTrue(resolve.getAttributes().containsKey("a2"));
    }
}
