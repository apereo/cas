package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;

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
            val rest = adaptiveAuthenticationProperties.getIpIntel().getRest();

            val parameters = new HashMap<String, Object>();
            parameters.put("clientIpAddress", clientIpAddress);

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .parameters(parameters)
                .build();

            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.equals(HttpStatus.FORBIDDEN) || status.equals(HttpStatus.UNAUTHORIZED)) {
                    throw new AuthenticationException("Unable to accept response status " + status);
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
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return IPAddressIntelligenceResponse.banned();
    }
}
