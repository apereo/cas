package org.apereo.cas.web.flow.resolver;

import module java.base;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.mapping.MappingResults;
import org.springframework.binding.mapping.impl.DefaultMapper;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.servlet.View;
import org.springframework.webflow.core.collection.LocalParameterMap;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.servlet.ServletMvcView;
import org.springframework.webflow.mvc.servlet.ServletMvcViewFactory;
import org.springframework.webflow.mvc.view.AbstractMvcView;
import org.springframework.webflow.mvc.view.AbstractMvcViewFactory;
import org.springframework.webflow.mvc.view.FlowViewResolver;
import org.springframework.webflow.validation.WebFlowMessageCodesResolver;

/**
 * This is {@link CasMvcViewFactoryCreator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasMvcViewFactoryCreator extends MvcViewFactoryCreator {
    @Override
    protected AbstractMvcViewFactory createMvcViewFactory(final Expression viewId,
                                                          final ExpressionParser expressionParser,
                                                          final ConversionService conversionService,
                                                          final BinderConfiguration binderConfiguration) {
        val field = ReflectionUtils.findField(MvcViewFactoryCreator.class, "flowViewResolver", FlowViewResolver.class);
        Objects.requireNonNull(field).trySetAccessible();
        val flowViewResolver = (FlowViewResolver) ReflectionUtils.getField(field, this);
        return new CasServletMvcViewFactory(viewId, flowViewResolver,
            expressionParser, conversionService, binderConfiguration,
            new WebFlowMessageCodesResolver());
    }

    private static class CasServletMvcViewFactory extends ServletMvcViewFactory {

        CasServletMvcViewFactory(final Expression viewId,
                                 final FlowViewResolver viewResolver,
                                 final ExpressionParser expressionParser,
                                 final ConversionService conversionService,
                                 final BinderConfiguration binderConfiguration,
                                 final MessageCodesResolver messageCodesResolver) {
            super(viewId, viewResolver, expressionParser, conversionService,
                binderConfiguration, messageCodesResolver);
        }

        @Override
        protected AbstractMvcView createMvcView(final View view, final RequestContext context) {
            return new CasServletMvcView(view, context);
        }
    }

    private static class CasServletMvcView extends ServletMvcView {

        CasServletMvcView(final View view, final RequestContext context) {
            super(view, context);
        }

        @Override
        protected String determineEventId(final RequestContext context) {
            var eventId = super.determineEventId(context);
            if (StringUtils.isBlank(eventId)) {
                val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
                eventId = (String) request.getAttribute("_eventId");
            }
            return eventId;
        }

        @Override
        protected MappingResults bind(final Object model) {
            val initialMappingResults = super.bind(model);

            if (initialMappingResults.getAllResults().isEmpty() && !initialMappingResults.hasErrorResults()) {
                val mapper = new DefaultMapper();
                val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(getRequestContext());
                val spliterator = Spliterators.spliteratorUnknownSize(request.getAttributeNames().asIterator(), 0);

                val parameterNames = new LocalParameterMap(StreamSupport.stream(spliterator, false)
                    .filter(attribute -> !attribute.contains("."))
                    .filter(attribute -> Objects.nonNull(request.getAttribute(attribute)))
                    .collect(Collectors.toMap(Function.identity(), request::getAttribute)));
                
                addModelBindings(mapper, parameterNames.asMap().keySet(), model);
                return mapper.map(parameterNames, model);
            }
            return initialMappingResults;
        }

    }
}
