package org.apereo.cas.metadata;

import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.bind.AbstractBindHandler;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DeprecatedElementsBindHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
@Getter
class DeprecatedElementsBindHandler extends AbstractBindHandler {
    private final List<ConfigurationProperty> deprecatedProperties = new ArrayList<>();

    @Override
    public Object onSuccess(final ConfigurationPropertyName name, final Bindable<?> target,
                            final BindContext context, final Object result) {
        FunctionUtils.doUnchecked(__ -> {
            if (result instanceof final Enum enumeration) {
                val field = enumeration.getDeclaringClass().getField(enumeration.name());
                if (field.isAnnotationPresent(Deprecated.class)) {
                    deprecatedProperties.add(context.getConfigurationProperty());
                }
            } else if (target.getAnnotation(Deprecated.class) != null) {
                val property = context.getConfigurationProperty() != null
                    ? context.getConfigurationProperty()
                    : new ConfigurationProperty(name, "N/A", null);
                deprecatedProperties.add(property);
            }
        });
        return super.onSuccess(name, target, context, result);
    }
}
