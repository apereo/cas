package org.thymeleaf.spring6.expression;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    }

    public String code(final String code) {
        LOGGER.info("Theme name {}", theme.getName());
        LOGGER.info("Theme class {}, code {}, locale {}", theme.getClass().getSimpleName(), code, this.locale);
        final MessageSource messageSource = theme.getMessageSource();
        LOGGER.info("Theme message source {}", messageSource.toString());
        val message = messageSource.getMessage(code, null, "*** Unknown ***", this.locale);
        LOGGER.info("Found value [{}] for code [{}] and locale [{}]", message, code, this.locale);
        return message;
    }
}
