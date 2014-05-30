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

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * This is a composite ticket expiration policy able to use
 * an {@link ExpirationPolicy} that is mapped to a {@link TicketExpirationPolicyEvaluator}.
 * The {@link TicketExpirationPolicyEvaluator} instance determines whether the policy is appropriate
 * for handling the given {@link TicketState}. If so, then its linked-to
 * {@link ExpirationPolicy} will be used.
 * 
 * <p>If none of the expiration policies satisfy the request, then the default policy will be used
 * that is set by {@link #setDefaultExpirationPolicy(ExpirationPolicy)}. If the default is not
 * explicitly set, the handling of the policy is delegated to {@link AlwaysExpiresExpirationPolicy}
 * which considers all tickets as expired.

 * @author Misagh Moayyed
 * @since 4.1
 * @see TicketExpirationPolicyEvaluator
 * @see ExpirationPolicy
 */
public final class CompositeTicketGrantingTicketExpirationPolicy implements ExpirationPolicy {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final long serialVersionUID = 3021175146846182330L;

    private final Map<TicketExpirationPolicyEvaluator, ExpirationPolicy> evaluators;

    /**
     * The default expiration policy if no evaluator succeeds.
     * By default, {@link AlwaysExpiresExpirationPolicy} is used.
     **/
    private ExpirationPolicy defaultExpirationPolicy = new AlwaysExpiresExpirationPolicy();

    /**
     * Init the policy with the given map of evaluators.
     * @param evaluators map of evaluators that are linked to expiration policies. 
     */
    public CompositeTicketGrantingTicketExpirationPolicy(final Map<TicketExpirationPolicyEvaluator, ExpirationPolicy> evaluators) {
        this.evaluators = evaluators;
    }

    public void setDefaultExpirationPolicy(final ExpirationPolicy def) {
        this.defaultExpirationPolicy = def;
    }

    @Override
    public boolean isExpired(final TicketState state) {
        final Set<TicketExpirationPolicyEvaluator> keys = this.evaluators.keySet();

        for (final TicketExpirationPolicyEvaluator eval : keys) {

            if (eval.satisfiesTicketExpirationPolicy(getRequest(), state)) {
                logger.debug("Expiration policy evaluator [{}] satisfies this request", eval);

                final ExpirationPolicy policy = this.evaluators.get(eval);
                final boolean expired = policy.isExpired(state);

                logger.debug("Delegated to mapped expiration policy [{}], which indicates the ticket has " + (expired ? "" : "not ")
                        + "expired", policy);

                return expired;
            }
        }

        logger.debug("Delegated to default expiration policy [{}]", this.defaultExpirationPolicy);
        return this.defaultExpirationPolicy.isExpired(state);
    }

    /**
     * Gets the http request based on the {@link RequestContextHolder}.
     *
     * @return the request
     */
    private HttpServletRequest getRequest() {
        final ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest();
    }

    /**
     * An expiration policy that always considers tickets expired.
     * Only enacted if no other policy is activated. 
     */
    private class AlwaysExpiresExpirationPolicy implements ExpirationPolicy {
        private static final long serialVersionUID = -5505383542873474014L;

        @Override
        public boolean isExpired(final TicketState ticketState) {
            logger.debug("Ticket is ALWAYS considered expired.");
            return true;
        }

    }
}
