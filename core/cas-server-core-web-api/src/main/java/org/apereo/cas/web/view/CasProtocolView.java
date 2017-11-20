package org.apereo.cas.web.view;

import org.apache.commons.lang3.StringUtils;
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
     * @param contentType        the content type
     */
    public CasProtocolView(final String templateName, final ApplicationContext applicationContext,
                           final SpringTemplateEngine templateEngine,
                           final ThymeleafProperties properties, final String contentType) {
        super(templateName);
        setApplicationContext(applicationContext);
        setTemplateEngine(templateEngine);
        setCharacterEncoding(properties.getEncoding().displayName());
        setLocale(Locale.getDefault());
        if (StringUtils.isNotBlank(contentType)) {
            setContentType(contentType);
        }
    }

    /**
     * Instantiates a new Cas protocol view.
     *
     * @param templateName       the template name
     * @param applicationContext the application context
     * @param templateEngine     the template engine
     * @param properties         the properties
     */
    public CasProtocolView(final String templateName, final ApplicationContext applicationContext,
                           final SpringTemplateEngine templateEngine,
                           final ThymeleafProperties properties) {
        this(templateName, applicationContext, templateEngine, properties, null);
    }

    @Override
    public String toString() {
        return getTemplateName();
    }
}
