package org.apereo.cas.validation;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test for {@link ImmutableAssertion} class.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public class ImmutableAssertionTests {

    @Test
    public void verifyGettersForChainedPrincipals() {
        final List<Authentication> list = new ArrayList<>();

        list.add(CoreAuthenticationTestUtils.getAuthentication("test"));
        list.add(CoreAuthenticationTestUtils.getAuthentication("test1"));
        list.add(CoreAuthenticationTestUtils.getAuthentication("test2"));

        final ImmutableAssertion assertion = new ImmutableAssertion(
                CoreAuthenticationTestUtils.getAuthentication(), list, true, RegisteredServiceTestUtils.getService());

        assertEquals(list.toArray(new Authentication[0]).length, assertion.getChainedAuthentications().size());
    }

    @Test
    public void verifyGetterFalseForNewLogin() {
        final List<Authentication> list = new ArrayList<>();

        list.add(CoreAuthenticationTestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                CoreAuthenticationTestUtils.getAuthentication(), list, false, RegisteredServiceTestUtils.getService());

        assertFalse(assertion.isFromNewLogin());
    }

    @Test
    public void verifyGetterTrueForNewLogin() {
        final List<Authentication> list = new ArrayList<>();

        list.add(CoreAuthenticationTestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                CoreAuthenticationTestUtils.getAuthentication(), list, true, RegisteredServiceTestUtils.getService());

        assertTrue(assertion.isFromNewLogin());
    }

    @Test
    public void verifyEqualsWithNull() {
        final List<Authentication> list = new ArrayList<>();
        list.add(CoreAuthenticationTestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                CoreAuthenticationTestUtils.getAuthentication(), list, true, RegisteredServiceTestUtils.getService());

        assertNotEquals(assertion, null);
    }

    @Test
    public void verifyEqualsWithInvalidObject() {
        final List<Authentication> list = new ArrayList<>();
        list.add(CoreAuthenticationTestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                CoreAuthenticationTestUtils.getAuthentication(), list, true, RegisteredServiceTestUtils.getService());

        assertFalse("test".equals(assertion));
    }

    @Test
    public void verifyEqualsWithValidObject() {
        final List<Authentication> list1 = new ArrayList<>();
        final List<Authentication> list2 = new ArrayList<>();

        final Authentication auth = CoreAuthenticationTestUtils.getAuthentication();
        list1.add(auth);
        list2.add(auth);

        final ImmutableAssertion assertion1 = new ImmutableAssertion(auth, list1, true, RegisteredServiceTestUtils.getService());
        final ImmutableAssertion assertion2 = new ImmutableAssertion(auth, list2, true, RegisteredServiceTestUtils.getService());

        assertTrue(assertion1.equals(assertion2));
    }

    @Test
    public void verifyGetService() {
        final Service service = RegisteredServiceTestUtils.getService();

        final List<Authentication> list = new ArrayList<>();
        list.add(CoreAuthenticationTestUtils.getAuthentication());

        final Assertion assertion = new ImmutableAssertion(
                CoreAuthenticationTestUtils.getAuthentication(), list, false, service);

        assertEquals(service, assertion.getService());
    }
}
