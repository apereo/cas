package org.apereo.cas.web.view;

import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafView;

import java.util.Locale;

/**
 * This is {@link CasProtocolView}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasProtocolView extends ThymeleafView {

    /**
     * Instantiates a new Cas protocol view.
     *
     * @param templateName       the template name
     * @param applicationContext the application context
     * @param templateEngine     the template engine
     * @param properties         the properties
     */
    public CasProtocolView(final String templateName, final ApplicationContext applicationContext,
                           final SpringTemplateEngine templateEngine, final ThymeleafProperties properties) {
        super(templateName);
        setApplicationContext(applicationContext);
        setTemplateEngine(templateEngine);
        setCharacterEncoding(properties.getEncoding().displayName());
        setLocale(Locale.getDefault());
    }

    @Override
    public String toString() {
        return getTemplateName();
    }
}
