package org.jasig.cas.authentication;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class AssertionImpl implements Assertion {
	private List principals;
	
	public AssertionImpl(final List principals) {
		this.principals = principals;
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
}
