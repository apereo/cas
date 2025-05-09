package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.GroovyAuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.configuration.model.core.authentication.RestAuthenticationPolicyProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
class CoreAuthenticationUtilsTests {
    private static final String PROPERTY = "property";

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static void verifySerialization(final Collection<AuthenticationPolicy> policy) throws IOException {
        val file = new File(FileUtils.getTempDirectoryPath(), UUID.randomUUID() + ".json");
        MAPPER.writeValue(file, policy);
        val readPolicy = MAPPER.readValue(file, Collection.class);
        assertEquals(policy, readPolicy);
    }

    @Test
    void verifyAuthnPolicyRequiredAttrs() throws Throwable {
        val props = new AuthenticationPolicyProperties();
        props.getRequiredAttributes().setEnabled(true);
        props.getRequiredAttributes().setAttributes(Map.of("hello", "world"));
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    void verifyAuthnPolicyRequired() throws Throwable {
        val props = new AuthenticationPolicyProperties();
        props.getReq().setEnabled(true);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    void verifyAuthnPolicyAllHandlers() throws Throwable {
        val props = new AuthenticationPolicyProperties();
        props.getAllHandlers().setEnabled(true);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    void verifyAuthnPolicyAll() throws Throwable {
        val props = new AuthenticationPolicyProperties();
        props.getAll().setEnabled(true);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    void verifyNoAuthPolicy() {
        val props = new AuthenticationPolicyProperties();
        props.getAny().setEnabled(false);
        props.getNotPrevented().setEnabled(false);
        assertTrue(CoreAuthenticationUtils.newAuthenticationPolicy(props).isEmpty());
    }

    @Test
    void verifyAuthnPolicyNotPrevented() throws Throwable {
        val props = new AuthenticationPolicyProperties();
        props.getNotPrevented().setEnabled(true);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    void verifyAuthnPolicyGroovy() throws Throwable {
        val props = new AuthenticationPolicyProperties();
        props.getGroovy()
            .add(new GroovyAuthenticationPolicyProperties().setScript("classpath:example.groovy"));
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    void verifyAuthnPolicyRest() throws Throwable {
        val props = new AuthenticationPolicyProperties();
        val rest = new RestAuthenticationPolicyProperties();
        rest.setUrl("http://example.org");
        props.getRest().add(rest);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    void verifyAuthnPolicyAny() throws Throwable {
        val props = new AuthenticationPolicyProperties();
        props.getAny().setEnabled(true);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
    }

    @Test
    void verifyMapTransform() {
        val results = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMap(CollectionUtils.wrapList("name", "family"));
        assertEquals(2, results.size());
    }

    @Test
    void verifyCredentialSelectionPredicateNone() {
        val pred = CoreAuthenticationUtils.newCredentialSelectionPredicate(null);
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    void verifyCredentialSelectionPredicateGroovy() {
        val pred = CoreAuthenticationUtils.newCredentialSelectionPredicate("classpath:CredentialPredicate.groovy");
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    void verifyCredentialSelectionPredicateClazz() {
        val pred = CoreAuthenticationUtils.newCredentialSelectionPredicate(PredicateExample.class.getName());
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    void verifyCredentialSelectionPredicateRegex() {
        val pred = CoreAuthenticationUtils.newCredentialSelectionPredicate("\\w.+");
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    void verifyPasswordPolicy() {
        val properties = new PasswordPolicyProperties();
        assertNotNull(CoreAuthenticationUtils.newPasswordPolicyHandlingStrategy(properties, mock(ApplicationContext.class)));

        properties.setStrategy(PasswordPolicyProperties.PasswordPolicyHandlingOptions.GROOVY);
        properties.getGroovy().setLocation(new ClassPathResource("passwordpolicy.groovy"));
        assertNotNull(CoreAuthenticationUtils.newPasswordPolicyHandlingStrategy(properties, mock(ApplicationContext.class)));

        properties.setStrategy(PasswordPolicyProperties.PasswordPolicyHandlingOptions.REJECT_RESULT_CODE);
        assertNotNull(CoreAuthenticationUtils.newPasswordPolicyHandlingStrategy(properties, mock(ApplicationContext.class)));
    }

    @Test
    void verifyAttributeMerger() {
        val merger = CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
        val m1 = CollectionUtils.<String, List<Object>>wrap("key", CollectionUtils.wrapList("value1"));
        val m2 = CollectionUtils.<String, List<Object>>wrap("key", CollectionUtils.wrapList("value2"));
        val result = merger.mergeAttributes(m1, m2);
        assertEquals(1, result.size());
        assertEquals(2, result.get("key").size());
    }

    @Test
    void verifyAttributeMergerOriginal() {
        val merger = CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.SOURCE);
        val m1 = CollectionUtils.<String, List<Object>>wrap("key1", CollectionUtils.wrapList("value1"));
        val m2 = CollectionUtils.<String, List<Object>>wrap("key2", CollectionUtils.wrapList("value2"));
        val result = merger.mergeAttributes(m1, m2);
        assertEquals(m1, result);
    }

    @Test
    void verifyAttributeMergerChanged() {
        val merger = CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.DESTINATION);
        val m1 = CollectionUtils.<String, List<Object>>wrap("key1", CollectionUtils.wrapList("value1"));
        val m2 = CollectionUtils.<String, List<Object>>wrap("key2", CollectionUtils.wrapList("value2"));
        val result = merger.mergeAttributes(m1, m2);
        assertEquals(m2, result);
    }

    @Test
    void verifyIpIntelligenceService() {
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
    void verifyPrincipalAttributeTransformations() {
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
    void verifyPrincipalConflictResolution() {
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

    static class PredicateExample implements Predicate<Credential> {
        @Override
        public boolean test(final Credential credential) {
            return true;
        }
    }

    @Test
    void verifyConvertAttributeValues() {
        var originalMap = CollectionUtils.<String, Object>wrap(PROPERTY, Boolean.FALSE);
        var newMap = CoreAuthenticationUtils.convertAttributeValuesToObjects(originalMap);
        assertEquals(Boolean.FALSE, newMap.get(PROPERTY));

        originalMap = CollectionUtils.wrap(PROPERTY, List.of(Boolean.FALSE));
        newMap = CoreAuthenticationUtils.convertAttributeValuesToObjects(originalMap);
        assertEquals(Boolean.FALSE, newMap.get(PROPERTY));

        val mapValue = new HashMap<>();
        mapValue.put("key", "value");
        originalMap = CollectionUtils.wrap(PROPERTY, List.of(mapValue));
        newMap = CoreAuthenticationUtils.convertAttributeValuesToObjects(originalMap);
        assertTrue(newMap.get(PROPERTY) instanceof Collection);
        assertEquals(mapValue, ((Collection<?>) newMap.get(PROPERTY)).iterator().next());
    }
}
