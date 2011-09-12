package org.jasig.cas.util;

import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;

import javax.validation.*;
import java.lang.annotation.ElementType;


/**
 * Provides a custom {@link javax.validation.TraversableResolver} that should work in JPA2 environments without the JPA2
 * restrictions (i.e. getters for all properties).
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.4
 *
 */
public final class CustomBeanValidationPostProcessor extends BeanValidationPostProcessor {

    public CustomBeanValidationPostProcessor() {
        final Configuration configuration = Validation.byDefaultProvider().configure();
        configuration.traversableResolver(new TraversableResolver() {
            public boolean isReachable(final Object o, final Path.Node node, final Class<?> aClass, final Path path, final ElementType elementType) {
                return true;
            }

            public boolean isCascadable(final Object o, final Path.Node node, final Class<?> aClass, final Path path, final ElementType elementType) {
                return true;
            }
        });

        final Validator validator = configuration.buildValidatorFactory().getValidator();
        setValidator(validator);
    }
}
