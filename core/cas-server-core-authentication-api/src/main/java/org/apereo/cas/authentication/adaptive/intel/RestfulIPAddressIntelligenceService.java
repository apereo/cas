package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
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
    public RestfulIPAddressIntelligenceService(final TenantExtractor tenantExtractor,
                                               final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties) {
        super(tenantExtractor, adaptiveAuthenticationProperties);
    }

    @Override
    public IPAddressIntelligenceResponse examineInternal(final RequestContext context, final String clientIpAddress) {
        HttpResponse response = null;
        try {
            val rest = adaptiveAuthenticationProperties.getIpIntel().getRest();

            val parameters = new HashMap<String, String>();
            parameters.put("clientIpAddress", clientIpAddress);

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(SpringExpressionLanguageValueResolver.getInstance().resolve(rest.getUrl()))
                .parameters(parameters)
                .headers(rest.getHeaders())
                .build();

            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
                if (status == HttpStatus.FORBIDDEN || status == HttpStatus.UNAUTHORIZED) {
                    throw new AuthenticationException("Unable to accept response status " + status);
                }
                if (status == HttpStatus.OK || status == HttpStatus.ACCEPTED) {
                    return IPAddressIntelligenceResponse.allowed();
                }
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val score = Double.parseDouble(IOUtils.toString(content, StandardCharsets.UTF_8));
                    return IPAddressIntelligenceResponse.builder()
                        .score(score)
                        .status(IPAddressIntelligenceResponse.IPAddressIntelligenceStatus.RANKED)
                        .build();
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return IPAddressIntelligenceResponse.banned();
    }
}
