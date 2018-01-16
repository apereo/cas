package org.apereo.cas.configuration.model.support.ldap;

import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link MergeAttributesSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
public class MergeAttributesSearchEntryHandlersProperties implements Serializable {

    private static final long serialVersionUID = -3988972992084584349L;

    /**
     * The Merge attribute name.
     */
    private String mergeAttributeName;

    /**
     * The Attribute names.
     */
    private List<String> attributeNames;
}
