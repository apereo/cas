package org.apereo.cas.util.serialization;

import org.apereo.cas.util.model.TriStateBoolean;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link JacksonObjectMapperFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@Getter
public class JacksonObjectMapperFactory {
    private final boolean defaultTypingEnabled;

    private final boolean failOnUnknownProperties;

    private final boolean singleValueAsArray;

    private final boolean singleArrayElementUnwrapped;

    private final boolean writeDatesAsTimestamps;

    @Builder.Default
    private final boolean defaultViewInclusion = true;

    @Builder.Default
    private final Map<String, Object> injectableValues = new LinkedHashMap<>();

    private final JsonFactory jsonFactory;

    /**
     * Produce an object mapper for YAML serialization/de-serialization.
     *
     * @return the object mapper
     */
    public ObjectMapper toObjectMapper() {
        return initialize(new ObjectMapper(this.jsonFactory));
    }

    /**
     * Initialize.
     *
     * @param mapper the mapper
     * @return the object mapper
     */
    protected ObjectMapper initialize(final ObjectMapper mapper) {
        mapper
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, isSingleArrayElementUnwrapped())
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, isDefaultViewInclusion())

            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, isFailOnUnknownProperties())
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, isSingleValueAsArray())

            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, isWriteDatesAsTimestamps())

            .setInjectableValues(new RelaxedInjectableValueProvider(getInjectableValues()))

            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
            .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
            .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
            .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
            .findAndRegisterModules();

        if (isDefaultTypingEnabled()) {
            mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        }

        return mapper;
    }

    public static class RelaxedInjectableValueProvider extends InjectableValues.Std {
        private static final long serialVersionUID = -7327438202032303292L;

        public RelaxedInjectableValueProvider(final Map<String, Object> values) {
            super(values);
        }

        @Override
        public Object findInjectableValue(final Object valueId, final DeserializationContext ctxt,
                                          final BeanProperty beanProperty, final Object beanInstance) {
            val key = valueId.toString();
            val valueToReturn = this._values.get(key);

            val wrapper = new DirectFieldAccessFallbackBeanWrapper(beanInstance);
            if (!this._values.containsKey(key)) {
                return wrapper.getPropertyValue(key);
            }
            val propType = wrapper.getPropertyType(key);
            if (propType.equals(TriStateBoolean.class)) {
                return TriStateBoolean.valueOf(valueToReturn.toString());
            }
            return valueToReturn;
        }
    }
}
