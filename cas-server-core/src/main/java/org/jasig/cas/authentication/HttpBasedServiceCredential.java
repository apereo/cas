/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication;

import java.net.URL;

import org.springframework.util.Assert;

/**
 * A credential representing an HTTP endpoint given by a URL. Authenticating the credential usually involves
 * contacting the endpoint via the URL and observing the resulting connection (e.g. SSL certificate) and response
 * (e.g. status, headers).
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0
 */
public class HttpBasedServiceCredential extends AbstractCredential {

    /** Unique Serializable ID. */
    private static final long serialVersionUID = 1492607216336354503L;

    /** The callbackURL to check that identifies the application. */
    private final URL callbackUrl;

    /** String form of callbackUrl. */
    private final String callbackUrlAsString;

    /**
     * Empty constructor used by Kryo for de-serialization.
     */
    protected HttpBasedServiceCredential() {
        this.callbackUrl =  null;
        this.callbackUrlAsString = null;
    }

    /**
     * Creates a new instance for an HTTP endpoint located at the given URL.
     *
     * @param callbackUrl Non-null URL that will be contacted to validate the credential.
     *
     * @throws IllegalArgumentException if the callbackUrl is null.
     */
    public HttpBasedServiceCredential(final URL callbackUrl) {
        Assert.notNull(callbackUrl, "callbackUrl cannot be null");
        this.callbackUrl = callbackUrl;
        this.callbackUrlAsString = callbackUrl.toExternalForm();
    }

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return this.callbackUrlAsString;
    }

    /**
     * @return Returns the callbackUrl.
     */
    public final URL getCallbackUrl() {
        return this.callbackUrl;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
            * result
            + ((this.callbackUrlAsString == null) ? 0 : this.callbackUrlAsString
                .hashCode());
        return result;
    }

    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HttpBasedServiceCredential other = (HttpBasedServiceCredential) obj;
        if (this.callbackUrlAsString == null) {
            if (other.callbackUrlAsString != null) {
                return false;
            }
        } else if (!this.callbackUrlAsString.equals(other.callbackUrlAsString)) {
            return false;
        }
        return true;
    }


}
