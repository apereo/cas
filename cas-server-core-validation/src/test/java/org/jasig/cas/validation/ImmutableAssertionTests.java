package org.jasig.cas.validation;

import org.jasig.cas.util.ServicesTestUtils;
import org.jasig.cas.util.AuthTestUtils;
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

        list.add(AuthTestUtils.getAuthentication("test"));
        list.add(AuthTestUtils.getAuthentication("test1"));
        list.add(AuthTestUtils.getAuthentication("test2"));

        final ImmutableAssertion assertion = new ImmutableAssertion(
                AuthTestUtils.getAuthentication(), list, ServicesTestUtils.getService(), true);

        assertEquals(list.toArray(new Authentication[0]).length, assertion.getChainedAuthentications().size());
    }

    @Test
    public void verifyGetterFalseForNewLogin() {
        final List<Authentication> list = new ArrayList<>();

        list.add(AuthTestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                AuthTestUtils.getAuthentication(), list, ServicesTestUtils.getService(), false);

        assertFalse(assertion.isFromNewLogin());
    }

    @Test
    public void verifyGetterTrueForNewLogin() {
        final List<Authentication> list = new ArrayList<>();

        list.add(AuthTestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                AuthTestUtils.getAuthentication(), list, ServicesTestUtils.getService(), true);

        assertTrue(assertion.isFromNewLogin());
    }

    @Test
    public void verifyEqualsWithNull() {
        final List<Authentication> list = new ArrayList<>();
        list.add(AuthTestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                AuthTestUtils.getAuthentication(), list, ServicesTestUtils.getService(), true);

        assertNotEquals(assertion, null);
    }

    @Test
    public void verifyEqualsWithInvalidObject() {
        final List<Authentication> list = new ArrayList<>();
        list.add(AuthTestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                AuthTestUtils.getAuthentication(), list, ServicesTestUtils.getService(), true);

        assertFalse("test".equals(assertion));
    }

    @Test
    public void verifyEqualsWithValidObject() {
        final List<Authentication> list1 = new ArrayList<>();
        final List<Authentication> list2 = new ArrayList<>();

        final Authentication auth = AuthTestUtils.getAuthentication();
        list1.add(auth);
        list2.add(auth);

        final ImmutableAssertion assertion1 = new ImmutableAssertion(auth, list1, ServicesTestUtils.getService(), true);
        final ImmutableAssertion assertion2 = new ImmutableAssertion(auth, list2, ServicesTestUtils.getService(), true);

        assertTrue(assertion1.equals(assertion2));
    }

    @Test
    public void verifyGetService() {
        final Service service = ServicesTestUtils.getService();

        final List<Authentication> list = new ArrayList<>();
        list.add(AuthTestUtils.getAuthentication());

        final Assertion assertion = new ImmutableAssertion(
                AuthTestUtils.getAuthentication(), list, service, false);

        assertEquals(service, assertion.getService());
    }
}
