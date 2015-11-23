package org.jasig.cas.validation;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test for {@link org.jasig.cas.validation.ImmutableAssertion} class.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ImmutableAssertionTests {

    @Test
    public void verifyGettersForChainedPrincipals() {
        final List<Authentication> list = new ArrayList<>();

        list.add(org.jasig.cas.authentication.TestUtils.getAuthentication("test"));
        list.add(org.jasig.cas.authentication.TestUtils.getAuthentication("test1"));
        list.add(org.jasig.cas.authentication.TestUtils.getAuthentication("test2"));

        final ImmutableAssertion assertion = new ImmutableAssertion(
                org.jasig.cas.authentication.TestUtils.getAuthentication(), list, org.jasig.cas.services.TestUtils.getService(), true);

        assertEquals(list.toArray(new Authentication[0]).length, assertion.getChainedAuthentications().size());
    }

    @Test
    public void verifyGetterFalseForNewLogin() {
        final List<Authentication> list = new ArrayList<>();

        list.add(org.jasig.cas.authentication.TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                org.jasig.cas.authentication.TestUtils.getAuthentication(), list, org.jasig.cas.services.TestUtils.getService(), false);

        assertFalse(assertion.isFromNewLogin());
    }

    @Test
    public void verifyGetterTrueForNewLogin() {
        final List<Authentication> list = new ArrayList<>();

        list.add(org.jasig.cas.authentication.TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                org.jasig.cas.authentication.TestUtils.getAuthentication(), list, org.jasig.cas.services.TestUtils.getService(), true);

        assertTrue(assertion.isFromNewLogin());
    }

    @Test
    public void verifyEqualsWithNull() {
        final List<Authentication> list = new ArrayList<>();
        list.add(org.jasig.cas.authentication.TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                org.jasig.cas.authentication.TestUtils.getAuthentication(), list, org.jasig.cas.services.TestUtils.getService(), true);

        assertNotEquals(assertion, null);
    }

    @Test
    public void verifyEqualsWithInvalidObject() {
        final List<Authentication> list = new ArrayList<>();
        list.add(org.jasig.cas.authentication.TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                org.jasig.cas.authentication.TestUtils.getAuthentication(), list, org.jasig.cas.services.TestUtils.getService(), true);

        assertFalse("test".equals(assertion));
    }

    @Test
    public void verifyEqualsWithValidObject() {
        final List<Authentication> list1 = new ArrayList<>();
        final List<Authentication> list2 = new ArrayList<>();

        final Authentication auth = org.jasig.cas.authentication.TestUtils.getAuthentication();
        list1.add(auth);
        list2.add(auth);

        final ImmutableAssertion assertion1 = new ImmutableAssertion(auth, list1, org.jasig.cas.services.TestUtils.getService(), true);
        final ImmutableAssertion assertion2 = new ImmutableAssertion(auth, list2, org.jasig.cas.services.TestUtils.getService(), true);

        assertTrue(assertion1.equals(assertion2));
    }

    @Test
    public void verifyGetService() {
        final Service service = org.jasig.cas.services.TestUtils.getService();

        final List<Authentication> list = new ArrayList<>();
        list.add(org.jasig.cas.authentication.TestUtils.getAuthentication());

        final Assertion assertion = new ImmutableAssertion(
                org.jasig.cas.authentication.TestUtils.getAuthentication(), list, service, false);

        assertEquals(service, assertion.getService());
    }
}
