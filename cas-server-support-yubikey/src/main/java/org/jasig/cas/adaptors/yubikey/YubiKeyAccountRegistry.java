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

package org.jasig.cas.adaptors.yubikey;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
/**
 * General contract that allows one to determine whether
 * a particular YubiKey account
 * is allowed to participate in the authentication.
 * Accounts are noted by the username
 * and the public id of the YubiKey device.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface YubiKeyAccountRegistry {
    /**
     * Determines whether the registered
     * YubiKey public id is allowed for the <code>uid</code> received.
     * @param uid user id
     * @param yubikeyPublicId public id of the yubi id
     * @return true if the public id is allowed and registered for the uid.
     */
    boolean isYubiKeyRegisteredFor(String uid, String yubikeyPublicId);
}
