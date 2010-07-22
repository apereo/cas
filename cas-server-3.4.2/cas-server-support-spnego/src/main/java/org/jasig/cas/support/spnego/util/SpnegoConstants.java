/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.spnego.util;

/**
 * Spnego Constants
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @version $Revision$ $Date$
 * @since 3.1
 */
public interface SpnegoConstants {
    
    String HEADER_AUTHENTICATE = "WWW-Authenticate";

    String HEADER_AUTHORIZATION = "Authorization";
    
    String HEADER_USER_AGENT = "User-Agent";

    String NEGOTIATE = "Negotiate";

    String SPNEGO_FIRST_TIME = "spnegoFirstTime";

    String SPNEGO_CREDENTIALS = "spnegoCredentials";

    byte[] NTLMSSP_SIGNATURE = new byte[]{(byte) 'N', (byte) 'T', (byte) 'L',
            (byte) 'M', (byte) 'S', (byte) 'S', (byte) 'P', (byte) 0};
    
    String NTLM = "NTLM";
}
