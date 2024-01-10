package org.apereo.cas.util.serialization;

import org.apereo.cas.configuration.support.TriStateBoolean;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import lombok.val;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import java.io.Serial;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * This is {@link JacksonInjectableValueSupplier}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class JacksonInjectableValueSupplier extends InjectableValues.Std {
    @Serial
    private static final long serialVersionUID = -7327438202032303292L;

    public JacksonInjectableValueSupplier(final Supplier<? extends Map<String, Object>> valueSupplier) {
        super(valueSupplier.get());
    }

    @Override
    public Object findInjectableValue(final Object valueId, final DeserializationContext deserializationContext,
                                      final BeanProperty beanProperty, final Object beanInstance) {
        val key = valueId.toString();
        val valueToReturn = this._values.get(key);

        val wrapper = new DirectFieldAccessFallbackBeanWrapper(beanInstance);
        if (!this._values.containsKey(key)) {
            return wrapper.getPropertyValue(key);
        }
        val propType = Objects.requireNonNull(wrapper.getPropertyType(key));
        if (propType.equals(TriStateBoolean.class)) {
            return TriStateBoolean.valueOf(valueToReturn.toString().toUpperCase(Locale.ENGLISH));
        }
        return valueToReturn;
    }
}
