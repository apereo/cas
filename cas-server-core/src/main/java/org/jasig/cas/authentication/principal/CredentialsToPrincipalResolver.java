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
 * CredentialsToPrincipalResolvers extract information from the Credentials
 * provided and determine the Principal represented by those credentials.
 * <p>
 * A minimal Principal object just has one ID value. This can be extended with
 * richer objects containing more properties. The SimplePrincipal class
 * implementing this interface just stores a userid.
 * </p>
 * <p>
 * The Credentials typically contains a userid typed by the user or a
 * Certificate presented by the browser. In the simplest case the userid is
 * stored as the Principal ID. The Certificate is a more complicated case
 * because the ID may have to be extracted from the Subject DN or from one of
 * the alternate subject names. In a few cases, the institution may prefer the
 * ID to be a student or employee ID number that can only be obtained by
 * database lookup using information supplied in the Credentials.
 * </p>
 * <p>
 * The Resolver is free to obtain additional information about the user and
 * place it in the fields of a class that extends Principal. Such extended
 * information will be stored like other Principal objects in the TGT, persisted
 * as needed, and will be available to the View layer, but it is transparent to
 * most CAS processing.
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.2 $ $Date: 2007/01/22 20:35:27 $
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 * @see org.jasig.cas.authentication.principal.Principal
 * @see org.jasig.cas.authentication.principal.Credentials
 */
public interface CredentialsToPrincipalResolver {

    /**
     * Turn Credentials into a Principal object by analyzing the information
     * provided in the Credentials and constructing a Principal object based on
     * that information or information derived from the Credentials object.
     * 
     * @param credentials from which to resolve Principal
     * @return resolved Principal, or null if the principal could not be resolved.
     */
    Principal resolvePrincipal(Credentials credentials);

    /**
     * Determine if a credentials type is supported by this resolver. This is
     * checked before calling resolve principal.
     * 
     * @param credentials The credentials to check if we support.
     * @return true if we support these credentials, false otherwise.
     */
    boolean supports(Credentials credentials);
}
