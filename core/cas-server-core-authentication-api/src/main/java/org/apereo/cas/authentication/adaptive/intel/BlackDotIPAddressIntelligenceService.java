package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * This is {@link BlackDotIPAddressIntelligenceService}.
 * See <a href="https://getipintel.net/">this link</a>.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class BlackDotIPAddressIntelligenceService extends BaseIPAddressIntelligenceService {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    public BlackDotIPAddressIntelligenceService(
        final TenantExtractor tenantExtractor,
        final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties) {
        super(tenantExtractor, adaptiveAuthenticationProperties);
    }

    @Override
    public IPAddressIntelligenceResponse examineInternal(final RequestContext context, final String clientIpAddress) {
        val bannedResponse = IPAddressIntelligenceResponse.banned();

        HttpResponse response = null;
        try {
            val properties = adaptiveAuthenticationProperties.getIpIntel().getBlackDot();
            val resolvedUrl = SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl());
            val builder = new StringBuilder(String.format(resolvedUrl, clientIpAddress));
            builder.append("&format=json");

            if (StringUtils.isNotBlank(properties.getEmailAddress())) {
                builder.append("&contact=");
                builder.append(properties.getEmailAddress());
            }

            val flags = switch (properties.getMode().toUpperCase(Locale.ENGLISH)) {
                case "DYNA_LIST" -> "&flags=m";
                case "DYNA_CHECK" -> "&flags=b";
                default -> "&flags=f";
            };
            builder.append(flags);
            val url = builder.toString();
            LOGGER.debug("Sending IP check request to [{}]", url);

            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url(url)
                .maximumRetryAttempts(1)
                .build();
            response = HttpUtils.execute(exec);
            if (response == null || response.getCode() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                LOGGER.error("Exceeded the number of allowed queries");
                return bannedResponse;
            }
            try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                LOGGER.debug("Received payload result after examining IP address [{}] as [{}]", clientIpAddress, result);

                val json = MAPPER.readValue(JsonValue.readHjson(result).toString(), Map.class);
                val status = json.getOrDefault("status", "error").toString();
                if ("success".equalsIgnoreCase(status)) {
                    val rank = Double.parseDouble(json.getOrDefault("result", 1).toString());
                    if (rank == 1) {
                        return bannedResponse;
                    }
                    if (rank == 0) {
                        return IPAddressIntelligenceResponse.allowed();
                    }
                    return IPAddressIntelligenceResponse.builder()
                        .score(rank)
                        .status(IPAddressIntelligenceResponse.IPAddressIntelligenceStatus.RANKED)
                        .build();
                }
                val message = json.getOrDefault("message", "Invalid IP address").toString();
                LOGGER.error(message);
                return bannedResponse;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return bannedResponse;
    }
}
