package org.apereo.cas.web.flow.actions;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.EncodingUtils;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Provides special handling for parameters in requests made to the CAS login
 * webflow.
 *
 * @author Scott Battaglia
 * @since 3.4
 */
public class CasDefaultFlowUrlHandler extends DefaultFlowUrlHandler {

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
        return request.getParameter(this.flowExecutionKeyParameter);
    }

    @Override
    public String createFlowExecutionUrl(final String flowId, final String flowExecutionKey, final HttpServletRequest request) {
        final String encoding = getEncodingScheme(request);
        final StringBuilder builder = new StringBuilder(request.getRequestURI())
                .append('?');

        final Map<String, String[]> flowParams = new LinkedHashMap<>(request.getParameterMap());
        flowParams.put(this.flowExecutionKeyParameter, new String[]{flowExecutionKey});

        final String queryString = flowParams.entrySet().stream()
                .flatMap(entry -> encodeMultiParameter(entry.getKey(), entry.getValue(), encoding))
                .reduce((param1, param2) -> param1 + '&' + param2)
                .orElse(StringUtils.EMPTY);

        builder.append(queryString);
        return builder.toString();
    }

    @Override
    public String createFlowDefinitionUrl(final String flowId, final AttributeMap input, final HttpServletRequest request) {
        return request.getRequestURI()
            + (request.getQueryString() != null ? '?'
            + request.getQueryString() : StringUtils.EMPTY);
    }

    private static Stream<String> encodeMultiParameter(final String key, final String[] values, final String encoding) {
        return Stream.of(values).map(value -> encodeSingleParameter(key, value, encoding));
    }

    private static String encodeSingleParameter(final String key, final String value, final String encoding) {
        return EncodingUtils.urlEncode(key, encoding) + '=' + EncodingUtils.urlEncode(value, encoding);
    }

}
