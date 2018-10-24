package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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
        val list = new ArrayList<Authentication>();

        list.add(CoreAuthenticationTestUtils.getAuthentication("test"));
        list.add(CoreAuthenticationTestUtils.getAuthentication("test1"));
        list.add(CoreAuthenticationTestUtils.getAuthentication("test2"));
        val assertion = new ImmutableAssertion(CoreAuthenticationTestUtils.getAuthentication(), list, true, RegisteredServiceTestUtils.getService());
        assertEquals(list.toArray(Authentication[]::new).length, assertion.getChainedAuthentications().size());
    }

    @Test
    public void verifyGetterFalseForNewLogin() {
        val list = new ArrayList<Authentication>();
        list.add(CoreAuthenticationTestUtils.getAuthentication());
        val assertion = new ImmutableAssertion(CoreAuthenticationTestUtils.getAuthentication(), list, false, RegisteredServiceTestUtils.getService());
        assertFalse(assertion.isFromNewLogin());
    }

    @Test
    public void verifyGetterTrueForNewLogin() {
        val list = new ArrayList<Authentication>();

        list.add(CoreAuthenticationTestUtils.getAuthentication());

        val assertion = new ImmutableAssertion(
            CoreAuthenticationTestUtils.getAuthentication(), list, true, RegisteredServiceTestUtils.getService());

        assertTrue(assertion.isFromNewLogin());
    }

    @Test
    public void verifyEqualsWithNull() {
        val list = new ArrayList<Authentication>();
        list.add(CoreAuthenticationTestUtils.getAuthentication());

        val assertion = new ImmutableAssertion(
            CoreAuthenticationTestUtils.getAuthentication(), list, true, RegisteredServiceTestUtils.getService());

        assertNotEquals(assertion, null);
    }

    @Test
    public void verifyEqualsWithInvalidObject() {
        val list = new ArrayList<Authentication>();
        list.add(CoreAuthenticationTestUtils.getAuthentication());
        val assertion = new ImmutableAssertion(CoreAuthenticationTestUtils.getAuthentication(), list, true, RegisteredServiceTestUtils.getService());
        assertFalse("test".equals(assertion));
    }

    @Test
    public void verifyEqualsWithValidObject() {
        val list1 = new ArrayList<Authentication>();
        val list2 = new ArrayList<Authentication>();

        val auth = CoreAuthenticationTestUtils.getAuthentication();
        list1.add(auth);
        list2.add(auth);

        val assertion1 = new ImmutableAssertion(auth, list1, true, RegisteredServiceTestUtils.getService());
        val assertion2 = new ImmutableAssertion(auth, list2, true, RegisteredServiceTestUtils.getService());

        assertTrue(assertion1.equals(assertion2));
    }

    @Test
    public void verifyGetService() {
        val service = RegisteredServiceTestUtils.getService();

        val list = new ArrayList<Authentication>();
        list.add(CoreAuthenticationTestUtils.getAuthentication());

        val assertion = new ImmutableAssertion(CoreAuthenticationTestUtils.getAuthentication(), list, false, service);

        assertEquals(service, assertion.getService());
    }
}
