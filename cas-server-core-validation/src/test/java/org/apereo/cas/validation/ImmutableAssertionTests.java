package org.apereo.cas.validation;

import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.Authentication;
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
public class ImmutableAssertionTests {

    @Test
    public void verifyGettersForChainedPrincipals() {
        final List<Authentication> list = new ArrayList<>();

        list.add(org.apereo.cas.authentication.TestUtils.getAuthentication("test"));
        list.add(TestUtils.getAuthentication("test1"));
        list.add(TestUtils.getAuthentication("test2"));

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, org.apereo.cas.services.TestUtils.getService(), true);

        assertEquals(list.toArray(new Authentication[0]).length, assertion.getChainedAuthentications().size());
    }

    @Test
    public void verifyGetterFalseForNewLogin() {
        final List<Authentication> list = new ArrayList<>();

        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, org.apereo.cas.services.TestUtils.getService(), false);

        assertFalse(assertion.isFromNewLogin());
    }

    @Test
    public void verifyGetterTrueForNewLogin() {
        final List<Authentication> list = new ArrayList<>();

        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, org.apereo.cas.services.TestUtils.getService(), true);

        assertTrue(assertion.isFromNewLogin());
    }

    @Test
    public void verifyEqualsWithNull() {
        final List<Authentication> list = new ArrayList<>();
        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, org.apereo.cas.services.TestUtils.getService(), true);

        assertNotEquals(assertion, null);
    }

    @Test
    public void verifyEqualsWithInvalidObject() {
        final List<Authentication> list = new ArrayList<>();
        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, org.apereo.cas.services.TestUtils.getService(), true);

        assertFalse("test".equals(assertion));
    }

    @Test
    public void verifyEqualsWithValidObject() {
        final List<Authentication> list1 = new ArrayList<>();
        final List<Authentication> list2 = new ArrayList<>();

        final Authentication auth = TestUtils.getAuthentication();
        list1.add(auth);
        list2.add(auth);

        final ImmutableAssertion assertion1 = new ImmutableAssertion(auth, list1, org.apereo.cas.services.TestUtils.getService(), true);
        final ImmutableAssertion assertion2 = new ImmutableAssertion(auth, list2, org.apereo.cas.services.TestUtils.getService(), true);

        assertTrue(assertion1.equals(assertion2));
    }

    @Test
    public void verifyGetService() {
        final Service service = org.apereo.cas.services.TestUtils.getService();

        final List<Authentication> list = new ArrayList<>();
        list.add(TestUtils.getAuthentication());

        final Assertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, service, false);

        assertEquals(service, assertion.getService());
    }
}
