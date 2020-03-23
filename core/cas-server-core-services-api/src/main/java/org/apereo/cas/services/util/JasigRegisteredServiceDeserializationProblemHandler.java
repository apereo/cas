package org.apereo.cas.services.util;

import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.util.function.FunctionUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.base.Predicates;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.jooq.lambda.Unchecked;

import java.util.regex.Pattern;

/**
 * This is {@link JasigRegisteredServiceDeserializationProblemHandler}
 * that attempts load JSON definitions assigned to the `org.jasig`
 * namespace. This component should be registered globally with JSON object mappers.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
class JasigRegisteredServiceDeserializationProblemHandler extends DeserializationProblemHandler {

    private static final int TOKEN_COUNT_DURATION = 6;
    private static final int TOKEN_COUNT_EXPIRATION = 3;
    private static final Pattern PATTERN_JASIG_NAMESPACE = Pattern.compile("jasig");

    @SneakyThrows
    @Override
    public JavaType handleUnknownTypeId(final DeserializationContext ctxt,
                                        final JavaType baseType,
                                        final String subTypeId, final TypeIdResolver idResolver,
                                        final String failureMsg) {

        if (subTypeId.contains("org.jasig.")) {
            val newTypeName = PATTERN_JASIG_NAMESPACE.matcher(subTypeId).replaceAll("apereo");
            LOGGER.warn("Found legacy CAS JSON definition type identified as [{}]. "
                    + "While CAS will attempt to convert the legacy definition to [{}] for the time being, "
                    + "the definition SHOULD manually be upgraded to the new supported syntax",
                subTypeId, newTypeName);
            val newType = ClassUtils.getClass(newTypeName);
            return SimpleType.constructUnsafe(newType);
        }
        return null;
    }

    @Override
    public boolean handleUnknownProperty(final DeserializationContext ctxt, final JsonParser p,
                                         final JsonDeserializer<?> deserializer,
                                         final Object beanOrClass, final String propertyName) {

        return FunctionUtils.doIf(Predicates.instanceOf(CachingPrincipalAttributesRepository.class),
            () -> {
                if (!"duration".equals(propertyName)) {
                    return Boolean.FALSE;
                }
                return Unchecked.supplier(() -> {
                    for (var i = 1; i <= TOKEN_COUNT_DURATION; i++) {
                        p.nextToken();
                    }
                    val timeUnit = p.getText();
                    for (var i = 1; i <= TOKEN_COUNT_EXPIRATION; i++) {
                        p.nextToken();
                    }

                    val expiration = p.getValueAsInt();
                    val repo = CachingPrincipalAttributesRepository.class.cast(beanOrClass);
                    repo.setTimeUnit(timeUnit);
                    repo.setExpiration(expiration);

                    LOGGER.warn("CAS has converted JSON property [{}] for type [{}]. It parsed 'expiration' value [{}] with time unit of [{}]."
                            + "It is STRONGLY recommended that you review the configuration and upgrade from the legacy syntax.",
                        propertyName, beanOrClass.getClass().getName(), expiration, timeUnit);
                    return Boolean.TRUE;
                }).get();
            },
            () -> Boolean.FALSE)
            .apply(beanOrClass);
    }
}
