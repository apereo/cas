package org.apereo.cas.authentication;

import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogatePrincipalElectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Impersonation")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class)
class SurrogatePrincipalElectionStrategyTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
    private AttributeRepositoryResolver attributeRepositoryResolver;

    @Autowired
    @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
    private RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;
    
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyNominate() throws Throwable {
        val credential = new BasicIdentifiableCredential();
        credential.getCredentialMetadata()
            .addTrait(new SurrogateCredentialTrait("cas-surrogate"));
        val surrogate = buildSurrogatePrincipal(credential,
            CoreAuthenticationTestUtils.getAuthentication("casuser")
        );

        val strategy = new SurrogatePrincipalElectionStrategy();
        val result = strategy.nominate(CollectionUtils.wrapList(CoreAuthenticationTestUtils.getPrincipal("two"), surrogate),
            CoreAuthenticationTestUtils.getAttributes());
        assertEquals(result, surrogate);
    }

    @Test
    void verifyOperation() throws Throwable {
        val strategy = new SurrogatePrincipalElectionStrategy();
        val attributes = CollectionUtils.wrap(
            "formalName", CollectionUtils.wrapSet("cas"),
            "theName", CollectionUtils.wrapSet("user"),
            "sysuser", CollectionUtils.wrapSet("casuser"),
            "firstName", CollectionUtils.wrapSet("cas-first"),
            "lastName", CollectionUtils.wrapSet("cas-last"));

        val authentications = new ArrayList<Authentication>();
        val primaryAuth = CoreAuthenticationTestUtils.getAuthentication("casuser");
        authentications.add(primaryAuth);

        val attributeRepository = CoreAuthenticationTestUtils.getAttributeRepository();
        val credential = new BasicIdentifiableCredential();
        credential.getCredentialMetadata()
            .addTrait(new SurrogateCredentialTrait("cas-surrogate"));
        val surrogatePrincipal = buildSurrogatePrincipal(credential, primaryAuth);

        authentications.add(CoreAuthenticationTestUtils.getAuthentication(surrogatePrincipal));
        val principal = strategy.nominate(authentications, (Map) attributes);
        assertNotNull(principal);
        assertEquals("cas-surrogate", principal.getId());
        assertEquals(6, principal.getAttributes().size());

        val result = attributeRepository.getBackingMap().keySet()
            .stream()
            .filter(key -> !principal.getAttributes().containsKey(key))
            .findAny();
        if (result.isPresent()) {
            fail();
        }
    }

    @Test
    void verifyMultiPrincipalsWithNoAttributes() throws Throwable {
        val strategy = new SurrogatePrincipalElectionStrategy();
        val attributes = CollectionUtils.<String, List<Object>>wrap(
            "primaryName1", CollectionUtils.wrapList("cas"),
            "primaryName2", CollectionUtils.wrapList("user"));

        val principals = new ArrayList<Principal>();
        val primaryPrincipal1 = CoreAuthenticationTestUtils.getPrincipal("primary", new HashMap<>());
        principals.add(primaryPrincipal1);

        val credential = new BasicIdentifiableCredential();
        credential.getCredentialMetadata()
            .addTrait(new SurrogateCredentialTrait("cas-surrogate"));
        val surrogatePrincipal = buildSurrogatePrincipal(credential,
            CoreAuthenticationTestUtils.getAuthentication(primaryPrincipal1));
        principals.add(surrogatePrincipal);

        val primaryPrincipal2 = CoreAuthenticationTestUtils.getPrincipal("primary", attributes);
        principals.add(primaryPrincipal2);

        val principal = (SurrogatePrincipal) strategy.nominate(principals, Map.of());
        assertNotNull(principal);
        assertEquals("cas-surrogate", principal.getId());
        assertEquals(6, principal.getAttributes().size());
        assertEquals("primary", principal.getPrimary().getId());
        assertEquals(attributes, principal.getPrimary().getAttributes());
    }

    @Test
    void verifyMultiPrincipalsWithoutSurrogate() throws Throwable {
        val strategy = new SurrogatePrincipalElectionStrategy();
        val attributes1 = CollectionUtils.<String, List<Object>>wrap(
            "name", CollectionUtils.wrapList("cas"),
            "lastname", CollectionUtils.wrapList("apereo"));
        val attributes2 = CollectionUtils.<String, List<Object>>wrap(
            "color", CollectionUtils.wrapList("blue"),
            "city", CollectionUtils.wrapList("london"));

        val primaryPrincipal1 = CoreAuthenticationTestUtils.getPrincipal("primary", attributes1);
        val primaryPrincipal2 = CoreAuthenticationTestUtils.getPrincipal("primary", attributes2);
        val principalAttributes = CoreAuthenticationUtils.mergeAttributes(attributes1, attributes2);
        val principal = strategy.nominate(List.of(
            RegisteredServiceTestUtils.getAuthentication(primaryPrincipal1),
            RegisteredServiceTestUtils.getAuthentication(primaryPrincipal2)), principalAttributes);
        assertNotNull(principal);
        assertEquals(principalAttributes, principal.getAttributes());
    }

    private Principal buildSurrogatePrincipal(final Credential surrogateId, final Authentication primaryAuth) throws Throwable {
        val surrogatePrincipalBuilder = getBuilder();
        return surrogatePrincipalBuilder.buildSurrogatePrincipal(surrogateId,
            primaryAuth.getPrincipal(),
            RegisteredServiceTestUtils.getRegisteredService());
    }

    private SurrogateAuthenticationPrincipalBuilder getBuilder() {
        val surrogateAuthenticationService = new SimpleSurrogateAuthenticationService(
            Map.of("test", List.of("surrogate")), servicesManager, casProperties,
            principalAccessStrategyEnforcer, applicationContext);
        return new DefaultSurrogateAuthenticationPrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(),
            CoreAuthenticationTestUtils.getAttributeRepository(),
            surrogateAuthenticationService,
            attributeRepositoryResolver,
            casProperties);
    }
}
