package org.apereo.cas.web.flow;

/**
 * Perform placeHolder for i18n when handle authentication exceptions
 *
 * @author Qimiao Chen
 * @since 6.0.0
 */

public interface PlaceholderCapableException {

    Object[] getArgs();

    void setArgs(Object... args);
}