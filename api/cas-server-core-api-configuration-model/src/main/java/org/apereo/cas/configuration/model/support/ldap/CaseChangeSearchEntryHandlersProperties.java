package org.apereo.cas.configuration.model.support.ldap;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link CaseChangeSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
public class CaseChangeSearchEntryHandlersProperties implements Serializable {

    private static final long serialVersionUID = 2420895955116725666L;

    /**
     * The Dn case change.
     */
    private String dnCaseChange;

    /**
     * The Attribute name case change.
     */
    private String attributeNameCaseChange;

    /**
     * The Attribute value case change.
     */
    private String attributeValueCaseChange;

    /**
     * The Attribute names.
     */
    private List<String> attributeNames;
}
