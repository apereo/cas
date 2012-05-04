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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.support.spnego.util.SpnegoConstants;
import org.jasig.cas.web.support.WebUtils;

import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * First action of a SPNEGO flow : negociation.<br/> The server checks if the
 * negociation string is in the request header and this is a supported browser:
 * <ul>
 * <li>If found do nothing and return <code>success()</code></li>
 * <li>else add a WWW-Authenticate response header and a 401 response status,
 * then return <code>success()</code></li>
 * </ul>
 * 
 * @see <a href='http://ietfreport.isoc.org/idref/rfc4559/#page-2'>RFC 4559</a>
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SpnegoNegociateCredentialsAction extends AbstractAction {

    /** Whether this is using the NTLM protocol or not. */
    private boolean ntlm = false;

    private List<String> supportedBrowser;

    private String messageBeginPrefix = constructMessagePrefix();

    protected Event doExecute(RequestContext context) {
        final HttpServletRequest request = WebUtils
            .getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils
            .getHttpServletResponse(context);
        final String authorizationHeader = request
            .getHeader(SpnegoConstants.HEADER_AUTHORIZATION);
        final String userAgent = request
            .getHeader(SpnegoConstants.HEADER_USER_AGENT);

        if (StringUtils.hasText(userAgent) && isSupportedBrowser(userAgent)) {
            if (!StringUtils.hasText(authorizationHeader)
                || !authorizationHeader.startsWith(this.messageBeginPrefix)
                || authorizationHeader.length() <= this.messageBeginPrefix
                    .length()) {
                if (logger.isDebugEnabled()) {
                    logger
                        .debug("Authorization header not found. Sending WWW-Authenticate header");
                }
                response.setHeader(SpnegoConstants.HEADER_AUTHENTICATE,
                    this.ntlm ? SpnegoConstants.NTLM
                        : SpnegoConstants.NEGOTIATE);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                // The responseComplete flag tells the pausing view-state not to render the response
                // because another object has taken care of it.
                context.getExternalContext().recordResponseComplete();
            }
        }
        return success();
    }

    public void setNtlm(final boolean ntlm) {
        this.ntlm = ntlm;
        this.messageBeginPrefix = constructMessagePrefix();
    }

    public void setSupportedBrowser(final List<String> supportedBrowser) {
        this.supportedBrowser = supportedBrowser;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.supportedBrowser == null) {
            this.supportedBrowser = new ArrayList<String>();
            this.supportedBrowser.add("MSIE");
            this.supportedBrowser.add("Firefox");
            this.supportedBrowser.add("AppleWebKit");
        }
    }

    protected String constructMessagePrefix() {
        return (this.ntlm ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE)
            + " ";
    }

    protected boolean isSupportedBrowser(final String userAgent) {
        for (final String supportedBrowser : this.supportedBrowser) {
            if (userAgent.contains(supportedBrowser)) {
                return true;
            }
        }
        return false;
    }
}
