package org.jasig.cas.web.flow;

import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides special handling for parameters in requests made to the CAS login
 * webflow.
 *
 * @author Scott Battaglia
 * @since 3.4
 */
public final class CasDefaultFlowUrlHandler extends DefaultFlowUrlHandler {

    /**
     * Default flow execution key parameter name, {@value}.
     * Same as that used by {@link DefaultFlowUrlHandler}.
     **/
    public static final String DEFAULT_FLOW_EXECUTION_KEY_PARAMETER = "execution";

    /** Flow execution parameter name. */
    private String flowExecutionKeyParameter = DEFAULT_FLOW_EXECUTION_KEY_PARAMETER;

    /**
     * Sets the parameter name used to carry flow execution key in request.
     *
     * @param parameterName Request parameter name.
     */
    public void setFlowExecutionKeyParameter(final String parameterName) {
        this.flowExecutionKeyParameter = parameterName;
    }

    /**
     * Get the flow execution key.
     *
     * @param request the current HTTP servlet request.
     * @return the flow execution key.
     */
    @Override
    public String getFlowExecutionKey(final HttpServletRequest request) {
        return request.getParameter(flowExecutionKeyParameter);
    }

    @Override
    public String createFlowExecutionUrl(final String flowId, final String flowExecutionKey, final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        final String encoding = getEncodingScheme(request);
        builder.append(request.getRequestURI());
        builder.append('?');

        final Map<String, String[]> flowParams = new LinkedHashMap<>(request.getParameterMap());
        flowParams.put(this.flowExecutionKeyParameter, new String[]{flowExecutionKey});
        builder.append(toQueryString(flowParams, encoding));

        return builder.toString();
    }

    @Override
    public String createFlowDefinitionUrl(final String flowId, final AttributeMap input, final HttpServletRequest request) {
        return request.getRequestURI()
            + (request.getQueryString() != null ? '?'
            + request.getQueryString() : "");
    }

    private String toQueryString(final Map<String, String[]> flowParams, final String encoding) {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<String, String[]> entry : flowParams.entrySet()) {
            if (builder.length() != 0) {
                builder.append("&");
            }

            builder.append(encodeMultiParameter(entry.getKey(), entry.getValue(), encoding));
        }
        return builder.toString();
    }

    private String encodeMultiParameter(final String key, final String[] values, final String encoding) {
        final StringBuilder builder = new StringBuilder();
        for (final String value : values) {
            if (builder.length() != 0) {
                builder.append("&");
            }

            builder.append(encodeSingleParameter(key, value, encoding));
        }
        return builder.toString();
    }

    private String encodeSingleParameter(final String key, final String value, final String encoding) {
        return urlEncode(key, encoding) + "=" + urlEncode(value, encoding);
    }

    private String urlEncode(final String value, final String encodingScheme) {
        try {
            return URLEncoder.encode(value, encodingScheme);
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Cannot url encode " + value);
        }
    }
}
