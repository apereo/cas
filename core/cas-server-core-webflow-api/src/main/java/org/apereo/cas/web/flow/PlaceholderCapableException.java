package org.apereo.cas.web.flow;

/**
 * Perform placeHolder for i18n when handle authentication exceptions.
 *
 * @author Qimiao Chen
 * @since 6.0.0
 */

public interface PlaceholderCapableException {
    /**
     * Get all args.
     * 
     * @return Object[] the args
     */
    Object[] getArgs();

    /**
     * Set all args.
     *
     * @param args the args
     */
    void setArgs(Object... args);
}
