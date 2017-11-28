package org.apereo.cas.interrupt;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.interrupt.InterruptProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link RestEndpointInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestEndpointInterruptInquirer extends BaseInterruptInquirer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestEndpointInterruptInquirer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final InterruptProperties.Rest restProperties;

    public RestEndpointInterruptInquirer(final InterruptProperties.Rest restProperties) {
        this.restProperties = restProperties;
    }

    @Override
    public InterruptResponse inquire(final Authentication authentication, final RegisteredService registeredService, final Service service) {
        try {
            final Map<String, String> parameters = new HashMap<>();
            parameters.put("username", authentication.getPrincipal().getId());
            
            if (service != null) {
                parameters.put(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            }
            if (registeredService != null) {
                parameters.put("registeredService", registeredService.getServiceId());
            }
            final HttpResponse response = HttpUtils.execute(restProperties.getUrl(), restProperties.getMethod(),
                    restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(),
                    parameters);
            return MAPPER.readValue(response.getEntity().getContent(), InterruptResponse.class);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new InterruptResponse(false);
    }
}
