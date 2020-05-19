package org.apereo.cas.interrupt;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.interrupt.InterruptProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.webflow.execution.RequestContext;

import java.nio.charset.StandardCharsets;
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
    public InterruptResponse inquireInternal(final Authentication authentication,
                                             final RegisteredService registeredService,
                                             final Service service,
                                             final Credential credential,
                                             final RequestContext requestContext) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            parameters.put("username", authentication.getPrincipal().getId());

            if (service != null) {
                parameters.put(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            }
            if (registeredService != null) {
                parameters.put("registeredService", registeredService.getServiceId());
            }

            val headers = new HashMap<String, Object>();
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val acceptedLanguage = request.getHeader("accept-language");
            if (StringUtils.isNotBlank(acceptedLanguage)) {
                headers.put("Accept-Language", acceptedLanguage);
            }

            response = HttpUtils.execute(restProperties.getUrl(), restProperties.getMethod(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(),
                parameters, headers);
            if (response != null && response.getEntity() != null) {
                val content = response.getEntity().getContent();
                val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                return MAPPER.readValue(JsonValue.readHjson(result).toString(), InterruptResponse.class);
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }
        return InterruptResponse.none();
    }
}
