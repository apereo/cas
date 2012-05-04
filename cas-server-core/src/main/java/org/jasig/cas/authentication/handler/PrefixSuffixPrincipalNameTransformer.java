/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */

package org.jasig.cas.authentication.handler;

/**
 * Transform the user id by adding a prefix or suffix.
 *
 * @author Howard Gilbert
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.6
 */

public final class PrefixSuffixPrincipalNameTransformer implements PrincipalNameTransformer {

    private String prefix;

    private String suffix;

    public String transform(final String formUserId) {
        final StringBuilder stringBuilder = new StringBuilder();

        if (this.prefix != null) {
            stringBuilder.append(this.prefix);
        }

        stringBuilder.append(formUserId);

        if (this.suffix != null) {
            stringBuilder.append(this.suffix);
        }

        return stringBuilder.toString();
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }
}
