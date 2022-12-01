package org.apereo.cas.util.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final boolean useWrapperNameAsProperty = false;

    @Builder.Default
    private final Map<String, Object> injectableValues = new LinkedHashMap<>();

    private final JsonFactory jsonFactory;

    /**
     * Configure an existing mapper.
     *
     * @param applicationContext the application context
     * @param objectMapper       the mapper
     */
    public static void configure(
        final ConfigurableApplicationContext applicationContext,
        final ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
        val serializers = new ArrayList<>(applicationContext.getBeansOfType(JacksonObjectMapperCustomizer.class).values());
        AnnotationAwareOrderComparator.sort(serializers);
        val injectedValues = (Map) serializers
            .stream()
            .map(JacksonObjectMapperCustomizer::getInjectableValues)
            .flatMap(entry -> entry.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        objectMapper.setInjectableValues(new JacksonInjectableValueSupplier(() -> injectedValues));
    }

    /**
     * Produce an object mapper for YAML serialization/de-serialization.
     *
     * @return the object mapper
     */
    public ObjectMapper toObjectMapper() {
        val mapper = determineMapperInstance();
        return initialize(mapper);
    }

    /**
     * Initialize.
     *
     * @param mapper the mapper
     * @return the object mapper
     */
    protected ObjectMapper initialize(final MapperBuilder<?, ?> mapper) {
        val obm = mapper
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, isSingleArrayElementUnwrapped())
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, isDefaultViewInclusion())
            .configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, isUseWrapperNameAsProperty())

            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, isFailOnUnknownProperties())
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, isSingleValueAsArray())

            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, isWriteDatesAsTimestamps())
            .build();

        obm.setInjectableValues(new JacksonInjectableValueSupplier(this::getInjectableValues))
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
            .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
            .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
            .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
            .findAndRegisterModules();

        if (isDefaultTypingEnabled()) {
            obm.activateDefaultTyping(obm.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        }
        return obm;
    }

    private MapperBuilder<?, ?> determineMapperInstance() {
        if (jsonFactory instanceof YAMLFactory) {
            return YAMLMapper.builder((YAMLFactory) jsonFactory);
        }
        if (jsonFactory instanceof XmlFactory) {
            return XmlMapper.builder((XmlFactory) jsonFactory);
        }
        return JsonMapper.builder(jsonFactory);
    }
}
