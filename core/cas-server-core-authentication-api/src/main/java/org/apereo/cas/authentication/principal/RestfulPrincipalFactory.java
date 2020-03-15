package org.apereo.cas.authentication.principal;

import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * This is {@link RestfulPrincipalFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class RestfulPrincipalFactory extends DefaultPrincipalFactory {
    private static final long serialVersionUID = -1344968589212057694L;

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final String url;

    private final String basicAuthUsername;

    private final String basicAuthPassword;

    @Override
    public Principal createPrincipal(final String id, final Map<String, List<Object>> attributes) {
        HttpResponse response = null;
        try {
            val current = super.createPrincipal(id, attributes);
            val entity = MAPPER.writeValueAsString(current);
            response = HttpUtils.executePost(this.url, this.basicAuthUsername, this.basicAuthPassword, entity);
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.debug("Principal factory response received: [{}]", result);
                return MAPPER.readValue(JsonValue.readHjson(result).toString(), SimplePrincipal.class);
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        LOGGER.error("Unable to create principal from REST endpoint [{}] for [{}]", this.url, id);
        return null;
    }
}
