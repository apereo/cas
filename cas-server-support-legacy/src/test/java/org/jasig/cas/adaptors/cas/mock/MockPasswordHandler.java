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

package org.jasig.cas.adaptors.cas.mock;

import javax.servlet.ServletRequest;

import edu.yale.its.tp.cas.auth.PasswordHandler;

/**
 * Mock PasswordHandler implementation in support of testcases.
 * 
 * @version $Revision$ $Date$
 */
public class MockPasswordHandler implements PasswordHandler {

    /**
     * Value this object will return on invocation of its interface method.
     */
    private boolean succeed;

    private ServletRequest request;

    private String username;

    private String password;

    public boolean authenticate(ServletRequest requestArg, String usernameArg,
        String passwordArg) {
        this.request = requestArg;
        this.username = usernameArg;
        this.password = passwordArg;
        return this.succeed;
    }

    /**
     * Return the value this object will return on invocation of its interface
     * method.
     * 
     * @return Returns the succeeed.
     */
    public boolean isSucceed() {
        return this.succeed;
    }

    /**
     * Set the value this object will return on invocation of its interface
     * method.
     * 
     * @param succeeed The succeeed to set.
     */
    public void setSucceed(boolean succeeed) {
        this.succeed = succeeed;
    }

    /**
     * Get the username most recently presented to the interface method.
     * 
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Get the ServletRequest most recently presented to the interface method.
     * 
     * @return Returns the request.
     */
    public ServletRequest getRequest() {
        return this.request;
    }

    /**
     * Get the username most recently presented to the interface method.
     * 
     * @return Returns the username.
     */
    public String getUsername() {
        return this.username;
    }
}
