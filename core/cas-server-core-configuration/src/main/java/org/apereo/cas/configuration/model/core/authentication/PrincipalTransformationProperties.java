package org.apereo.cas.configuration.model.core.authentication;

import java.io.Serializable;

/**
 * This is {@link PrincipalTransformationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class PrincipalTransformationProperties implements Serializable {

    private static final long serialVersionUID = 1678602647607236322L;

    public enum CaseConversion {
        /** No conversion. */
        NONE,
        /** Lowercase conversion. */
        UPPERCASE,
        /** Uppcase conversion. */
        LOWERCASE,
    }
    private String prefix;
    private String suffix;
    private CaseConversion caseConversion = CaseConversion.NONE;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    public CaseConversion getCaseConversion() {
        return caseConversion;
    }

    public void setCaseConversion(final CaseConversion caseConversion) {
        this.caseConversion = caseConversion;
    }
}
