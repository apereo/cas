/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.principal;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpBasedServiceCredentialsToPrincipalResolverTests extends
    TestCase {

    private CredentialsToPrincipalResolver resolver = new HttpBasedServiceCredentialsToPrincipalResolver();

    public void testInValidSupportsCredentials() {
        assertFalse(this.resolver.supports(TestUtils
            .getCredentialsWithSameUsernameAndPassword()));
    }

    public void testNullSupportsCredentials() {
        assertFalse(this.resolver.supports(null));
    }

    public void testValidSupportsCredentials() {
        assertTrue(this.resolver.supports(TestUtils
            .getHttpBasedServiceCredentials()));
    }

    public void testValidCredentials() {
        assertEquals(this.resolver.resolvePrincipal(
            TestUtils.getHttpBasedServiceCredentials()).getId(), TestUtils
            .getHttpBasedServiceCredentials().getCallbackUrl().toExternalForm());
    }
}