/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

public class SpnegoCredentials implements Credentials {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -3606956950223931438L;

    private final String spnegoToken;

    public SpnegoCredentials(final String spnegoToken) {
        this.spnegoToken = spnegoToken;
    }

}
