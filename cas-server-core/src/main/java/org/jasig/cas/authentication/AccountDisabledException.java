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
package org.jasig.cas.authentication;

import java.io.Serializable;
import javax.security.auth.login.AccountException;

/**
 * Exception describes an authentication failure caused by a disabled user account. The conditions for
 * this exception are distinguised from {@link javax.security.auth.login.AccountLockedException} in that a locked
 * account MAY have been locked through actions caused by the user, whereas a disabled account is not capable of
 * authentication due to administrator action.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class AccountDisabledException extends AccountException implements Serializable {

    private static final long serialVersionUID = 3028978427657056083L;

    public AccountDisabledException() {
        super();
    }

    public AccountDisabledException(final String message) {
        super(message);
    }
}
