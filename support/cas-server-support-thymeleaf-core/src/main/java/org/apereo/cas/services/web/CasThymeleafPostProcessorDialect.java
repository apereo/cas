package org.apereo.cas.services.web;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.dialect.IPostProcessorDialect;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.postprocessor.PostProcessor;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * This is {@link CasThymeleafPostProcessorDialect}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class CasThymeleafPostProcessorDialect implements IPostProcessorDialect {
    private final TemplateMode templateMode;

    @Override
    public int getDialectPostProcessorPrecedence() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Set<IPostProcessor> getPostProcessors() {
        return CollectionUtils.wrapSet(new PostProcessor(templateMode, CasThymeleafOutputTemplateHandler.class, Integer.MAX_VALUE));
    }

    @Override
    public String getName() {
        return CasThymeleafOutputTemplateHandler.class.getSimpleName();
    }
}

