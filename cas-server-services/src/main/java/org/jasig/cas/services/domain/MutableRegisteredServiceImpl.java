/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Mutable implementation of the RegisteredService interface.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class MutableRegisteredServiceImpl implements RegisteredService,
    Serializable {

    /** Unique id for serialization. */
    private static final long serialVersionUID = -726751356116986525L;

    /** The unique simple id for the service. */
    private String id;

    /** The url that matches the service url. */
    private String url;

    /** Is this service allowed to proxy? */
    private boolean allowedToProxy;

    /** Is this service enable? */
    private boolean enabled;

    /** Can this service participate in the SSO? */
    private boolean ssoParticipant;

    /** Mutable internal list to store attributee names. */
    private Set allowedAttributeNames = new HashSet();

    private String name;

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean isAllowedToProxy() {
        return this.allowedToProxy;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isSsoParticipant() {
        return this.ssoParticipant;
    }

    public void setAllowedToProxy(final boolean allowedToProxy) {
        this.allowedToProxy = allowedToProxy;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setSsoParticipant(final boolean ssoParticipant) {
        this.ssoParticipant = ssoParticipant;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String[] getAllowedAttributeNames() {
        return (String[]) this.allowedAttributeNames
            .toArray(new String[this.allowedAttributeNames.size()]);
    }

    public Set getAttributeNames() {
        return this.allowedAttributeNames;
    }

    public void setAttributeNames(final Set set) {
        this.allowedAttributeNames = set;
    }
}
