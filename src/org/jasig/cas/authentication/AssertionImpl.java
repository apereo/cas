package org.jasig.cas.authentication;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Principal;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class AssertionImpl implements Assertion {
	private Principal principal;
	
	public AssertionImpl() {
		this.principal = null;
	}
	
	public AssertionImpl(final Principal principal) {
		this.principal = principal;
	}

	/**
	 * @see org.jasig.cas.authentication.Assertion#getPrincipal()
	 */
	public Principal getPrincipal() {
		return this.principal;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
