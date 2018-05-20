package org.apereo.cas.authentication;

import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * This is {@link CoreAuthenticationUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CoreAuthenticationUtilsTests {

    @Test
    public void verifyMapTransform() {
        final Map results = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMap(CollectionUtils.wrapList("name", "family"));
        assertEquals(2, results.size());
    }

    @Test
    public void verifyCredentialSelectionPredicateNone() {
        final Predicate<Credential> pred = CoreAuthenticationUtils.newCredentialSelectionPredicate(null);
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyCredentialSelectionPredicateGroovy() {
        final Predicate<Credential> pred = CoreAuthenticationUtils.newCredentialSelectionPredicate("classpath:CredentialPredicate.groovy");
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyCredentialSelectionPredicateClazz() {
        final Predicate<Credential> pred = CoreAuthenticationUtils.newCredentialSelectionPredicate(PredicateExample.class.getName());
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyCredentialSelectionPredicateRegex() {
        final Predicate<Credential> pred = CoreAuthenticationUtils.newCredentialSelectionPredicate("\\w.+");
        assertTrue(pred.test(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyPasswordPolicy() {
        final PasswordPolicyProperties properties = new PasswordPolicyProperties();
        assertNotNull(CoreAuthenticationUtils.newPasswordPolicyHandlingStrategy(properties));

        properties.setStrategy(PasswordPolicyProperties.PasswordPolicyHandlingOptions.GROOVY);
        properties.getGroovy().setLocation(new ClassPathResource("passwordpolicy.groovy"));
        assertNotNull(CoreAuthenticationUtils.newPasswordPolicyHandlingStrategy(properties));

        properties.setStrategy(PasswordPolicyProperties.PasswordPolicyHandlingOptions.REJECT_RESULT_CODE);
        assertNotNull(CoreAuthenticationUtils.newPasswordPolicyHandlingStrategy(properties));
    }

    @Test
    public void verifyPrincipalAttributeTransformations() {
        final List<String> list = Stream.of("a1", "a2:newA2", "a1:newA1").collect(Collectors.toList());
        final Multimap<String, Object> result = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(list);
        assertEquals(3, result.size());
        assertTrue(result.containsKey("a2"));
        assertTrue(result.containsKey("a1"));

        final Map<String, Object> map = CollectionUtils.wrap(result);
        final Collection a2 = (Collection) map.get("a2");
        assertEquals(1, a2.size());
        final Collection a1 = (Collection) map.get("a1");
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
