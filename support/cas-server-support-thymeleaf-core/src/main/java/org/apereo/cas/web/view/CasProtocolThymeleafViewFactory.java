package org.apereo.cas.web.view;

import module java.base;
import org.apereo.cas.validation.CasProtocolViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.View;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * This is {@link CasProtocolThymeleafViewFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class CasProtocolThymeleafViewFactory implements CasProtocolViewFactory {
    private final SpringTemplateEngine springTemplateEngine;

    private final ThymeleafProperties thymeleafProperties;

    @Override
    public View create(final ConfigurableApplicationContext applicationContext,
                       final String viewName, final String contentType) {
        LOGGER.trace("Creating CAS protocol view [{}] with content type of [{}]", viewName, contentType);
        return new CasThymeleafView(viewName, applicationContext,
            springTemplateEngine, thymeleafProperties, contentType);
    }
}
