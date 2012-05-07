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
package org.jasig.cas.support.spnego.web.flow;

import jcifs.util.Base64;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredentials;
import org.jasig.cas.support.spnego.util.SpnegoConstants;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Second action of a SPNEGO flow : decode the gssapi-data and build a new
 * {@link org.jasig.cas.support.spnego.authentication.principal.SpnegoCredentials}.<br/>
 * Once AbstractNonInteractiveCredentialsAction has executed the authentication
 * procedure, this action check wether a principal is present in Credentials and
 * add correspondings response headers.
 * 
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @version $Revision$ $Date$
 * @see <a href='http://ietfreport.isoc.org/idref/rfc4559/#page-2'>RFC 4559</a>
 * @since 3.1
 */
public final class SpnegoCredentialsAction extends
    AbstractNonInteractiveCredentialsAction {

    private boolean ntlm = false;

    private String messageBeginPrefix = constructMessagePrefix();

    protected Credentials constructCredentialsFromRequest(
        final RequestContext context) {
        final HttpServletRequest request = WebUtils
            .getHttpServletRequest(context);

        final String authorizationHeader = request
            .getHeader(SpnegoConstants.HEADER_AUTHORIZATION);

        if (StringUtils.hasText(authorizationHeader)
            && authorizationHeader.startsWith(this.messageBeginPrefix)
            && authorizationHeader.length() > this.messageBeginPrefix.length()) {
            if (logger.isDebugEnabled()) {
                logger.debug("SPNEGO Authorization header found with "
                    + (authorizationHeader.length() - this.messageBeginPrefix
                        .length()) + " bytes");
            }
            final byte[] token = Base64.decode(authorizationHeader
                .substring(this.messageBeginPrefix.length()));
            if (logger.isDebugEnabled()) {
                logger.debug("Obtained token: " + new String(token));
            }
            return new SpnegoCredentials(token);
        }

        return null;
    }

    protected String constructMessagePrefix() {
        return (this.ntlm ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE)
            + " ";
    }

    protected void onError(final RequestContext context,
        final Credentials credentials) {
        setResponseHeader(context, credentials);
    }

    protected void onSuccess(final RequestContext context,
        final Credentials credentials) {
        setResponseHeader(context, credentials);
    }

    private void setResponseHeader(final RequestContext context,
        final Credentials credentials) {
        if (credentials == null) {
            return;
        }

        final HttpServletResponse response = WebUtils
            .getHttpServletResponse(context);
        final SpnegoCredentials spnegoCredentials = (SpnegoCredentials) credentials;
        final byte[] nextToken = spnegoCredentials.getNextToken();
        if (nextToken != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Obtained output token: " + new String(nextToken));
            }
            response.setHeader(SpnegoConstants.HEADER_AUTHENTICATE, (this.ntlm
                ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE)
                + " " + Base64.encode(nextToken));
        } else {
            logger.debug("Unable to obtain the output token required.");
        }

        if (spnegoCredentials.getPrincipal() == null) {
            logger.debug("Setting HTTP Status to 401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    public void setNtlm(final boolean ntlm) {
        this.ntlm = ntlm;
        this.messageBeginPrefix = constructMessagePrefix();
    }
}