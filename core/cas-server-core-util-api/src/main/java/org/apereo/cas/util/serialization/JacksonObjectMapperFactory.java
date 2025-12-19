package org.apereo.cas.util.serialization;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.jackson.JacksonComponentModule;
import org.springframework.boot.jackson.JacksonMixinModule;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ReflectionUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TokenStreamFactory;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.json.JsonWriteFeature;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.MinimalPrettyPrinter;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.cbor.CBORFactory;
import tools.jackson.dataformat.cbor.CBORMapper;
import tools.jackson.dataformat.smile.SmileFactory;
import tools.jackson.dataformat.smile.SmileMapper;
import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;

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
    private final List<JacksonModule> modules = new ArrayList<>();

    @Builder.Default
    private final boolean minimal = false;

    @Builder.Default
    private final boolean sorted = false;

    @Builder.Default
    private final TokenStreamFactory jsonFactory = new JsonFactory();

    private final ConfigurableApplicationContext applicationContext;

    /**
     * Produce an object mapper for YAML serialization/de-serialization.
     *
     * @return the object mapper
     */
    public ObjectMapper toObjectMapper() {
        val builder = toBuilder();
        val mapper = builder.build();
        FunctionUtils.doUnchecked(_ -> {
            val field = ReflectionUtils.findField(mapper.getClass(), "_injectableValues");
            Objects.requireNonNull(field).trySetAccessible();
            field.set(mapper, builder.injectableValues());
        });
        return mapper;
    }

    /**
     * To json mapper.
     *
     * @return the json mapper
     */
    public JsonMapper toJsonMapper() {
        return (JsonMapper) toObjectMapper();
    }

    /**
     * To builder.
     *
     * @return the mapper builder
     */
    public MapperBuilder toBuilder() {
        val mapper = determineMapperInstance();
        var builder = initialize(mapper);

        val allCustomizers = getObjectMapperCustomizers();
        builder = configureInjectableValues(allCustomizers, builder);
        builder = configureObjectMapperModules(builder);
        for (val customizer : allCustomizers) {
            builder = customizer.customize(builder);
        }
        return builder;
    }

    private MapperBuilder configureObjectMapperModules(
        final MapperBuilder<?, ?> mapperBuilder) {
        if (applicationContext != null) {
            mapperBuilder.addModule(new DecodableCipherExecutorMapModule(applicationContext));
        }
        return mapperBuilder;
    }

    private MapperBuilder configureInjectableValues(final List<JacksonObjectMapperCustomizer> allCustomizers,
                                                    final MapperBuilder<?, ?> mapperBuilder) {
        val injectedValues = (Map) allCustomizers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(JacksonObjectMapperCustomizer::getInjectableValues)
            .flatMap(entry -> entry.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        injectedValues.putAll(this.injectableValues);

        return mapperBuilder.injectableValues(new JacksonInjectableValueSupplier(() -> injectedValues));
    }

    private List<JacksonObjectMapperCustomizer> getObjectMapperCustomizers() {
        val customizers = ServiceLoader.load(JacksonObjectMapperCustomizer.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(BeanSupplier::isNotProxy)
            .collect(Collectors.toList());

        val effectiveContext = ObjectUtils.getIfNull(applicationContext, ApplicationContextProvider.getApplicationContext());
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
     * @param mapperBuilder the mapper
     * @return the object mapper
     */
    protected MapperBuilder initialize(final MapperBuilder<?, ?> mapperBuilder) {
        var configuredBuilder = mapperBuilder
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, isSingleArrayElementUnwrapped())
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, sorted)

            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, isDefaultViewInclusion())
            .configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, isUseWrapperNameAsProperty())
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, sorted)

            .configure(EnumFeature.READ_ENUMS_USING_TO_STRING, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)

            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, isFailOnUnknownProperties())
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, isSingleValueAsArray())
            .configure(SerializationFeature.INDENT_OUTPUT, !minimal)

            .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, isWriteDatesAsTimestamps())
            .changeDefaultPropertyInclusion(handler -> handler.withValueInclusion(JsonInclude.Include.NON_DEFAULT)
                .withContentInclusion(JsonInclude.Include.NON_DEFAULT))
            .changeDefaultVisibility(handler -> handler.withSetterVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
                .withGetterVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC))
            .findAndAddModules()
            .addModules(this.modules)
            .addModule(new JacksonComponentModule())
            .addModule(new JacksonMixinModule())
            .addModule(getCasJacksonModule());

        if (jsonFactory instanceof JsonFactory) {
            configuredBuilder = configuredBuilder.defaultPrettyPrinter(
                minimal ? new MinimalPrettyPrinter() : new DefaultPrettyPrinter());
        }

        if (isDefaultTypingEnabled()) {
            val ptv = BasicPolymorphicTypeValidator.builder()
                .allowSubTypesWithExplicitDeserializer()
                .allowIfSubTypeIsArray()
                .allowIfBaseType("org.apereo.cas.")
                .allowIfBaseType("org.apereo.inspektr.")
                .allowIfBaseType("java.util.")
                .allowIfBaseType("java.lang.")
                .allowIfBaseType("java.time.")
                .allowIfBaseType("java.io.")
                .allowIfBaseType("com.nimbusds.oauth2.")
                .allowIfBaseType("org.pac4j.")
                .allowIfBaseType("org.opensaml.")
                .build();
            return configuredBuilder.polymorphicTypeValidator(ptv)
                .activateDefaultTyping(ptv, DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
                .addMixIn(Map.class, PolymorphicMapMixIn.class);
        }
        return configuredBuilder.deactivateDefaultTyping();
    }

    private MapperBuilder determineMapperInstance() {
        switch (jsonFactory) {
            case final YAMLFactory factory -> {
                return YAMLMapper.builder(factory);
            }
            case final XmlFactory factory -> {
                return XmlMapper.builder(factory);
            }
            case final CBORFactory factory -> {
                return CBORMapper.builder(factory);
            }
            case final SmileFactory factory -> {
                return SmileMapper.builder(factory);
            }
            case final JsonFactory factory -> {
                return JsonMapper.builder(factory)
                    .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS, isQuoteFieldNames())
                    .configure(JsonWriteFeature.QUOTE_PROPERTY_NAMES, isQuoteFieldNames());
            }
            default -> throw new IllegalStateException("Unexpected value: " + jsonFactory);
        }
    }

    private static JacksonModule getCasJacksonModule() {
        val casModule = new SimpleModule();
        casModule.addDeserializer(URI.class, new URIDeserializer());
        return casModule;
    }

    private static final class URIDeserializer extends StdDeserializer<URI> {
        URIDeserializer() {
            this(URI.class);
        }

        URIDeserializer(final Class<?> vc) {
            super(vc);
        }

        @Override
        public URI deserialize(final JsonParser jp, final DeserializationContext ctxt) {
            return FunctionUtils.doUnchecked(() -> {
                val value = SpringExpressionLanguageValueResolver.getInstance().resolve(jp.getString().trim());
                return StringUtils.isNotBlank(value) ? new URI(value) : null;
            });
        }
    }

    @JsonDeserialize(contentUsing = MapContentDeserializer.class)
    private interface PolymorphicMapMixIn {
    }
}
    
