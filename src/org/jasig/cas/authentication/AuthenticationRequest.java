/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.io.Serializable;

/**
 * Marker interface to identify objects as related to the authentication of a user. The AuthenticationManager knows how to handle
 * AuthenticationRequests while an AuthenticationHandler will know how to handle a specific implementation of an AuthenticationRequest.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface AuthenticationRequest extends Serializable {
}
