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
package org.jasig.cas.ticket.registry.support;

/**
 * Strategy pattern for defining a locking strategy in support of exclusive
 * execution of some process.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.3.6
 *
 */
public interface LockingStrategy {

    /**
     * Attempt to acquire the lock.
     *
     * @return  True if lock was successfully acquired, false otherwise.
     */
    boolean acquire();


    /**
     * Release the lock if held.  If the lock is not held nothing is done.
     */
    void release();
}
