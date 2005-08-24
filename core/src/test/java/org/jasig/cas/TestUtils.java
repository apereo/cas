/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import java.net.MalformedURLException;
import java.net.URL;

import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.2
 *
 */
public final class TestUtils {

    public static UsernamePasswordCredentials getCredentialsWithSameUsernameAndPassword() {
        return getCredentialsWithSameUsernameAndPassword("test");
    }
    
    public static UsernamePasswordCredentials getCredentialsWithSameUsernameAndPassword(final String username) {
        return getCredentialsWithDifferentUsernameAndPassword(username, username);
    }
    
    public static UsernamePasswordCredentials getCredentialsWithDifferentUsernameAndPassword(final String username, final String password) {
        final UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.setPassword(password);
        
        return usernamePasswordCredentials;
    }
    
    public static HttpBasedServiceCredentials getHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials("https://www.acs.rutgers.edu");
    }
    
    public static HttpBasedServiceCredentials getBadHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials("http://www.acs.rutgers.edu");
    }
    
    public static HttpBasedServiceCredentials getHttpBasedServiceCredentials(final String url) {
        try {
            HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(
                new URL(url));
            return c;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }
}
