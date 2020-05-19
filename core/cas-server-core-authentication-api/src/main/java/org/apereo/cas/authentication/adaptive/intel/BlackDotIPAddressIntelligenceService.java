package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;

import java.nio.charset.StandardCharsets;
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
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public BlackDotIPAddressIntelligenceService(final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties) {
        super(adaptiveAuthenticationProperties);
    }

    @Override
    public IPAddressIntelligenceResponse examineInternal(final RequestContext context, final String clientIpAddress) {
        val bannedResponse = IPAddressIntelligenceResponse.banned();

        HttpResponse response = null;
        try {
            val properties = adaptiveAuthenticationProperties.getIpIntel().getBlackDot();
            val builder = new StringBuilder(String.format(properties.getUrl(), clientIpAddress));
            builder.append("&format=json");

            if (StringUtils.isNotBlank(properties.getEmailAddress())) {
                builder.append("&contact=");
                builder.append(properties.getEmailAddress());
            }

            switch (properties.getMode().toUpperCase()) {
                case "DYNA_LIST":
                    builder.append("&flags=m");
                    break;
                case "DYNA_CHECK":
                    builder.append("&flags=b");
                    break;
                default:
                    builder.append("&flags=f");
                    break;
            }
            val url = builder.toString();
            LOGGER.debug("Sending IP check request to [{}]", url);
            response = HttpUtils.execute(url, HttpMethod.GET.name());
            if (response == null) {
                return bannedResponse;
            }

            if (response.getStatusLine().getStatusCode() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                LOGGER.error("Exceeded the number of allowed queries");
                return bannedResponse;
            }
            val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
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
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }
        return bannedResponse;
    }
}
