/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

/**
 * Represents a service using CAS that comes from the web.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.3 $ $Date: 2007/02/27 19:31:58 $
 * @since 3.1
 */
public interface WebApplicationService extends Service {

    /**
     * Constructs the url to redirect the service back to.
     * 
     * @param ticketId the service ticket to provide to the service.
     * @return the redirect url.
     */
    Response getResponse(String ticketId);

    /**
     * Retrieves the artifact supplied with the service. May be null.
     * 
     * @return the artifact if it exists, null otherwise.
     */
    String getArtifactId();
}
