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
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.ContextualAuthenticationPolicy;
import org.springframework.util.Assert;

/**
 * Error condition arising at ticket creation or validation time when a ticketing operation relying on authentication
 * cannot proceed due to unsatisfied authentication security policy.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class UnsatisfiedAuthenticationPolicyException extends TicketException {

    /** Serializable ID for unique id. */
    private static final long serialVersionUID = -827432780367197133L;

    /** Code description. */
    private static final String CODE = "UNSATISFIED_AUTHN_POLICY";

    /** Unfulfilled policy that caused this exception. */
    private final ContextualAuthenticationPolicy<?> policy;

    /**
     * Creates a new instance with no cause.
     *
     * @param policy Non-null unfulfilled security policy that caused exception.
     */
    public UnsatisfiedAuthenticationPolicyException(final ContextualAuthenticationPolicy<?> policy) {
        super(CODE);
        Assert.notNull(policy, "ContextualAuthenticationPolicy cannot be null");
        this.policy = policy;
    }

    /**
     * Gets the unsatisfied policy that caused this exception.
     *
     * @return Non-null unsatisfied policy cause.
     */
    public ContextualAuthenticationPolicy<?> getPolicy() {
        return policy;
    }
}
