package org.apereo.cas.authentication;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.DefaultPrincipalResolutionExecutionPlan;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogatePrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("Impersonation")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class SurrogatePrincipalResolverTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
    private AttributeRepositoryResolver attributeRepositoryResolver;
    
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
    private RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    private AttributeDefinitionStore attributeDefinitionStore;
    
    @BeforeEach
    void before() {
        this.attributeDefinitionStore = mock(AttributeDefinitionStore.class);
    }

    @Test
    void verifySupports() {
        val context = getPrincipalResolutionContext(StringUtils.EMPTY, CoreAuthenticationTestUtils.getAttributeRepository());
        val surrogatePrincipalBuilder = getBuilder();
        val resolver = new SurrogatePrincipalResolver(context);
        resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
        val upc = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertFalse(resolver.supports(upc));

        val credential = new UsernamePasswordCredential();
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("surrogate"));
        credential.setUsername("username");
        assertTrue(resolver.supports(credential));
    }

    @Test
    void verifyResolverDefault() throws Throwable {
        val context = getPrincipalResolutionContext(StringUtils.EMPTY, CoreAuthenticationTestUtils.getAttributeRepository());
        val surrogatePrincipalBuilder = getBuilder();
        val resolver = new SurrogatePrincipalResolver(context);
        resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val principal = resolver.resolve(credential);
        assertNotNull(principal);
        assertEquals(principal.getId(), credential.getId());
    }

    @Test
    void verifyResolverWithNoAttributes() throws Throwable {
        val context = getPrincipalResolutionContext(StringUtils.EMPTY, mock(PersonAttributeDao.class));
        val surrogatePrincipalBuilder = getBuilder();
        val resolver = new SurrogatePrincipalResolver(context);
        resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
        val credential = new UsernamePasswordCredential();
        val surrogateTrait = new SurrogateCredentialTrait("surrogate");
        credential.getCredentialMetadata().addTrait(surrogateTrait);
        credential.setUsername("test");
        val principal = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal("test")),
            Optional.of(mock(AuthenticationHandler.class)), Optional.of(CoreAuthenticationTestUtils.getService()));
        assertInstanceOf(SurrogatePrincipal.class, principal);
        assertEquals(principal.getId(), surrogateTrait.getSurrogateUsername());
    }

    @Test
    void verifyResolverAttribute() throws Throwable {
        val context = getPrincipalResolutionContext("cn", CoreAuthenticationTestUtils.getAttributeRepository());
        val surrogatePrincipalBuilder = getBuilder();
        val resolver = new SurrogatePrincipalResolver(context);
        resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val principal = resolver.resolve(credential);
        assertNotNull(principal);
        assertEquals("TEST", principal.getId());
    }

    @Test
    void verifyResolverSurrogateWithoutPrincipal() {
        val surrogatePrincipalBuilder = getBuilder();

        val context = getPrincipalResolutionContext("cn", CoreAuthenticationTestUtils.getAttributeRepository());
        val resolver = new SurrogatePrincipalResolver(context);
        resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
        val credential = new UsernamePasswordCredential();
        credential.setUsername("something");
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("something2"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(credential));
    }

    @Test
    void verifyResolverSurrogate() throws Throwable {
        val surrogatePrincipalBuilder = getBuilder();

        val context = getPrincipalResolutionContext(StringUtils.EMPTY, CoreAuthenticationTestUtils.getAttributeRepository());
        val resolver = new SurrogatePrincipalResolver(context);
        resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
        val credential = new UsernamePasswordCredential();
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("surrogate"));
        credential.setUsername("username");
        val principal = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal("casuser")),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
        assertEquals("surrogate", principal.getId());
    }

    @Test
    void verifyPrincipalResolutionPlan() throws Throwable {
        val surrogatePrincipalBuilder = getBuilder();
        val upc = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();

        val surrogateCreds = new UsernamePasswordCredential();
        surrogateCreds.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("surrogate"));
        surrogateCreds.setUsername(upc.getUsername());

        val plan = new DefaultPrincipalResolutionExecutionPlan();

        val context = getPrincipalResolutionContext(StringUtils.EMPTY, CoreAuthenticationTestUtils.getAttributeRepository());
        plan.registerPrincipalResolver(new PersonDirectoryPrincipalResolver(context));
        plan.registerPrincipalResolver(new SurrogatePrincipalResolver(context).setSurrogatePrincipalBuilder(surrogatePrincipalBuilder));

        val resolver = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), tenantExtractor,
            plan.getRegisteredPrincipalResolvers(), casProperties);

        val upcPrincipal = resolver.resolve(upc, Optional.of(CoreAuthenticationTestUtils.getPrincipal("test")),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(upcPrincipal);
        assertEquals(1, upcPrincipal.getAttributes().get("givenName").size());
        assertEquals(upc.getId(), upcPrincipal.getId());

        val surrogatePrincipal = resolver.resolve(surrogateCreds, Optional.of(CoreAuthenticationTestUtils.getPrincipal("casuser")),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(surrogatePrincipal);
        assertEquals(1, surrogatePrincipal.getAttributes().get("givenName").size());
        assertEquals(surrogateCreds.getId(), surrogatePrincipal.getId());
    }

    private SurrogateAuthenticationPrincipalBuilder getBuilder() {
        val surrogateAuthenticationService = new SimpleSurrogateAuthenticationService(
            Map.of("test", List.of("surrogate")),
            servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext);
        return new DefaultSurrogateAuthenticationPrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            surrogateAuthenticationService,
            attributeRepositoryResolver,
            casProperties);
    }

    private PrincipalResolutionContext getPrincipalResolutionContext(
        final String principalAttributes,
        final PersonAttributeDao attributeRepository) {
        return PrincipalResolutionContext.builder()
            .attributeDefinitionStore(attributeDefinitionStore)
            .servicesManager(servicesManager)
            .attributeRepositoryResolver(attributeRepositoryResolver)
            .applicationContext(applicationContext)
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(attributeRepository)
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .principalAttributeNames(principalAttributes)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
    }
}
