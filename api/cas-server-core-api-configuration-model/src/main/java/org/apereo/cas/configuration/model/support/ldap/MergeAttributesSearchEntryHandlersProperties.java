package org.apereo.cas.configuration.model.support.ldap;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MergeAttributesSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-ldap")
@Getter
@Setter
@Accessors(chain = true)
public class MergeAttributesSearchEntryHandlersProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -3988972992084584349L;

    /**
     * The Merge attribute name.
     */
    private String mergeAttributeName;

    /**
     * The Attribute names.
     */
    private List<String> attributeNames = new ArrayList<>();
}
