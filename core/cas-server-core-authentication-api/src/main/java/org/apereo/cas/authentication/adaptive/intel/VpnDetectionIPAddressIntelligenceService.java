package org.apereo.cas.authentication.adaptive.intel;

import module java.base;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link VpnDetectionIPAddressIntelligenceService}.
 * Bans VPN and Tor exit IP addresses, and optionally relays,
 * as reported by <a href="https://vpndetection.io/">VPNDetection</a>.
 *
 * @author Uman Shahzad
 * @since 8.1.0
 */
@Slf4j
public class VpnDetectionIPAddressIntelligenceService extends BaseIPAddressIntelligenceService {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    public VpnDetectionIPAddressIntelligenceService(
        final TenantExtractor tenantExtractor,
        final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties) {
        super(tenantExtractor, adaptiveAuthenticationProperties);
    }

    @Override
    public IPAddressIntelligenceResponse examineInternal(final RequestContext context, final String clientIpAddress) {
        HttpResponse response = null;
        try {
            val properties = adaptiveAuthenticationProperties.getIpIntel().getVpnDetection();
            val resolvedUrl = SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl());
            val url = String.format(resolvedUrl, clientIpAddress);
            LOGGER.debug("Sending IP check request to [{}]", url);

            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url(url)
                .build()
                .withoutRetry();
            response = HttpUtils.execute(exec);
            if (response == null || response.getCode() != HttpStatus.OK.value()) {
                LOGGER.error("Unable to examine IP address [{}]; response code is [{}]",
                    clientIpAddress, response == null ? null : response.getCode());
                return IPAddressIntelligenceResponse.banned();
            }
            try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                LOGGER.debug("Received payload result after examining IP address [{}] as [{}]", clientIpAddress, result);
                val json = MAPPER.readValue(result, Map.class);
                if (json.get("is_vpn") instanceof final Boolean vpn
                    && json.get("is_tor") instanceof final Boolean tor
                    && json.get("is_relay") instanceof final Boolean relay) {
                    val banned = vpn || tor || (relay && properties.isBanRelays());
                    return banned ? IPAddressIntelligenceResponse.banned() : IPAddressIntelligenceResponse.allowed();
                }
                LOGGER.error("Unable to examine IP address [{}]; unexpected response [{}]", clientIpAddress, result);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return IPAddressIntelligenceResponse.banned();
    }
}
