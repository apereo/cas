package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.core.authentication.AuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.GroovyAuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.RestAuthenticationPolicyProperties;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@Tag("Simple")
public class CoreAuthenticationUtilsTests {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        .findAndRegisterModules();

    private static void verifySerialization(final Collection<AuthenticationPolicy> policy) throws IOException {
        val file = new File(FileUtils.getTempDirectoryPath(), UUID.randomUUID().toString() + ".json");
        MAPPER.writeValue(file, policy);
        val readPolicy = MAPPER.readValue(file, Collection.class);
        assertEquals(policy, readPolicy);
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
        props.getAllHandlers().setEnabled(true);
        val policy = CoreAuthenticationUtils.newAuthenticationPolicy(props);
        verifySerialization(policy);
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

    public static class PredicateExample implements Predicate<Credential> {
        @Override
        public boolean test(final Credential credential) {
            return true;
        }
    }
}
