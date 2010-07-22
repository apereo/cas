/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.spnego;

import java.security.Principal;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @version $Id$
 * @since 3.1
 * 
 */
public class MockPrincipal implements Principal {

    private String principal;

	public MockPrincipal(String principal) {
		super();
		this.principal = principal;
	}

	public String getName() {
		return this.principal;
	}

}
