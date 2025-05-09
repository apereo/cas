package org.apereo.cas.util.serialization;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.io.Serial;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
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
    private final boolean quoteFieldNames = true;

    @Builder.Default
    private final boolean useWrapperNameAsProperty = false;

    @Builder.Default
    private final Map<String, Object> injectableValues = new LinkedHashMap<>();

    @Builder.Default
    private final boolean minimal = false;

    @Builder.Default
    private final boolean sorted = false;

    private final JsonFactory jsonFactory;

    private final ConfigurableApplicationContext applicationContext;

    /**
     * Produce an object mapper for YAML serialization/de-serialization.
     *
     * @return the object mapper
     */
    public ObjectMapper toObjectMapper() {
        val mapper = determineMapperInstance();
        val objectMapper = initialize(mapper);
        
        val allCustomizers = getObjectMapperCustomizers();
        configureInjectableValues(allCustomizers, objectMapper);
        configureObjectMapperModules(objectMapper);
        allCustomizers.forEach(customizer -> customizer.customize(objectMapper));
        
        return objectMapper;
    }

    private void configureObjectMapperModules(final ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
        if (applicationContext != null) {
            objectMapper.registerModule(new DecodableCipherExecutorMapModule(applicationContext));
        }
    }

    private void configureInjectableValues(final List<JacksonObjectMapperCustomizer> allCustomizers, final ObjectMapper objectMapper) {
        val injectedValues = (Map) allCustomizers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(JacksonObjectMapperCustomizer::getInjectableValues)
            .flatMap(entry -> entry.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        injectedValues.putAll(this.injectableValues);
        objectMapper.setInjectableValues(new JacksonInjectableValueSupplier(() -> injectedValues));
    }

    private List<JacksonObjectMapperCustomizer> getObjectMapperCustomizers() {
        val customizers = ServiceLoader.load(JacksonObjectMapperCustomizer.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(BeanSupplier::isNotProxy)
            .collect(Collectors.toList());

        val effectiveContext = ObjectUtils.defaultIfNull(applicationContext, ApplicationContextProvider.getApplicationContext());
        if (effectiveContext != null) {
            val customizerBeans = effectiveContext.getBeansOfType(JacksonObjectMapperCustomizer.class).values();
            customizers.addAll(customizerBeans);
        }
        AnnotationAwareOrderComparator.sort(customizers);
        return customizers;
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

            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, sorted)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, sorted)

            .configure(JsonWriteFeature.QUOTE_FIELD_NAMES.mappedFeature(), isQuoteFieldNames())

            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, isFailOnUnknownProperties())
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, isSingleValueAsArray())
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, isQuoteFieldNames())

            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, isWriteDatesAsTimestamps())

            .build();

        obm
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
            .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
            .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
            .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
            .findAndRegisterModules()
            .registerModule(getCasJacksonModule())
            .registerModule(new JavaTimeModule())
            .registerModule(new ParameterNamesModule());

        if (isDefaultTypingEnabled()) {
            obm.activateDefaultTyping(obm.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        }

        if (minimal) {
            obm.setDefaultPrettyPrinter(new MinimalPrettyPrinter());
        }
        return obm;
    }

    private MapperBuilder<?, ?> determineMapperInstance() {
        if (jsonFactory instanceof final YAMLFactory factory) {
            return YAMLMapper.builder(factory);
        }
        if (jsonFactory instanceof final XmlFactory factory) {
            return XmlMapper.builder(factory);
        }
        return JsonMapper.builder(jsonFactory);
    }

    private static Module getCasJacksonModule() {
        val casModule = new SimpleModule();
        casModule.addDeserializer(URI.class, new URIDeserializer());
        return casModule;
    }

    private static final class URIDeserializer extends StdDeserializer<URI> {
        @Serial
        private static final long serialVersionUID = -7547162569192932415L;

        URIDeserializer() {
            this(null);
        }

        URIDeserializer(final Class<?> vc) {
            super(vc);
        }

        @Override
        public URI deserialize(final JsonParser jp, final DeserializationContext ctxt) {
            return FunctionUtils.doUnchecked(() -> {
                val value = SpringExpressionLanguageValueResolver.getInstance().resolve(jp.getText().trim());
                return StringUtils.isNotBlank(value) ? new URI(value) : null;
            });
        }
    }
}
