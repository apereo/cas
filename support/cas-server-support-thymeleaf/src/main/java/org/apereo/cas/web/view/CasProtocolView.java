package org.apereo.cas.web.view;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafView;

import java.util.Locale;

/**
 * This is {@link CasProtocolView}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasProtocolView extends ThymeleafView {

    public CasProtocolView(final String templateName, final ApplicationContext applicationContext,
                           final SpringTemplateEngine templateEngine, final ThymeleafProperties properties, final String contentType) {
        super(templateName);
        setApplicationContext(applicationContext);
        setTemplateEngine(templateEngine);
        setCharacterEncoding(properties.getEncoding().displayName());
        if (StringUtils.isNotBlank(contentType)) {
            setContentType(contentType);
        }
    }

    @Override
    public String toString() {
        return getTemplateName();
    }

    @Override
    protected Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }
}
