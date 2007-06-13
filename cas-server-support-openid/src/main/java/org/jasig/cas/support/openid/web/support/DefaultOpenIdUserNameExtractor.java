/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.web.support;


/**
 * Extracts a local Id from an openid.identity. The default provider can extract
 * the following uris: http://openid.myprovider.com/scottb provides a local id
 * of scottb.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class DefaultOpenIdUserNameExtractor implements
    OpenIdUserNameExtractor {

    public String extractLocalUsernameFromUri(final String uri) {
        if (uri == null) {
            return null;
        }

        if (!uri.contains("/")) {
            return null;
        }

        return uri.substring(uri.lastIndexOf("/") + 1);
    }

}
