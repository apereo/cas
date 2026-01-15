package org.apereo.cas.web.view;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.CasProtocolViewFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.mustache.servlet.view.MustacheViewResolver;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.View;

/**
 * This is {@link CasProtocolMustacheViewFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class CasProtocolMustacheViewFactory implements CasProtocolViewFactory {
    private final MustacheViewResolver mustacheViewResolver;

    @Override
    public View create(final ConfigurableApplicationContext applicationContext,
                       final String viewName, final String contentType) {
        return FunctionUtils.doUnchecked(() -> {
            mustacheViewResolver.setViewClass(CasMustacheView.class);
            return mustacheViewResolver.resolveViewName(viewName, Locale.ENGLISH);
        });
    }
}
