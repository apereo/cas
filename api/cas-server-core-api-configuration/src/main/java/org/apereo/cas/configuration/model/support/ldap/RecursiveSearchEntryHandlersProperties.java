package org.apereo.cas.configuration.model.support.ldap;

import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link RecursiveSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
public class RecursiveSearchEntryHandlersProperties implements Serializable {

    private static final long serialVersionUID = 7038108925310792763L;

    /**
     * The Search attribute.
     */
    private String searchAttribute;

    /**
     * The Merge attributes.
     */
    private List<String> mergeAttributes;
}
