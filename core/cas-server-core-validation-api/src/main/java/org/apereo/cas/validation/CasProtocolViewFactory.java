package org.apereo.cas.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.View;

/**
 * This is {@link CasProtocolViewFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@FunctionalInterface
public interface CasProtocolViewFactory {
    /**
     * Create view.
     *
     * @param applicationContext the application context
     * @param viewName           the view name
     * @param contentType        the content type
     * @return the view
     */
    View create(ConfigurableApplicationContext applicationContext,
                String viewName,
                String contentType);

    /**
     * Create view.
     *
     * @param applicationContext the application context
     * @param viewName           the view name
     * @return the view
     */
    default View create(final ConfigurableApplicationContext applicationContext, final String viewName) {
        return create(applicationContext, viewName, StringUtils.EMPTY);
    }
}
