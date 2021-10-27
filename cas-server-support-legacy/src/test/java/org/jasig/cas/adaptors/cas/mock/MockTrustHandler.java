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

import edu.yale.its.tp.cas.auth.TrustHandler;

/**
 * Mock object in support of testcases involving TrustHandler.
 * 
 * @version $Revision$ $Date$
 */
public class MockTrustHandler implements TrustHandler {

    /**
     * The value this mock object will return to the getUsername(Request)
     * interface method.
     */
    private String userName;

    /**
     * Record the argument given in the most recent invocation of
     * getUsername(ServletRequest request).
     */
    private ServletRequest request;

    /**
     * Get the username String this object will return on invocations of the
     * getUsername(Request) interface method.
     * 
     * @return Returns the userName.
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Set the username String tghis object should return on invocations of the
     * getUsername(Request) method.
     * 
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUsername(ServletRequest requestArg) {
        /*
         * Dummy implementation simply records the request and then returns the
         * configured username.
         */

        this.request = requestArg;
        return this.userName;
    }

    /**
     * Get the ServletRequest recorded from the most recent invocation of
     * getUsername(Request).
     * 
     * @return Returns the request.
     */
    public ServletRequest getRequest() {
        return this.request;
    }
}
