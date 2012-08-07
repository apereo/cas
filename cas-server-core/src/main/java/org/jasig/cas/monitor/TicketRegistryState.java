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
package org.jasig.cas.monitor;

/**
 * Describes important state information that may be optionally exposed by
 * {@link org.jasig.cas.ticket.registry.TicketRegistry} components that might
 * be of interest to monitors.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public interface TicketRegistryState {
    /**
     * Computes the number of SSO sessions stored in the ticket registry.
     *
     * @return Number of ticket-granting tickets in the registry at time of invocation
     *         or {@link Integer#MIN_VALUE} if unknown.
     */
    int sessionCount();


     /**
     * Computes the number of service tickets stored in the ticket registry.
     *
     * @return Number of service tickets in the registry at time of invocation
     *         or {@link Integer#MIN_VALUE} if unknown.
     */
    int serviceTicketCount();
}
