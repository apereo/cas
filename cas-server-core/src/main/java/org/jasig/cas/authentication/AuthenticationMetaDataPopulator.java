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

import org.jasig.cas.authentication.principal.Credentials;

/**
 * An extension point to the Authentication process that allows CAS to provide
 * additional attributes related to the overall Authentication (such as
 * authentication type) that are specific to the Authentication request versus
 * the Principal itself. AuthenticationAttributePopulators are a new feature in
 * CAS3. In order for an installation to be CAS2 compliant, deployers do not
 * need an AuthenticationMetaDataPopulator.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface AuthenticationMetaDataPopulator {

    /**
     * Provided with an Authentication object and the original credentials
     * presented, provide any additional attributes to the Authentication
     * object. Implementations have the option of returning the same
     * Authentication object, or a new one.
     * 
     * @param authentication The Authentication to potentially augment with
     * additional attributes.
     * @return the original Authentication object or a new Authentication
     * object.
     */
    Authentication populateAttributes(Authentication authentication,
        Credentials credentials);
}
