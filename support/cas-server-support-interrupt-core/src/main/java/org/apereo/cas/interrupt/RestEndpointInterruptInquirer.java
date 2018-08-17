package org.apereo.cas.interrupt;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.interrupt.InterruptProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;

/**
 * This is {@link RestEndpointInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class RestEndpointInterruptInquirer extends BaseInterruptInquirer {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final InterruptProperties.Rest restProperties;

    @Override
    public InterruptResponse inquireInternal(final Authentication authentication, final RegisteredService registeredService,
                                             final Service service, final Credential credential) {
        try {
            val parameters = new HashMap<String, Object>();
            parameters.put("username", authentication.getPrincipal().getId());

            if (service != null) {
                parameters.put(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            }
            if (registeredService != null) {
                parameters.put("registeredService", registeredService.getServiceId());
            }
            val response = HttpUtils.execute(restProperties.getUrl(), restProperties.getMethod(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(),
                parameters, new HashMap<>());
            if (response != null && response.getEntity() != null) {
                return MAPPER.readValue(response.getEntity().getContent(), InterruptResponse.class);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return InterruptResponse.none();
    }
}
