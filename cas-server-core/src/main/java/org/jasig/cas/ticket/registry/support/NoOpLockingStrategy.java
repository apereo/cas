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
package org.jasig.cas.ticket.registry.support;


/**
 * No-Op locking strategy that allows the use of {@link DefaultTicketRegistryCleaner}
 * in environments where exclusive access to the registry for cleaning is either
 * unnecessary or not possible.
 *
 * @author Marvin Addison
 * @since 3.3.6
 *
 */
public class NoOpLockingStrategy implements LockingStrategy {

    /**
     * {@inheritDoc}
     * @see org.jasig.cas.ticket.registry.support.LockingStrategy#acquire()
     */
    @Override
    public boolean acquire() {
        return true;
    }

    /**
     * @see org.jasig.cas.ticket.registry.support.LockingStrategy#release()
     */
    public void release() {
        // Nothing to release
    }

}
