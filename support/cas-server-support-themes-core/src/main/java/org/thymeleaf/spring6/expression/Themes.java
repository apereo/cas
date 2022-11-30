package org.thymeleaf.spring6.expression;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.ui.context.Theme;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.spring6.context.IThymeleafRequestContext;
import org.thymeleaf.spring6.context.SpringContextUtils;

import java.util.Locale;

/**
 * This is {@link Themes}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class Themes {
    private final Theme theme;
    private final Locale locale;

    public Themes(final IExpressionContext context) {
        this.locale = context.getLocale();
        IThymeleafRequestContext requestContext = SpringContextUtils.getRequestContext(context);
        this.theme = requestContext != null ? requestContext.getTheme() : null;
        LOGGER.debug("Theme provided {}", theme);
    }

    public String code(final String code) {
        LOGGER.debug("Theme name {}", theme.getName());
        LOGGER.debug("Theme class {}, code {}", theme.getClass().getSimpleName(), code);
        var defaultMsg = "This is a default message";
        final MessageSource messageSource = theme.getMessageSource();
        LOGGER.debug("Theme message source {}", messageSource.toString());
        return messageSource.getMessage(code, null, defaultMsg, this.locale);
    }
}
