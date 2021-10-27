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

import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;

/**
 * Ticket expiration policy based on a hard timeout from ticket creation time rather than the
 * "idle" timeout provided by {@link org.jasig.cas.ticket.support.TimeoutExpirationPolicy}.
 *
 * @author Andrew Feller
 * @version $Revision$ $Date$
 * @since 3.1.2
 */
public final class HardTimeoutExpirationPolicy implements ExpirationPolicy {

	/** Unique Id for serialization. */
    private static final long serialVersionUID = -1465997330804816888L;
    
    /** The time to kill in milliseconds. */
	private final long timeToKillInMilliSeconds;

	public HardTimeoutExpirationPolicy(final long timeToKillInMilliSeconds) {
		this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
	}

	public boolean isExpired(final TicketState ticketState) {
		return (ticketState == null)
					|| (System.currentTimeMillis() - ticketState.getCreationTime() >= this.timeToKillInMilliSeconds);
	}
}
