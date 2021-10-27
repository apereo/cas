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
package org.jasig.cas.authentication.handler;

/**
 * Interface to provide a standard way to translate a plaintext password into a
 * different representation of that password so that the password may be
 * compared with the stored encrypted password without having to decode the
 * encrypted password.
 * <p>
 * PasswordEncoders are useful because often the stored passwords are encoded
 * with a one way hash function which makes them almost impossible to decode.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface PasswordEncoder {

    /**
     * Method that actually performs the transformation of the plaintext
     * password into the encrypted password.
     * 
     * @param password the password to translate
     * @return the transformed version of the password
     */
    String encode(String password);
}
