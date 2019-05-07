package org.apereo.cas.web.flow.decorator;

import org.apereo.cas.configuration.model.webapp.WebflowLoginDecoratorProperties;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;

/**
 * This is {@link RestfulLoginWebflowDecorator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RestfulLoginWebflowDecorator implements WebflowDecorator {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final WebflowLoginDecoratorProperties.Rest restProperties;

    @Override
    @SneakyThrows
    public void decorate(final RequestContext requestContext, final ApplicationContext applicationContext) {
        HttpResponse response = null;
        try {
            response = HttpUtils.execute(restProperties.getUrl(), restProperties.getUrl(),
                    restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword());
            val statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                val jsonObject = MAPPER.readValue(response.getEntity().getContent(), Map.class);
                requestContext.getFlowScope().put("decoration", jsonObject);
            }
        } finally {
            HttpUtils.close(response);
        }
    }
}
