package org.apereo.cas.support.sms;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.SmsSender;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link ClickatellSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class ClickatellSmsSender implements SmsSender {
    private final String token;
    private final String serverUrl;

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final transient RestTemplate restTemplate = new RestTemplate(CollectionUtils.wrapList(new MappingJackson2HttpMessageConverter()));

    @Override
    public boolean send(final String from, final String to, final String message) {
        try {
            val headers = new LinkedMultiValueMap<String, String>();
            headers.add("Authorization", this.token);
            headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

            val map = new HashMap<String, Object>();
            map.put("content", message);
            map.put("to", CollectionUtils.wrap(to));
            map.put("from", from);

            val stringify = new StringWriter();
            mapper.writeValue(stringify, map);

            val request = new HttpEntity<String>(stringify.toString(), headers);
            val response = restTemplate.postForEntity(new URI(this.serverUrl), request, Map.class);
            if (response.hasBody()) {
                val body = response.getBody();
                LOGGER.debug("Received response [{}]", body);

                if (body == null || !body.containsKey("messages")) {
                    LOGGER.error("Response body does not contain any messages");
                    return false;
                }
                val messages = (List<Map>) body.get("messages");

                val error = (String) body.get("error");
                if (StringUtils.isNotBlank(error)) {
                    LOGGER.error(error);
                    return false;
                }

                val errors = messages.stream()
                    .filter(m -> m.containsKey("accepted") && !Boolean.parseBoolean(m.get("accepted").toString()) && m.containsKey("error"))
                    .map(m -> (String) m.get("error"))
                    .collect(Collectors.toList());
                if (errors.isEmpty()) {
                    return true;
                }
                errors.forEach(LOGGER::error);
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return false;
    }
}


