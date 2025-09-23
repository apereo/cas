package org.apereo.cas.util;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON utility methods.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@UtilityClass
public class JsonUtils {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder().build().toObjectMapper();

    /**
     * Render.
     *
     * @param model the model
     * @return the string
     */
    public String render(final Object model) {
        return render(MAPPER, model);
    }

    /**
     * Render string.
     *
     * @param mapper the mapper
     * @param model  the model
     * @return the string
     */
    public String render(final ObjectMapper mapper, final Object model) {
        return FunctionUtils.doUnchecked(() -> mapper.writeValueAsString(model));
    }

    /**
     * Render model and view.
     *
     * @param model    the model
     * @param response the response
     */
    public void render(final Object model, final HttpServletResponse response) {
        Unchecked.consumer(__ -> {
            val jsonConverter = new MappingJackson2HttpMessageConverter();
            jsonConverter.setPrettyPrint(true);
            val jsonMimeType = MediaType.APPLICATION_JSON;
            jsonConverter.write(model, jsonMimeType, new ServletServerHttpResponse(response));
        }).accept(model);
    }

    /**
     * Render model and view. Sets the response status to OK.
     *
     * @param response the response
     */
    public void render(final HttpServletResponse response) {
        val map = new HashMap<String, Object>();
        response.setStatus(HttpServletResponse.SC_OK);
        render(map, response);
    }

    /**
     * Render exceptions. Adds error messages and the stack trace to the json model
     * and sets the response status accordingly to note bad requests.
     *
     * @param ex       the ex
     * @param response the response
     */
    public void renderException(final Exception ex, final HttpServletResponse response) {
        val map = new HashMap<String, Object>();
        map.put("error", ex.getMessage());
        map.put("stacktrace", Arrays.deepToString(ex.getStackTrace()));
        renderException(map, response);
    }

    /**
     * Render exceptions. Sets the response status accordingly to note bad requests.
     *
     * @param model    the model
     * @param response the response
     */
    private void renderException(final Map<String, Object> model, final HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        model.put("status", HttpServletResponse.SC_BAD_REQUEST);
        render(model, response);
    }

    /**
     * Is valid json?.
     *
     * @param json the json
     * @return true/false
     */
    public boolean isValidJson(final String json) {
        return FunctionUtils.doAndHandle(() -> !MAPPER.readTree(json).isEmpty(), t -> false).get();
    }
}
