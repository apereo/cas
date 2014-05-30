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
package org.jasig.cas.ticket.support;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of the {@link TicketExpirationPolicyEvaluator} that is able to
 * determine whether the remote address of the incoming request matches a particular IP pattern.
 * The pattern may be specified as regular expression that is compiled and run against the remote address.
 * The retrieval of the remote IP address may be provided via IPv6 or IPv4 syntax.
 * 
 * <p><strong>NOTE:</strong> If you prefer to configure the pattern by IPv4 syntax only,
 * add the <code>-Djava.net.preferIPv4Stack=true</code> flag to your <code>JAVA_OPTS</code>
 * environment variable prior to restarting the container.
 * @author Misagh Moayyed
 * @since 4.1
 * @see CompositeTicketGrantingTicketExpirationPolicy
 */
public final class HttpRequestRemoteAddrExpirationPolicyEvaluator implements TicketExpirationPolicyEvaluator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Pattern ipAddressPattern;

    /**
     * A remote address evaluator initialized with the ip pattern.
     *
     * @param ipPattern the ip regex pattern. The matching by default
     * is case insensitive.
     */
    public HttpRequestRemoteAddrExpirationPolicyEvaluator(final String ipPattern) {
        this.ipAddressPattern = Pattern.compile(ipPattern, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean satisfiesTicketExpirationPolicy(final HttpServletRequest request, final TicketState state) {
        final String currentIp = request.getRemoteAddr();
        logger.debug("Remote address [{}] received to compare against [{}]", this.ipAddressPattern.pattern());
        return this.ipAddressPattern.matcher(currentIp).find();
    }

}
