/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.mock;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.principal.Credentials;

public class MockAuthenticationMetaDataPopulator implements
    AuthenticationMetaDataPopulator {

    public Authentication populateAttributes(Authentication authentication,
        Credentials credentials) {
        return authentication;
    }

}
