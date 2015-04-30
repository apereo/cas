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

package org.jasig.cas.support.spnego.web.flow.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * Abstract class for defining a simple binary filter to determine whether a
 * given client system should be prompted for SPNEGO / KRB / NTLM credentials.
 *
 * Envisioned implementations would include LDAP and DNS based determinations,
 * but of course others may have value as well for local architectures.
 *
 * @author Sean Baker sean.baker@usuhs.edu
 * @author Misagh Moayyed
 * @since 4.1
 */
public class BaseSpnegoKnownClientSystemsFilterAction extends AbstractAction {
    /** Logger instance. **/
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Pattern of idp addresses to check. **/
    private Pattern ipsToCheckPattern;

    private String alternativeRemoteHostAttribute;

    /** The remote ip address. **/
    private String remoteIp;

    /**
     * {@inheritDoc}
     * Gets the remote ip from the request, and invokes spnego if it isn't filtered.
     *
     * @param context
     * @return {@link #yes()} if spnego should be invoked and ip isnt filtered,
     * {@link #no()} otherwise.
     */
    @Override
    protected final Event doExecute(@NotNull final RequestContext context) {
        this.remoteIp = getRemoteIp(context);
        logger.debug("Current user IP {}", this.remoteIp);
        return !isIpFiltered() && doSpnego() ? yes() : no();
    }

    /**
     * Simple pattern match to determine whether an IP should be checked.
     * Could stand to be extended to support "real" IP addresses and patterns, but
     * for the local / first implementation regex made more sense.
     * @return whether the remote ip received should be ignored
     */
    private boolean isIpFiltered() {
        return this.ipsToCheckPattern != null
            && ipsToCheckPattern.matcher(this.remoteIp).find();
    }

    /**
     * Pulls the remote IP from the current HttpServletRequest, or grabs the value
     * for the specified alternative attribute (say, for proxied requests).  Falls
     * back to providing the "normal" remote address if no value can be retrieved
     * from the specified alternative header value.
     * @param context the context
     * @return the remote ip
     */
    private String getRemoteIp(@NotNull final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        String userAddr = request.getRemoteAddr();
        logger.debug("Remote Address = {}", userAddr);

        if (StringUtils.isNotBlank(this.alternativeRemoteHostAttribute)) {

            userAddr = request.getHeader(this.alternativeRemoteHostAttribute);
            logger.debug("Header Attribute [{}] = [{}]", this.alternativeRemoteHostAttribute, userAddr);

            if (StringUtils.isBlank(userAddr)) {
                userAddr = request.getRemoteAddr();
                logger.warn("No value could be retrieved from the header [{}]. Falling back to [{}].",
                        this.alternativeRemoteHostAttribute, userAddr);
            }
        }
        return userAddr;
    }

    /**
     * Convenience method to perform a reverse DNS lookup, presumably of the
     * remote system's IP address, though of course it could be used for anything.
     * Falls back to providing the IP address back if no value is found or the
     * request results in an exception.
     * @return the remote host name
     */
    protected final String getRemoteHostName() {
        try {
            final InetAddress remoteHost = InetAddress.getByName(this.remoteIp);
            return remoteHost.getCanonicalHostName();
        } catch (final UnknownHostException e) {
            logger.error("No host found for IP {}", this.remoteIp, e);
        }
        return this.remoteIp;
    }

    /**
     * Alternative header to be used for retrieving the remote system IP address.
     * @param alternativeRemoteHostAttribute the alternative remote host attribute
     */
    public final void setAlternativeRemoteHostAttribute(@NotNull final String alternativeRemoteHostAttribute) {
        this.alternativeRemoteHostAttribute = alternativeRemoteHostAttribute;
    }

    /**
     * Regular expression string to define IPs which should be considered.
     * @param ipsToCheckPattern the ips to check as a regex pattern
     */
    public final void setIpsToCheckPattern(@NotNull final String ipsToCheckPattern) {
        this.ipsToCheckPattern = Pattern.compile(ipsToCheckPattern);
    }

    protected final String getRemoteIp() {
        return this.remoteIp;
    }

    @Override
    public final String toString() {
        return new ToStringBuilder(this)
                .append("ipsToCheckPattern", this.ipsToCheckPattern)
                .append("alternativeRemoteHostAttribute", this.alternativeRemoteHostAttribute)
                .append("remoteIp", this.remoteIp)
                .toString();
    }

    /**
     * Left to the implementer to provide a binary value
     * as to whether Spnego should
     * be performed for the requesting system.
     * @return always true
     */
    protected boolean doSpnego() {
        return true;
    }
}
