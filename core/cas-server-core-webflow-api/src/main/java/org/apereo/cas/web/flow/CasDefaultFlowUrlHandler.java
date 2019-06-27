package org.apereo.cas.web.flow;

import org.apereo.cas.util.EncodingUtils;

import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides special handling for parameters in requests made to the CAS login
 * webflow.
 *
 * @author Scott Battaglia
 * @since 3.4
 */
@Setter
public class CasDefaultFlowUrlHandler extends DefaultFlowUrlHandler {

    /**
     * Default flow execution key parameter name, {@value}.
     * Same as that used by {@link DefaultFlowUrlHandler}.
     **/
    public static final String DEFAULT_FLOW_EXECUTION_KEY_PARAMETER = "execution";
    private static final String DELIMITER = "&";

    /**
     * Flow execution parameter name.
     */
    private String flowExecutionKeyParameter = DEFAULT_FLOW_EXECUTION_KEY_PARAMETER;

    private static Stream<String> encodeMultiParameter(final String key, final String[] values, final String encoding) {
        return Stream.of(values).map(value -> encodeSingleParameter(key, value, encoding));
    }

    private static String encodeSingleParameter(final String key, final String value, final String encoding) {
        return EncodingUtils.urlEncode(key, encoding) + '=' + EncodingUtils.urlEncode(value, encoding);
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
        val encoding = getEncodingScheme(request);


        val executionKey = encodeSingleParameter(this.flowExecutionKeyParameter, flowExecutionKey, encoding);
        return request.getParameterMap().entrySet()
            .stream()
            .flatMap(entry -> encodeMultiParameter(entry.getKey(), entry.getValue(), encoding))
            .collect(Collectors.joining(DELIMITER, request.getRequestURI() + '?', DELIMITER + executionKey));
    }

    @Override
    public String createFlowDefinitionUrl(final String flowId, final AttributeMap input, final HttpServletRequest request) {
        return request.getRequestURI() + (request.getQueryString() != null ? '?' + request.getQueryString() : StringUtils.EMPTY);
    }
}
