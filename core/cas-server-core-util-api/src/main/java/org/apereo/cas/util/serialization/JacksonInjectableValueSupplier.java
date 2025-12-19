package org.apereo.cas.util.serialization;

import module java.base;
import org.apereo.cas.configuration.support.TriStateBoolean;
import lombok.val;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.InjectableValues;

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
    public Object findInjectableValue(final DeserializationContext ctxt, final Object valueId,
                                      final BeanProperty forProperty, final Object beanInstance,
                                      final Boolean optional, final Boolean useInput) {
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
