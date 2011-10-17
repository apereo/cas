/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.handler.AuthenticationException;
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
     * @throws AuthenticationException An AuthenticationException can contain
     * details about why a particular meta data populator failed.
     */
    Authentication populateAttributes(Authentication authentication,
        Credentials credentials) throws AuthenticationException;
}
