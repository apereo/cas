package org.apereo.cas.util;

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
public final class JsonUtils {

    /** Private constructor. */
    private JsonUtils() {}

    /**
     * Render model and view.
     *
     * @param model the model
     * @param response the response
     */
    public static void render(final Object model, final HttpServletResponse response) {
        try {
            final MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
            jsonConverter.setPrettyPrint(true);
            final MediaType jsonMimeType = MediaType.APPLICATION_JSON;
            jsonConverter.write(model, jsonMimeType, new ServletServerHttpResponse(response));
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Render model and view. Sets the response status to OK.
     *
     * @param response the response
     */
    public static void render(final HttpServletResponse response) {
        try {
            final Map<String, Object> map = new HashMap<>();
            response.setStatus(HttpServletResponse.SC_OK);
            render(map, response);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    /**
     * Render exceptions. Adds error messages and the stack trace to the json model
     * and sets the response status accordingly to note bad requests.
     *
     * @param ex the ex
     * @param response the response
     */
    public static void renderException(final Exception ex, final HttpServletResponse response) {
        final Map<String, String> map = new HashMap<>();
        map.put("error", ex.getMessage());
        map.put("stacktrace", Arrays.deepToString(ex.getStackTrace()));
        renderException(map, response);
    }

    /**
     * Render exceptions. Sets the response status accordingly to note bad requests.
     *
     * @param model the model
     * @param response the response
     */
    private static void renderException(final Map model, final HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        model.put("status", HttpServletResponse.SC_BAD_REQUEST);
        render(model, response);
    }

}
