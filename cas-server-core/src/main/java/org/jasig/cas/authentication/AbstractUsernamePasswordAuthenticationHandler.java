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

import java.security.GeneralSecurityException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.support.NoOpPrincipalNameTransformer;
import org.jasig.cas.authentication.support.PasswordEncoder;
import org.jasig.cas.authentication.support.PlainTextPasswordEncoder;
import org.jasig.cas.authentication.support.PrincipalNameTransformer;

/**
 * Abstract class to override supports so that we don't need to duplicate the
 * check for UsernamePasswordCredential.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public abstract class AbstractUsernamePasswordAuthenticationHandler extends
    AbstractPreAndPostProcessingAuthenticationHandler {

    /** Default class to support if one is not supplied. */
    private static final Class<UsernamePasswordCredential> DEFAULT_CLASS = UsernamePasswordCredential.class;

    /** Class that this instance will support. */
    @NotNull
    private Class< ? > classToSupport = DEFAULT_CLASS;

    /**
     * Boolean to determine whether to support subclasses of the class to
     * support.
     */
    private boolean supportSubClasses = true;

    /**
     * PasswordEncoder to be used by subclasses to encode passwords for
     * comparing against a resource.
     */
    @NotNull
    private PasswordEncoder passwordEncoder = new PlainTextPasswordEncoder();

    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    /**
     * Method automatically handles conversion to UsernamePasswordCredential
     * and delegates to abstract authenticateUsernamePasswordInternal so
     * subclasses do not need to cast.
     */
    protected final HandlerResult doAuthentication(final Credential credential)
            throws GeneralSecurityException, PreventedException {
        return authenticateUsernamePasswordInternal((UsernamePasswordCredential) credential);
    }

    /**
     * Abstract convenience method that assumes the credential passed in are a
     * subclass of UsernamePasswordCredential.
     * 
     *
     * @param credential Username/password credential to authenticate.
     *
     * @return Authentication handler result on authentication success.
     *
     * @throws GeneralSecurityException On authentication failures where the root cause is security related,
     * e.g. invalid credential.
     * @throws PreventedException On errors that prevented authentication from occurring.
     */
    protected abstract HandlerResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException;

    /**
     * Method to return the PasswordEncoder to be used to encode passwords.
     * 
     * @return the PasswordEncoder associated with this class.
     */
    protected final PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    protected final PrincipalNameTransformer getPrincipalNameTransformer() {
        return this.principalNameTransformer;
    }

    /**
     * Method to set the class to support.
     * 
     * @param classToSupport the class we want this handler to support
     * explicitly.
     */
    public final void setClassToSupport(final Class< ? > classToSupport) {
        this.classToSupport = classToSupport;
    }

    /**
     * Method to set whether this handler will support subclasses of the
     * supported class.
     * 
     * @param supportSubClasses boolean of whether to support subclasses or not.
     */
    public final void setSupportSubClasses(final boolean supportSubClasses) {
        this.supportSubClasses = supportSubClasses;
    }

    /**
     * Sets the PasswordEncoder to be used with this class.
     * 
     * @param passwordEncoder the PasswordEncoder to use when encoding
     * passwords.
     */
    public final void setPasswordEncoder(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public final void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    /**
     * @return true if the credential are not null and the credential class is
     * equal to the class defined in classToSupport.
     */
    public final boolean supports(final Credential credential) {
        return credential != null
            && (this.classToSupport.equals(credential.getClass()) || (this.classToSupport
                .isAssignableFrom(credential.getClass()))
                && this.supportSubClasses);
    }
}
