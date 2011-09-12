/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

/**
 * Simple implementation that actually does NO transformation.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.6
 */
public final class NoOpPrincipalNameTransformer implements PrincipalNameTransformer {

    public String transform(final String formUserId) {
        return formUserId;
    }
}
