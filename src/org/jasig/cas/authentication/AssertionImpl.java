package org.jasig.cas.authentication;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class AssertionImpl implements Assertion {
	final private List principals;
    final private boolean fromNewLogin;
	
	public AssertionImpl(final List principals, boolean fromNewLogin) {
		this.principals = principals;
        this.fromNewLogin = fromNewLogin;
	}

	/**
	 * @see org.jasig.cas.authentication.Assertion#getPrincipal()
	 */
	public List getChainedPrincipals() {
		return this.principals;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
    /**
     * @return Returns the isFromNewLogin.
     */
    public boolean isFromNewLogin() {
        return this.fromNewLogin;
    }
}
