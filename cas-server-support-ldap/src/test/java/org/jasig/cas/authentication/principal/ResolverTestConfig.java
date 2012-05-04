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
package org.jasig.cas.authentication.principal;


/**
 * Container for test properties used by LDAP resolver.
 *
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class ResolverTestConfig {

    private String existsCredential;

    private String existsPrincipal;
    
    private String notExistsCredential;
    
    private String tooManyCredential;

    
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
     * @param existsPrincipal The existsPrincipal to set.
     */
    public void setExistsPrincipal(String existsPrincipal) {
        this.existsPrincipal = existsPrincipal;
    }


    /**
     * @return Returns the existsPrincipal.
     */
    public String getExistsPrincipal() {
        return this.existsPrincipal;
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

    
    /**
     * @return Returns the tooManyCredential.
     */
    public String getTooManyCredential() {
        return this.tooManyCredential;
    }

    
    /**
     * @param tooManyCredential The tooManyCredential to set.
     */
    public void setTooManyCredential(String tooManyCredential) {
        this.tooManyCredential = tooManyCredential;
    }
}