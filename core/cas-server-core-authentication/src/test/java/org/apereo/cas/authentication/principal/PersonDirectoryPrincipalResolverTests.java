package org.apereo.cas.authentication.principal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.EchoingPrincipalResolver;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link PersonDirectoryPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class PersonDirectoryPrincipalResolverTests {

    @Test
    public void verifyNullPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        final Principal p = resolver.resolve(() -> null, CoreAuthenticationTestUtils.getPrincipal());
        assertNull(p);

    }

    @Test
    public void verifyNullAttributes() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setReturnNullIfNoAttributes(true);
        resolver.setPrincipalAttributeName(CoreAuthenticationTestUtils.CONST_USERNAME);
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c, null);
        assertNull(p);
    }

    @Test
    public void verifyNoAttributesWithPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setPrincipalAttributeName(CoreAuthenticationTestUtils.CONST_USERNAME);
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c, null);
        assertNotNull(p);
    }

    @Test
    public void verifyAttributesWithPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setAttributeRepository(CoreAuthenticationTestUtils.getAttributeRepository());
        resolver.setPrincipalAttributeName("cn");
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c, null);
        assertNotNull(p);
        assertNotEquals(p.getId(), CoreAuthenticationTestUtils.CONST_USERNAME);
        assertTrue(p.getAttributes().containsKey("memberOf"));
    }

    @Test
    public void verifyChainingResolverOverwrite() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setAttributeRepository(CoreAuthenticationTestUtils.getAttributeRepository());

        final ChainingPrincipalResolver chain = new ChainingPrincipalResolver();
        chain.setChain(Lists.newArrayList(resolver, new EchoingPrincipalResolver()));
        final Principal p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME,
                        ImmutableMap.of("cn", "changedCN", "attr1", "value1")));
        assertEquals(p.getAttributes().size(),
                CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames().size() + 1);
        assertTrue(p.getAttributes().containsKey("attr1"));
        assertTrue(p.getAttributes().containsKey("cn"));
        assertTrue(CollectionUtils.toCollection(p.getAttributes().get("cn")).contains("changedCN"));
    }

    @Test
    public void verifyChainingResolver() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setAttributeRepository(CoreAuthenticationTestUtils.getAttributeRepository());

        final ChainingPrincipalResolver chain = new ChainingPrincipalResolver();
        chain.setChain(Lists.newArrayList(resolver, new EchoingPrincipalResolver()));
        final Principal p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, ImmutableMap.of("attr1", "value")));
        assertEquals(p.getAttributes().size(),
                CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames().size() + 1);
        assertTrue(p.getAttributes().containsKey("attr1"));
    }

    @Test(expected = PrincipalException.class)
    public void verifyChainingResolverDistinct() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setAttributeRepository(CoreAuthenticationTestUtils.getAttributeRepository());

        final ChainingPrincipalResolver chain = new ChainingPrincipalResolver();
        chain.setChain(Lists.newArrayList(resolver, new EchoingPrincipalResolver()));
        chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                CoreAuthenticationTestUtils.getPrincipal("somethingelse", ImmutableMap.of("attr1", "value")));
    }
}
