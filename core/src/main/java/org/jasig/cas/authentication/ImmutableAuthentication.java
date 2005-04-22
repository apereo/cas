/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Principal;

/**
 * The implementation of Authentation returned by AuthenticationManagerImpl.
 * 
 * <p>All property values must be set in the constructor and then may not
 * be changed.
 * Serializable since it is tied to the TGT and may be persisted.
 * </p>
 * 
 * 
 * 
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ImmutableAuthentication implements Authentication {

    /** UID for serializing. */
    private static final long serialVersionUID = 3906647483978365235L;

    /** A Principal object returned by the CredentialsToPrincipalResolver */
    private final Principal principal; // TODO refactor to be an immutable

    // proxy?

    /** The date/time this authentication object became valid. */
    private final Date authenticatedDate;

    /** Associated authentication attributes. Initially empty but may
     * be filled in by an AuthenticationAttributesPopulator */
    private final Map attributes;

	/**
	 * Only constructor, must provide values for properties.
	 * @param principal Principal representing user
	 * @param attributes Authentication attributes map.
	 */
    protected ImmutableAuthentication(final Principal principal,
        final Map attributes) {
        this.principal = principal;
        this.attributes = Collections
            .unmodifiableMap(attributes == null ? new HashMap() : attributes);
        this.authenticatedDate = new Date();
    }

    public Principal getPrincipal() {
        return this.principal;
    }

    public Date getAuthenticatedDate() {
        return new Date(this.authenticatedDate.getTime());
    }

    public Map getAttributes() {
        return this.attributes;
    }

    public boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
