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
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class SurrogatePrincipalResolverTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Mock
    private ServicesManager servicesManager;

    @Mock
    private AttributeDefinitionStore attributeDefinitionStore;

    @Mock
    private AttributeRepositoryResolver attributeRepositoryResolver;

    @BeforeEach
    public void before() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    void verifySupports() throws Throwable {
        val context = getPrincipalResolutionContext(StringUtils.EMPTY, CoreAuthenticationTestUtils.getAttributeRepository());
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
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
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val resolver = new SurrogatePrincipalResolver(context);
        resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(credential);
        assertNotNull(p);
        assertEquals(p.getId(), credential.getId());
    }

    @Test
    void verifyResolverWithNoAttributes() throws Throwable {
        val context = getPrincipalResolutionContext(StringUtils.EMPTY, mock(PersonAttributeDao.class));
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            context.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val resolver = new SurrogatePrincipalResolver(context);
        resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
        val credential = new UsernamePasswordCredential();
        val surrogateTrait = new SurrogateCredentialTrait("surrogate");
        credential.getCredentialMetadata().addTrait(surrogateTrait);
        credential.setUsername("test");
        val p = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal("test")),
            Optional.of(mock(AuthenticationHandler.class)), Optional.of(CoreAuthenticationTestUtils.getService()));
        assertInstanceOf(SurrogatePrincipal.class, p);
        assertEquals(p.getId(), surrogateTrait.getSurrogateUsername());
    }

    @Test
    void verifyResolverAttribute() throws Throwable {
        val context = getPrincipalResolutionContext("cn", CoreAuthenticationTestUtils.getAttributeRepository());
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val resolver = new SurrogatePrincipalResolver(context);
        resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(credential);
        assertNotNull(p);
        assertEquals("TEST", p.getId());
    }

    @Test
    void verifyResolverSurrogateWithoutPrincipal() throws Throwable {
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));

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
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));

        val context = getPrincipalResolutionContext(StringUtils.EMPTY, CoreAuthenticationTestUtils.getAttributeRepository());
        val resolver = new SurrogatePrincipalResolver(context);
        resolver.setSurrogatePrincipalBuilder(surrogatePrincipalBuilder);
        val credential = new UsernamePasswordCredential();
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("surrogate"));
        credential.setUsername("username");
        val p = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal("casuser")),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(p);
        assertEquals("surrogate", p.getId());
    }

    @Test
    void verifyPrincipalResolutionPlan() throws Throwable {
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")), mock(ServicesManager.class)));
        val upc = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();

        val surrogateCreds = new UsernamePasswordCredential();
        surrogateCreds.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("surrogate"));
        surrogateCreds.setUsername(upc.getUsername());

        val plan = new DefaultPrincipalResolutionExecutionPlan();

        val context = getPrincipalResolutionContext(StringUtils.EMPTY, CoreAuthenticationTestUtils.getAttributeRepository());
        plan.registerPrincipalResolver(new PersonDirectoryPrincipalResolver(context));
        plan.registerPrincipalResolver(new SurrogatePrincipalResolver(context).setSurrogatePrincipalBuilder(surrogatePrincipalBuilder));

        val resolver = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), casProperties);
        resolver.setChain(plan.getRegisteredPrincipalResolvers());

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
