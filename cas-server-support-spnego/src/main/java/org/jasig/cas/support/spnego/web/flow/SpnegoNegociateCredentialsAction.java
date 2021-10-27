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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.support.spnego.util.SpnegoConstants;
import org.jasig.cas.web.support.WebUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * First action of a SPNEGO flow : negotiation.
 * <p>The server checks if the
 * negotiation string is in the request header and this is a supported browser:
 * <ul>
 * <li>If found do nothing and return <code>success()</code></li>
 * <li>else add a WWW-Authenticate response header and a 401 response status,
 * then return <code>success()</code></li>
 * </ul>
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @author Scott Battaglia
 * @author John Gasper
 * @see <a href="http://ietfreport.isoc.org/idref/rfc4559/#page-2">RFC 4559</a>
 * @since 3.1
 */
public final class SpnegoNegociateCredentialsAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpnegoNegociateCredentialsAction.class);

    /** Whether this is using the NTLM protocol or not. */
    private boolean ntlm;

    private boolean mixedModeAuthentication;

    private List<String> supportedBrowser;

    private String messageBeginPrefix = constructMessagePrefix();

    /**
     * Instantiates a new Spnego negociate credentials action.
     * Also initializes the list of supported browser user agents with the following:
     * <ul>
     *     <li><code>MSIE</code></li>
     *     <li><code>Trident</code></li>
     *     <li><code>Firefox</code></li>
     *     <li><code>AppleWebKit</code></li>
     * </ul>
     * @see #setSupportedBrowser(List)
     * @since 4.1
     */
    public SpnegoNegociateCredentialsAction() {
        super();

        this.supportedBrowser = new ArrayList<>();
        this.supportedBrowser.add("MSIE");
        this.supportedBrowser.add("Trident");
        this.supportedBrowser.add("Firefox");
        this.supportedBrowser.add("AppleWebKit");

    }

    @Override
    protected Event doExecute(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);

        final String authorizationHeader = request.getHeader(SpnegoConstants.HEADER_AUTHORIZATION);
        final String userAgent = request.getHeader(SpnegoConstants.HEADER_USER_AGENT);

        LOGGER.debug("Authorization header [{}], User Agent header [{}]", authorizationHeader, userAgent);

        if (!StringUtils.hasText(userAgent) || this.supportedBrowser.isEmpty()) {
            LOGGER.debug("User Agent header [{}] is empty, or no browsers are supported", userAgent);
            return success();
        }

        if (!isSupportedBrowser(userAgent)) {
            LOGGER.debug("User Agent header [{}] is not supported in the list of supported browsers [{}]",
                    userAgent, this.supportedBrowser);
            return success();
        }

        if (!StringUtils.hasText(authorizationHeader)
                || !authorizationHeader.startsWith(this.messageBeginPrefix)
                || authorizationHeader.length() <= this.messageBeginPrefix
                .length()) {

            final String wwwHeader = this.ntlm ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE;
            LOGGER.debug("Authorization header not found or does not match the message prefix [{}]. Sending [{}] header [{}]",
                    this.messageBeginPrefix, SpnegoConstants.HEADER_AUTHENTICATE, wwwHeader);
            response.setHeader(SpnegoConstants.HEADER_AUTHENTICATE, wwwHeader);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // The responseComplete flag tells the pausing view-state not to render the response
            // because another object has taken care of it. If mixed mode authentication is allowed
            // then responseComplete should not be called so that webflow will display the login page.
            if (!this.mixedModeAuthentication) {
                LOGGER.debug("Mixed-mode authentication is disabled. Executing completion of response");
                context.getExternalContext().recordResponseComplete();
            }
        }
        return success();
    }

    /**
     * Sets the ntlm. Generates the message prefix as well.
     *
     * @param ntlm the new ntlm
     */
    public void setNtlm(final boolean ntlm) {
        this.ntlm = ntlm;
        this.messageBeginPrefix = constructMessagePrefix();
    }

    /**
     * Sets supported browsers by their user agent. The user agent
     * header defined by {@link SpnegoConstants#HEADER_USER_AGENT}
     * will be compared against this list. The user agents configured
     * here need not be an exact match. So longer is the user agent identifier
     * configured in this list is "found" in the user agent header retrieved,
     * the check will pass.
     *
     * @param supportedBrowser the supported browsers list
     */
    public void setSupportedBrowser(final List<String> supportedBrowser) {
        this.supportedBrowser = supportedBrowser;
    }

   /**
    * Sets whether mixed mode authentication should be enabled. If it is
    * enabled then control is allowed to pass back to the Spring Webflow
    * instead of immediately terminating the page after issuing the
    * unauthorized (401) header. This has the effect of displaying the login
    * page on unsupported/configured browsers.
    * <p>
    * If this is set to false then the page is immediately closed after the
    * unauthorized header is sent. This is ideal in environments that only
    * want to use Windows Integrated Auth/SPNEGO and not forms auth.
    *
    * @param enabled should mixed mode authentication be allowed. Default is false.
    */
    public void setMixedModeAuthentication(final boolean enabled) {
        this.mixedModeAuthentication = enabled;
    }

    /**
     * Construct message prefix.
     *
     * @return if {@link #ntlm} is enabled, {@link SpnegoConstants#NTLM}, otherwise
     * {@link SpnegoConstants#NEGOTIATE}. An extra space is appended to the end.
     */
    protected String constructMessagePrefix() {
        return (this.ntlm ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE)
                + ' ';
    }

    /**
     * Checks if is supported browser.
     *
     * @param userAgent the user agent
     * @return true, if  supported browser
     */
    protected boolean isSupportedBrowser(final String userAgent) {
        for (final String supportedBrowser : this.supportedBrowser) {
            if (userAgent.contains(supportedBrowser)) {
                return true;
            }
        }
        return false;
    }
}
