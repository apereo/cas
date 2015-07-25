/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredential;
import org.jasig.cas.support.spnego.util.SpnegoConstants;
import org.jasig.cas.util.CompressionUtils;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;

/**
 * Second action of a SPNEGO flow : decode the gssapi-data and build a new
 * {@link org.jasig.cas.support.spnego.authentication.principal.SpnegoCredential}.
 * <p>
 * Once AbstractNonInteractiveCredentialsAction has executed the authentication
 * procedure, this action check whether a principal is present in Credential and
 * add corresponding response headers.</p>
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @see <a href="http://ietfreport.isoc.org/idref/rfc4559/#page-2">RFC 4559</a>
 * @since 3.1
 */
public final class SpnegoCredentialsAction extends AbstractNonInteractiveCredentialsAction {


    private boolean ntlm;

    private String messageBeginPrefix = constructMessagePrefix();

    /**
     * Behavior in case of SPNEGO authentication failure :
     * <ul><li>True : if SPNEGO is the last authentication method with no fallback.</li>
     * <li>False : if an interactive view (eg: login page) should be send to user as SPNEGO failure fallback</li>
     * </ul>
     */
    private boolean send401OnAuthenticationFailure = true;

    @Override
    protected Credential constructCredentialsFromRequest(
            final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);

        final String authorizationHeader = request
                .getHeader(SpnegoConstants.HEADER_AUTHORIZATION);

        if (StringUtils.hasText(authorizationHeader)
                && authorizationHeader.startsWith(this.messageBeginPrefix)
                && authorizationHeader.length() > this.messageBeginPrefix.length()) {

            logger.debug("SPNEGO Authorization header found with {} bytes",
                    authorizationHeader.length() - this.messageBeginPrefix.length());

            final byte[] token = CompressionUtils.decodeBase64ToByteArray(authorizationHeader.substring(this.messageBeginPrefix.length()));
            if (token == null) {
                logger.warn("Could not compress authorization header in base64");
                return null;
            }
            logger.debug("Obtained token: {}", new String(token, Charset.defaultCharset()));
            return new SpnegoCredential(token);
        }

        return null;
    }

    /**
     * Construct message prefix.
     *
     * @return the string
     */
    protected String constructMessagePrefix() {
        return (this.ntlm ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE)
                + ' ';
    }

    @Override
    protected void onError(final RequestContext context,
            final Credential credential) {
        setResponseHeader(context, credential);
    }

    @Override
    protected void onSuccess(final RequestContext context,
            final Credential credential) {
        setResponseHeader(context, credential);
    }

    /**
     * Sets the response header based on the retrieved tocken.
     *
     * @param context the context
     * @param credential the credential
     */
    private void setResponseHeader(final RequestContext context,
            final Credential credential) {
        if (credential == null) {
            return;
        }

        final HttpServletResponse response = WebUtils
                .getHttpServletResponse(context);
        final SpnegoCredential spnegoCredentials = (SpnegoCredential) credential;
        final byte[] nextToken = spnegoCredentials.getNextToken();
        if (nextToken != null) {
            logger.debug("Obtained output token: {}", new String(nextToken, Charset.defaultCharset()));
            response.setHeader(SpnegoConstants.HEADER_AUTHENTICATE, (this.ntlm
                    ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE)
                    + ' ' + CompressionUtils.encodeBase64(nextToken));
        } else {
            logger.debug("Unable to obtain the output token required.");
        }

        if (spnegoCredentials.getPrincipal() == null && send401OnAuthenticationFailure) {
            logger.debug("Setting HTTP Status to 401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * Sets the ntlm and generate message prefix.
     *
     * @param ntlm the new ntlm
     */
    public void setNtlm(final boolean ntlm) {
        this.ntlm = ntlm;
        this.messageBeginPrefix = constructMessagePrefix();
    }

    public void setSend401OnAuthenticationFailure(final boolean send401OnAuthenticationFailure) {
        this.send401OnAuthenticationFailure = send401OnAuthenticationFailure;
    }

}
