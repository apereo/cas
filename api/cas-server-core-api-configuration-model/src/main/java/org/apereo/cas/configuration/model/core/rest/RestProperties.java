package org.apereo.cas.configuration.model.core.rest;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link RestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-rest", automated = true)
@Getter
@Setter
public class RestProperties implements Serializable {

    private static final long serialVersionUID = -1833107478273171342L;

    /**
     * Authorization attribute name required by the REST endpoint in order to allow for the requested operation.
     * Attribute must be resolvable by the authenticated principal, or must have been already.
     */
    private String attributeName;

    /**
     * Matching authorization attribute value, pulled from the attribute
     * required by the REST endpoint in order to allow for the requested operation.
     * The attribute value may also be constructed as a regex pattern.
     */
    private String attributeValue;

    /**
     * Usage Warning!
     * The X.509 feature over REST provides a tremendously convenient target for claiming user identities. To
     * securely use this feature, network configuration MUST allow connections to the CAS server only from
     * trusted hosts which in turn have strict security limitations and logging.
     * 
     * this defaults to false, i'd make it explicit but checkstyle complains
     */
    private boolean x509insecure;
}
