package org.jasig.cas.authentication;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class AssertionImpl implements Assertion {

    final private List principals;

    final private boolean fromNewLogin;

    public AssertionImpl(final List principals, boolean fromNewLogin) {
        if (principals == null || principals.isEmpty()) {
            throw new IllegalArgumentException("principals cannot be null or empty.");
        }

        this.principals = principals;
        this.fromNewLogin = fromNewLogin;
    }

    /*
     * @see org.jasig.cas.authentication.Assertion#getChainedPrincipals()
     */
    public List getChainedPrincipals() {
        return this.principals;
    }

    /*
     * @see org.jasig.cas.authentication.Assertion#isFromNewLogin()
     */
    public boolean isFromNewLogin() {
        return this.fromNewLogin;
    }

    public boolean equals(Object o) {
        if (o == null || !this.getClass().equals(o.getClass()))
            return false;

        return EqualsBuilder.reflectionEquals(this, o);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
