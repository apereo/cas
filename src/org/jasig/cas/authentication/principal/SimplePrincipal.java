/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class SimplePrincipal implements Principal {
	final private String id;
	
	public SimplePrincipal(final String id) {
		if (id == null)
			throw new IllegalArgumentException("id is a required parameters.");

		this.id = id;
	}
	/**
	 * @see org.jasig.cas.authentication.principal.Principal#getId()
	 */
	public String getId() {
		return id;
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof SimplePrincipal))
			return false;
		
		SimplePrincipal test = (SimplePrincipal) o;
		
		return this.id.equals(test.getId()); 
	}
	

}
