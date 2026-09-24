package org.apereo.cas.services.web.support;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webmvc.autoconfigure.error.DefaultErrorViewResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

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
    public ModelAndView resolveErrorView(final @NonNull HttpServletRequest request,
                                         final @NonNull HttpStatus status,
                                         final @NonNull Map<String, Object> map) {
        val errorContext = new ErrorContext(request, status, map);
        val defaultModelAndView = defaultFallback.apply(errorContext)
            .orElseGet(() -> super.resolveErrorView(request, status, map));
        val exception = (Exception) request.getAttribute("jakarta.servlet.error.exception");
        val rootCause = ExceptionUtils.getRootCause(exception);
        if (exception != null) {
            return mappings.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isAssignableFrom(exception.getClass())
                    || (rootCause != null && entry.getKey().isAssignableFrom(rootCause.getClass())))
                .min(Comparator.comparingInt(entry -> inheritanceDistance(entry.getKey(), exception, rootCause)))
                .map(Map.Entry::getValue)
                .map(mv -> {
                    mv.getModelMap().putAll(CollectionUtils.wrap(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION,
                        Objects.requireNonNullElse(rootCause, exception)));
                    return mv;
                })
                .orElse(defaultModelAndView);
        }
        return defaultModelAndView;
    }

    /**
     * How far the thrown exception is from a mapping, counted in superclasses.
     * <p>
     * Mappings overlap: {@code UnauthorizedServiceException} is a {@code RootCasException}, so both
     * match and the specific one has to win. The stream this feeds runs over an immutable map, whose
     * iteration order is salted per JVM, so taking the first match made the answer -- a 403 or a 400
     * for the same exception -- depend on which JVM happened to be running.
     * <p>
     * A mapping keyed on an interface is not reached by walking superclasses and therefore sorts
     * last; it still matches, as it did before, but a class mapping is preferred to it.
     *
     * @param mapped    the exception class a mapping is keyed on
     * @param exception the exception that was thrown
     * @param rootCause its root cause, if any
     * @return the number of superclasses between the two, or {@link Integer#MAX_VALUE}
     */
    private static int inheritanceDistance(final Class<? extends Throwable> mapped,
                                           final Throwable exception, final Throwable rootCause) {
        val direct = inheritanceDistance(mapped, exception.getClass());
        val viaRootCause = rootCause != null ? inheritanceDistance(mapped, rootCause.getClass()) : Integer.MAX_VALUE;
        return Math.min(direct, viaRootCause);
    }

    private static int inheritanceDistance(final Class<? extends Throwable> mapped, final Class<?> thrown) {
        var distance = 0;
        for (var current = thrown; current != null; current = current.getSuperclass()) {
            if (current.equals(mapped)) {
                return distance;
            }
            distance++;
        }
        return Integer.MAX_VALUE;
    }

    public record ErrorContext(HttpServletRequest request, HttpStatus status, Map<String, Object> map) {}
}
