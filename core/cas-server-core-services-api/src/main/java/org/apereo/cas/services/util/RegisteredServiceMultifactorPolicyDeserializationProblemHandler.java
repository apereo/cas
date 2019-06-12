package org.apereo.cas.services.util;

import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * This is {@link RegisteredServiceMultifactorPolicyDeserializationProblemHandler}
 * that attempts load JSON definitions assigned to the `org.jasig`
 * namespace. This component should be registered globally with JSON object mappers.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
class RegisteredServiceMultifactorPolicyDeserializationProblemHandler extends DeserializationProblemHandler {
    @Override
    public Object handleWeirdStringValue(final DeserializationContext ctxt, final Class<?> targetType,
                                         final String valueToConvert, final String failureMsg) throws IOException {
        if (targetType.equals(RegisteredServiceMultifactorPolicyFailureModes.class)) {
            if (StringUtils.equals("NOT_SET", valueToConvert)) {
                LOGGER.warn("Found legacy attribute value [{}] which will be converted to [{}] as part of a service multifactor authentication policy."
                        + "The definition SHOULD manually be upgraded to the new supported syntax",
                    valueToConvert, RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED);
                return RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED;
            }
        }
        return super.handleWeirdStringValue(ctxt, targetType, valueToConvert, failureMsg);
    }
}
