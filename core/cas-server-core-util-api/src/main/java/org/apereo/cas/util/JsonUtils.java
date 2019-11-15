package org.apereo.cas.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@UtilityClass
public class JsonUtils {
    /**
     * Render model and view.
     *
     * @param model    the model
     * @param response the response
     */
    @SneakyThrows
    public static void render(final Object model, final HttpServletResponse response) {
        val jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setPrettyPrint(true);
        val jsonMimeType = MediaType.APPLICATION_JSON;
        jsonConverter.write(model, jsonMimeType, new ServletServerHttpResponse(response));
    }

    /**
     * Render model and view. Sets the response status to OK.
     *
     * @param response the response
     */
    @SneakyThrows
    public static void render(final HttpServletResponse response) {
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
    public static void renderException(final Exception ex, final HttpServletResponse response) {
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
    private static void renderException(final Map<String, Object> model, final HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        model.put("status", HttpServletResponse.SC_BAD_REQUEST);
        render(model, response);
    }

}
