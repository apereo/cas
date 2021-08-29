package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.GroovyAuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.configuration.model.core.authentication.RestAuthenticationPolicyProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CoreAuthenticationUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Utility")
public class CoreAuthenticationUtilsTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static void verifySerialization(final Collection<AuthenticationPolicy> policy) throws IOException {
        val file = new File(FileUtils.getTempDirectoryPath(), UUID.randomUUID().toString() + ".json");
        MAPPER.writeValue(file, policy);
        val readPolicy = MAPPER.readValue(file, Collection.class);
        assertEquals(policy, readPolicy);
    }

    @Test
    public void verifyAttributeRepositories() {
        val repository = CoreAuthenticationTestUtils.getAttributeRepository();
        val attrs = CoreAuthenticationUtils.retrieveAttributesFromAttributeRepository(repository, "casuser",
            Set.of("StubAttributeRepository"), Optional.of(CoreAuthenticationTestUtils.getPrincipal("casuser")));
        assertTrue(attrs.containsKey("uid"));
        assertTrue(attrs.containsKey("mail"));
        assertTrue(attrs.containsKey("memberOf"));
    }

    @Test
    public void verifyAttributeRepositoriesByFilter() {
        val repository = new StubPersonAttributeDao(CoreAuthenticationTestUtils.getAttributes()) {
            @Override
            public IPersonAttributes getPerson(final String uid, final IPersonAttributeDaoFilter filter) {
                if (filter.choosePersonAttributeDao(this)) {
                    return super.getPerson(uid, filter);
                }
                return null;
            }
        };
        var attrs = CoreAuthenticationUtils.retrieveAttributesFromAttributeRepository(repository, "casuser",
            Set.of("*"), Optional.of(CoreAuthenticationTestUtils.getPrincipal("casuser")));
        assertTrue(attrs.containsKey("uid"));
        assertTrue(attrs.containsKey("mail"));
        assertTrue(attrs.containsKey("memberOf"));

        attrs = CoreAuthenticationUtils.retrieveAttributesFromAttributeRepository(repository, "casuser",
            Set.of("Invalid"), Optional.of(CoreAuthenticationTestUtils.getPrincipal("casuser")));
        assertTrue(attrs.isEmpty());
    }

    @Test
    public void verifyAuthnPolicyRequired() throws Exception {
        val props = new AuthenticationPolicyProperties();
        props.getReq().setEnabled(true);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    public void verifyAuthnPolicyAllHandlers() throws Exception {
        val props = new AuthenticationPolicyProperties();
        props.getAllHandlers().setEnabled(true);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    public void verifyAuthnPolicyAll() throws Exception {
        val props = new AuthenticationPolicyProperties();
        props.getAll().setEnabled(true);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    public void verifyNoAuthPolicy() {
        val props = new AuthenticationPolicyProperties();
        props.getAny().setEnabled(false);
        props.getNotPrevented().setEnabled(false);
        assertTrue(CoreAuthenticationUtils.newAuthenticationPolicy(props).isEmpty());
    }

    @Test
    public void verifyAuthnPolicyNotPrevented() throws Exception {
        val props = new AuthenticationPolicyProperties();
        props.getNotPrevented().setEnabled(true);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    public void verifyAuthnPolicyGroovy() throws Exception {
        val props = new AuthenticationPolicyProperties();
        props.getGroovy()
            .add(new GroovyAuthenticationPolicyProperties().setScript("classpath:example.groovy"));
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    public void verifyAuthnPolicyRest() throws Exception {
        val props = new AuthenticationPolicyProperties();
        val rest = new RestAuthenticationPolicyProperties();
        rest.setUrl("http://example.org");
        props.getRest().add(rest);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    public void verifyAuthnPolicyAny() throws Exception {
        val props = new AuthenticationPolicyProperties();
        props.getAny().setEnabled(true);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    public void verifyMapTransform() {
        val results = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMap(CollectionUtils.wrapList("name", "family"));
        assertEquals(2, results.size());
    }

    @Test
    public void verifyCredentialSelectionPredicateNone() {
        val pred = CoreAuthenticationUtils.newCredentialSelectionPredicate(null);
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyCredentialSelectionPredicateGroovy() {
        val pred = CoreAuthenticationUtils.newCredentialSelectionPredicate("classpath:CredentialPredicate.groovy");
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyCredentialSelectionPredicateClazz() {
        val pred = CoreAuthenticationUtils.newCredentialSelectionPredicate(PredicateExample.class.getName());
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyCredentialSelectionPredicateRegex() {
        val pred = CoreAuthenticationUtils.newCredentialSelectionPredicate("\\w.+");
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyPasswordPolicy() {
        val properties = new PasswordPolicyProperties();
        assertNotNull(CoreAuthenticationUtils.newPasswordPolicyHandlingStrategy(properties, mock(ApplicationContext.class)));

        properties.setStrategy(PasswordPolicyProperties.PasswordPolicyHandlingOptions.GROOVY);
        properties.getGroovy().setLocation(new ClassPathResource("passwordpolicy.groovy"));
        assertNotNull(CoreAuthenticationUtils.newPasswordPolicyHandlingStrategy(properties, mock(ApplicationContext.class)));

        properties.setStrategy(PasswordPolicyProperties.PasswordPolicyHandlingOptions.REJECT_RESULT_CODE);
        assertNotNull(CoreAuthenticationUtils.newPasswordPolicyHandlingStrategy(properties, mock(ApplicationContext.class)));
    }

    @Test
    public void verifyAttributeMerger() {
        val merger = CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
        val m1 = CollectionUtils.<String, List<Object>>wrap("key", CollectionUtils.wrapList("value1"));
        val m2 = CollectionUtils.<String, List<Object>>wrap("key", CollectionUtils.wrapList("value2"));
        val result = merger.mergeAttributes(m1, m2);
        assertEquals(1, result.size());
        assertEquals(2, result.get("key").size());
    }

    @Test
    public void verifyIpIntelligenceService() {
        var properties = new AdaptiveAuthenticationProperties();
        assertNotNull(CoreAuthenticationUtils.newIpAddressIntelligenceService(properties));

        properties = new AdaptiveAuthenticationProperties();
        properties.getIpIntel().getRest().setUrl("http://localhost:1234");
        assertNotNull(CoreAuthenticationUtils.newIpAddressIntelligenceService(properties));

        properties = new AdaptiveAuthenticationProperties();
        properties.getIpIntel().getGroovy().setLocation(new ClassPathResource("GroovyIPService.groovy"));
        assertNotNull(CoreAuthenticationUtils.newIpAddressIntelligenceService(properties));

        properties = new AdaptiveAuthenticationProperties();
        properties.getIpIntel().getBlackDot().setEmailAddress("cas@example.org");
        assertNotNull(CoreAuthenticationUtils.newIpAddressIntelligenceService(properties));
    }

    @Test
    public void verifyPrincipalAttributeTransformations() {
        val list = Stream.of("a1", "a2:newA2", "a1:newA1").collect(Collectors.toList());
        val result = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(list);
        assertEquals(3, result.size());
        assertTrue(result.containsKey("a2"));
        assertTrue(result.containsKey("a1"));

        val map = CollectionUtils.wrap(result);
        val a2 = (Collection) map.get("a2");
        assertEquals(1, a2.size());
        val a1 = (Collection) map.get("a1");
        assertEquals(2, a1.size());
        assertTrue(a2.contains("newA2"));
        assertTrue(a1.contains("a1"));
        assertTrue(a1.contains("newA1"));
    }

    @Test
    public void verifyPrincipalConflictResolution() {
        val r1 = CoreAuthenticationUtils.newPrincipalElectionStrategyConflictResolver(
            new PersonDirectoryPrincipalResolverProperties().setPrincipalResolutionConflictStrategy("LAST"));
        assertNotNull(r1);

        val r2 = CoreAuthenticationUtils.newPrincipalElectionStrategyConflictResolver(
            new PersonDirectoryPrincipalResolverProperties().setPrincipalResolutionConflictStrategy("FIRST"));
        assertNotNull(r2);

        val r3 = CoreAuthenticationUtils.newPrincipalElectionStrategyConflictResolver(
            new PersonDirectoryPrincipalResolverProperties().setPrincipalResolutionConflictStrategy("INVALID"));
        assertEquals(r3, r1);
    }

    public static class PredicateExample implements Predicate<Credential> {
        @Override
        public boolean test(final Credential credential) {
            return true;
        }
    }

    @Test
    public void verifyPersonDirectoryOverrides() {
        val principal = new PersonDirectoryPrincipalResolverProperties();
        val personDirectory = new PersonDirectoryPrincipalResolverProperties();
        val principalResolutionContext = CoreAuthenticationUtils.buildPrincipalResolutionContext(
            PrincipalFactoryUtils.newPrincipalFactory(),
            new StubPersonAttributeDao(Collections.EMPTY_MAP),
            CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD),
            principal, personDirectory);
        assertFalse(principalResolutionContext.isUseCurrentPrincipalId());
        assertTrue(principalResolutionContext.isResolveAttributes());
        assertFalse(principalResolutionContext.isReturnNullIfNoAttributes());
        assertTrue(principalResolutionContext.getActiveAttributeRepositoryIdentifiers().isEmpty());
        assertTrue(principalResolutionContext.getPrincipalAttributeNames().isEmpty());

        personDirectory.setUseExistingPrincipalId(TriStateBoolean.TRUE);
        personDirectory.setAttributeResolutionEnabled(TriStateBoolean.TRUE);
        personDirectory.setReturnNull(TriStateBoolean.TRUE);
        personDirectory.setAttributeResolutionEnabled(TriStateBoolean.FALSE);
        personDirectory.setActiveAttributeRepositoryIds("test1,test2");
        personDirectory.setPrincipalAttribute("principalAttribute");
        val principalResolutionContext2 = CoreAuthenticationUtils.buildPrincipalResolutionContext(
            PrincipalFactoryUtils.newPrincipalFactory(),
            new StubPersonAttributeDao(Collections.EMPTY_MAP),
            CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD),
            principal, personDirectory);
        assertTrue(principalResolutionContext2.isUseCurrentPrincipalId());
        assertFalse(principalResolutionContext2.isResolveAttributes());
        assertTrue(principalResolutionContext2.isReturnNullIfNoAttributes());
        assertEquals(2, principalResolutionContext2.getActiveAttributeRepositoryIdentifiers().size());
        assertEquals("principalAttribute", principalResolutionContext2.getPrincipalAttributeNames());

        principal.setUseExistingPrincipalId(TriStateBoolean.FALSE);
        principal.setAttributeResolutionEnabled(TriStateBoolean.FALSE);
        principal.setReturnNull(TriStateBoolean.FALSE);
        principal.setAttributeResolutionEnabled(TriStateBoolean.TRUE);
        principal.setActiveAttributeRepositoryIds("test1,test2,test3");
        principal.setPrincipalAttribute("principalAttribute2");
        val principalResolutionContext3 = CoreAuthenticationUtils.buildPrincipalResolutionContext(
            PrincipalFactoryUtils.newPrincipalFactory(),
            new StubPersonAttributeDao(Collections.EMPTY_MAP),
            CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD),
            principal, personDirectory);
        assertFalse(principalResolutionContext3.isUseCurrentPrincipalId());
        assertTrue(principalResolutionContext3.isResolveAttributes());
        assertFalse(principalResolutionContext3.isReturnNullIfNoAttributes());
        assertEquals(3, principalResolutionContext3.getActiveAttributeRepositoryIdentifiers().size());
        assertEquals("principalAttribute2", principalResolutionContext3.getPrincipalAttributeNames());

        val principalResolutionContext4 = CoreAuthenticationUtils.buildPrincipalResolutionContext(
            PrincipalFactoryUtils.newPrincipalFactory(),
            new StubPersonAttributeDao(Collections.EMPTY_MAP),
            CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD),
            personDirectory);
        assertTrue(principalResolutionContext4.isUseCurrentPrincipalId());
        assertFalse(principalResolutionContext4.isResolveAttributes());
        assertTrue(principalResolutionContext4.isReturnNullIfNoAttributes());
        assertEquals(2, principalResolutionContext4.getActiveAttributeRepositoryIdentifiers().size());
        assertEquals("principalAttribute", principalResolutionContext4.getPrincipalAttributeNames());
    }
}
