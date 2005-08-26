/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
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
