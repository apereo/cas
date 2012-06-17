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
package org.jasig.cas.adaptors.ldap;


/**
 * Contains test data to facilitate customizing LDAP bind authentication
 * tests for arbitrary LDAP environment.
 *
 * @author marvin
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class BindTestConfig {
    /** Name of credential that exists in target LDAP */
    private String existsCredential;
   
    /** Correct password for test credential */
    private String existsSuccessPassword;
    
    /** Incorrect password for test credential */
    private String existsFailurePassword;
  
    /** A credential that does not exist in the target LDAP */
    private String notExistsCredential;

    
    /**
     * @return Returns the existsCredential.
     */
    public String getExistsCredential() {
        return this.existsCredential;
    }

    
    /**
     * @param existsCredential The existsCredential to set.
     */
    public void setExistsCredential(String existsCredential) {
        this.existsCredential = existsCredential;
    }

    
    /**
     * @return Returns the existsSuccessPassword.
     */
    public String getExistsSuccessPassword() {
        return this.existsSuccessPassword;
    }

    
    /**
     * @param existsSuccessPassword The existsSuccessPassword to set.
     */
    public void setExistsSuccessPassword(String existsSuccessPassword) {
        this.existsSuccessPassword = existsSuccessPassword;
    }

    
    /**
     * @return Returns the existsFailurePassword.
     */
    public String getExistsFailurePassword() {
        return this.existsFailurePassword;
    }

    
    /**
     * @param existsFailurePassword The existsFailurePassword to set.
     */
    public void setExistsFailurePassword(String existsFailurePassword) {
        this.existsFailurePassword = existsFailurePassword;
    }

    
    /**
     * @return Returns the notExistsCredential.
     */
    public String getNotExistsCredential() {
        return this.notExistsCredential;
    }

    
    /**
     * @param notExistsCredential The notExistsCredential to set.
     */
    public void setNotExistsCredential(String notExistsCredential) {
        this.notExistsCredential = notExistsCredential;
    }



}
