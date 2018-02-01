package org.apereo.cas.configuration.model.support.ldap;

import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link DnAttributeSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
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
