package org.apereo.cas.services.web.support;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.DefaultErrorViewResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * This is {@link MappedExceptionErrorViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class MappedExceptionErrorViewResolver extends DefaultErrorViewResolver {
    private final Map<Class<? extends Throwable>, ModelAndView> mappings;
    private final Function<ErrorContext, Optional<ModelAndView>> defaultFallback;

    public MappedExceptionErrorViewResolver(final ApplicationContext applicationContext,
                                            final WebProperties.Resources resources,
                                            final Map<Class<? extends Throwable>, ModelAndView> mappings,
                                            final Function<ErrorContext, Optional<ModelAndView>> defaultFallback) {
        super(applicationContext, resources);
        this.mappings = Map.copyOf(mappings);
        this.defaultFallback = defaultFallback;
        setOrder(100);
    }

    @Override
    public ModelAndView resolveErrorView(final HttpServletRequest request,
                                         final HttpStatus status,
                                         final Map<String, Object> map) {
        val errorContext = new ErrorContext(request, status, map);
        val defaultModelAndView = defaultFallback.apply(errorContext)
            .orElseGet(() -> super.resolveErrorView(request, status, map));
        val exception = (Exception) request.getAttribute("jakarta.servlet.error.exception");
        if (exception != null) {
            return mappings.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isAssignableFrom(exception.getClass())
                    || (exception.getCause() != null && entry.getKey().isAssignableFrom(exception.getCause().getClass())))
                .map(Map.Entry::getValue)
                .peek(mv -> mv.getModelMap().putAll(CollectionUtils.wrap(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION, exception)))
                .findFirst()
                .orElse(defaultModelAndView);
        }
        return defaultModelAndView;
    }

    public record ErrorContext(HttpServletRequest request, HttpStatus status, Map<String, Object> map) {}
}
