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

package org.jasig.cas.extension.clearpass.integration.uportal;

import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;
import org.jasig.portal.security.provider.cas.CasAssertionSecurityContext;
import org.springframework.util.Assert;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 1.0.0.GA
 */
public class PasswordCachingCasAssertionSecurityContext extends CasAssertionSecurityContext {

    private final String clearPassUrl;

    private byte[] cachedCredentials;

    protected PasswordCachingCasAssertionSecurityContext(final String clearPassUrl) {
        super();
        Assert.notNull(clearPassUrl, "clearPassUrl cannot be null.");
        this.clearPassUrl = clearPassUrl;
    }

    @Override
    protected final void postAuthenticate(final Assertion assertion) {
        final String proxyTicket = assertion.getPrincipal().getProxyTicketFor(this.clearPassUrl);

        if (proxyTicket == null) {
            log.error("Unable to obtain proxy ticket for ClearPass service.");
            return;
        }

        final String password = retrievePasswordFromResponse(proxyTicket);

        if (password != null) {
            log.debug("Password retrieved from ClearPass.");
            this.cachedCredentials = password.getBytes();
        } else {
            log.debug("Unable to retrieve password from ClearPass.");
        }
    }

    @Override
    public final IOpaqueCredentials getOpaqueCredentials() {
        if (this.cachedCredentials == null) {
            return super.getOpaqueCredentials();
        }

        final NotSoOpaqueCredentials credentials = new CacheOpaqueCredentials();
        credentials.setCredentials(this.cachedCredentials);
        return credentials;
    }

    protected final String retrievePasswordFromResponse(final String proxyTicket) {
        final String url = this.clearPassUrl + (this.clearPassUrl.contains("?") ? "&" : "?") + "ticket=" + proxyTicket;
        final String response = retrieveResponseFromServer(url, "UTF-8");
        final String password = XmlUtils.getTextForElement(response, "credentials");


        if (log.isTraceEnabled()) {
            log.trace(String.format("ClearPass Response was:\n %s", response));
        }

        if (CommonUtils.isNotBlank(password)) {
            return password;
        }

        log.error("Unable to Retrieve Password.  If you see a [403] HTTP response code returned from the CommonUtils then it most likely means the proxy configuration on the CAS server is not correct.\n\n"
                + "Full Response from ClearPass was [" + response + "].");
        return null;
    }

    /**
     * Exists purely for testing purposes.
     */
    protected String retrieveResponseFromServer(final String url, final String encoding) {
        return CommonUtils.getResponseFromServer(url, "UTF-8");
    }


    /**
	 * Copied from {@link org.jasig.portal.security.provider.CacheSecurityContext}
	 */
	private class CacheOpaqueCredentials extends ChainingOpaqueCredentials implements NotSoOpaqueCredentials {

		private static final long serialVersionUID = 1l;

		public String getCredentials() {
            return this.credentialstring != null ? new String(this.credentialstring) : null;
		}
	}

}
