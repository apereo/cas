package org.apereo.cas.nativex;

import org.apereo.cas.services.web.CasThymeleafOutputTemplateHandler;
import org.apereo.cas.services.web.CasThymeleafTemplatesDirector;
import org.apereo.cas.services.web.ThemeViewResolverFactory;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.view.CasMustacheView;
import org.apereo.cas.web.view.CasThymeleafView;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.context.ApplicationContextAware;
import org.thymeleaf.DialectConfiguration;
import org.thymeleaf.engine.IterationStatusVar;
import org.thymeleaf.engine.StandardModelFactory;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.standard.expression.FragmentExpression;
import org.thymeleaf.standard.processor.StandardTextTagProcessor;
import org.thymeleaf.standard.processor.StandardXmlNsTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;
import java.util.List;

/**
 * This is {@link CasThymeleafRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasThymeleafRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources()
            .registerPattern("cas-theme-*")
            .registerResourceBundle("messages");

        registerReflectionHints(hints, List.of(
            CasThymeleafTemplatesDirector.class,
            CasThymeleafOutputTemplateHandler.class,
            CasThymeleafView.class,
            CasMustacheView.class,
            TemplateMode.class,
            StandardXmlNsTagProcessor.class,
            IterationStatusVar.class,
            FragmentExpression.class,
            StandardTextTagProcessor.class,
            IOpenElementTag.class,
            ICloseElementTag.class,
            StandardModelFactory.class,
            DialectConfiguration.class
        ));

        registerReflectionHints(hints, List.of("org.thymeleaf.engine.Text"));
        registerSpringProxyHints(hints, ThemeViewResolverFactory.class, ApplicationContextAware.class);
    }
}
