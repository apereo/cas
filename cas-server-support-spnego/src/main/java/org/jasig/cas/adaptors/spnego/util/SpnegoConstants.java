/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.util;

/**
 * Spnego Constants
 * 
 * 
 * @author Arnaud Lessueur
 * @author Marc-Antoine Garrigue
 * @version $Id$
 * @since 3.1
 */
public class SpnegoConstants {

    public static final String HEADER_AUTHENTICATE = "WWW-Authenticate";

    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String NEGOTIATE = "Negotiate";

    public static final String SPNEGO_FIRST_TIME = "spnegoFirstTime";

    public static final String SPNEGO_CREDENTIALS = "spnegoCredentials";

    public static final byte[] NTLMSSP_SIGNATURE = new byte[] {(byte) 'N', (byte) 'T', (byte) 'L',
            (byte) 'M', (byte) 'S', (byte) 'S', (byte) 'P', (byte) 0};
}
