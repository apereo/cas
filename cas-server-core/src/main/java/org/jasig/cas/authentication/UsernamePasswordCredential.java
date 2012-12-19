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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * UsernamePasswordCredential respresents the username and password that a user
 * may provide in order to prove the authenticity of who they say they are.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.2 $ $Date: 2007/01/22 20:35:26 $
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public class UsernamePasswordCredential implements Credential {

    /** Unique ID for serialization. */
    private static final long serialVersionUID = -8343864967200862794L;

    /** The username. */
    @NotNull
    @Size(min=1,message = "required.username")
    private String username;

    /** The password. */
    @NotNull
    @Size(min=1, message = "required.password")
    private String password;

    /**
     * @return Returns the password.
     */
    public final String getPassword() {
        return this.password;
    }

    /**
     * @param password The password to set.
     */
    public final void setPassword(final String password) {
        this.password = password;
    }

    /**
     * @return Returns the userName.
     */
    public final String getUsername() {
        return this.username;
    }

    /**
     * @param userName The userName to set.
     */
    public final void setUsername(final String userName) {
        this.username = userName;
    }

    public String toString() {
        return this.username;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final UsernamePasswordCredential that = (UsernamePasswordCredential) o;

        if (this.password != null ? !this.password.equals(that.password) : that.password != null) return false;
        if (this.username != null ? !this.username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.username != null ? this.username.hashCode() : 0;
        result = 31 * result + (this.password != null ? this.password.hashCode() : 0);
        return result;
    }
}
