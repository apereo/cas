package org.apereo.cas.oidc.discovery.webfinger.userinfo;

import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerUserInfoRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.hjson.JsonValue;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OidcRestfulWebFingerUserInfoRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcRestfulWebFingerUserInfoRepository implements OidcWebFingerUserInfoRepository {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final RestEndpointProperties properties;

    @Override
    public Map<String, Object> findByEmailAddress(final String email) {
        return findAccountViaRestApi(CollectionUtils.wrap("email", email));
    }

    @Override
    public Map<String, Object> findByUsername(final String username) {
        return findAccountViaRestApi(CollectionUtils.wrap("username", username));
    }

    /**
     * Find account via rest api and return user-info map.
     *
     * @param headers the headers
     * @return the map
     */
    protected Map<String, Object> findAccountViaRestApi(final Map<String, Object> headers) {
        HttpResponse response = null;
        try {
            response = HttpUtils.execute(properties.getUrl(), properties.getMethod(),
                properties.getBasicAuthUsername(), properties.getBasicAuthPassword(),
                new HashMap<>(0), headers);
            if (response != null && response.getEntity() != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return MAPPER.readValue(JsonValue.readHjson(result).toString(), Map.class);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return new HashMap<>(0);
    }
}
