package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.HttpUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * This is {@link RestfulIPAddressIntelligenceService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class RestfulIPAddressIntelligenceService extends BaseIPAddressIntelligenceService {
    public RestfulIPAddressIntelligenceService(final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties) {
        super(adaptiveAuthenticationProperties);
    }

    @Override
    public IPAddressIntelligenceResponse examineInternal(final RequestContext context, final String clientIpAddress) {
        HttpResponse response = null;
        try {
            val restProperties = adaptiveAuthenticationProperties.getIpIntel().getRest();

            val parameters = new HashMap<String, Object>();
            parameters.put("clientIpAddress", clientIpAddress);
            response = HttpUtils.execute(restProperties.getUrl(), HttpMethod.GET.name(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(),
                parameters, new HashMap<>(0));

            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.equals(HttpStatus.FORBIDDEN) || status.equals(HttpStatus.UNAUTHORIZED)) {
                    return IPAddressIntelligenceResponse.banned();
                }
                if (status.equals(HttpStatus.OK) || status.equals(HttpStatus.ACCEPTED)) {
                    return IPAddressIntelligenceResponse.allowed();
                }
                val score = Double.parseDouble(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                return IPAddressIntelligenceResponse.builder()
                    .score(score)
                    .status(IPAddressIntelligenceResponse.IPAddressIntelligenceStatus.RANKED)
                    .build();
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
        return IPAddressIntelligenceResponse.banned();
    }
}
