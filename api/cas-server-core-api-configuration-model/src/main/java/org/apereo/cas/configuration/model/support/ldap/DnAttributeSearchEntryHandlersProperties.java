package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link DnAttributeSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-ldap")
@Getter
@Setter
@Accessors(chain = true)
public class DnAttributeSearchEntryHandlersProperties implements Serializable {

    private static final long serialVersionUID = -1174594647679213858L;

    /**
     * The Dn attribute name.
     */
    private String dnAttributeName = "entryDN";

    /**
     * The Add if exists.
     */
    private boolean addIfExists;
}
