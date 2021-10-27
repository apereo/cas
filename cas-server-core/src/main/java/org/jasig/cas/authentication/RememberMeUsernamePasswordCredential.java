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

/**
 * Handles both remember me services and username and password.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public class RememberMeUsernamePasswordCredential extends UsernamePasswordCredential implements RememberMeCredential, Serializable {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -6710007659431302397L;

    private boolean rememberMe;

    public final boolean isRememberMe() {
        return this.rememberMe;
    }

    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (this.rememberMe ? 1231 : 1237);
        return result;
    }

    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RememberMeUsernamePasswordCredential other = (RememberMeUsernamePasswordCredential) obj;
        if (this.rememberMe != other.rememberMe) {
            return false;
        }
        return true;
    }

    public final void setRememberMe(final boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
