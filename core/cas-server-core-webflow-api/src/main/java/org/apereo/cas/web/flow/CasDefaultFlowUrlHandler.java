package org.apereo.cas.web.flow;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
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
@Slf4j
@RequiredArgsConstructor
public class CasDefaultFlowUrlHandler extends DefaultFlowUrlHandler {

    /**
     * Default flow execution key parameter name, {@value}.
     * Same as that used by {@link DefaultFlowUrlHandler}.
     **/
    public static final String DEFAULT_FLOW_EXECUTION_KEY_PARAMETER = "execution";

    private static final String DELIMITER = "&";

    private final List<CasWebflowIdExtractor> flowIdExtractors;

    private static Stream<String> encodeMultiParameter(final String key, final String[] values, final String encoding) {
        return Stream.of(values).map(value -> encodeSingleParameter(key, value, encoding));
    }

    private static String encodeSingleParameter(final String key, final String value, final String encoding) {
        return EncodingUtils.urlEncode(key, encoding) + '=' + EncodingUtils.urlEncode(value, encoding);
    }

    @Override
    public String getFlowExecutionKey(final HttpServletRequest request) {
        var executionKey = request.getParameter(DEFAULT_FLOW_EXECUTION_KEY_PARAMETER);
        if (StringUtils.isBlank(executionKey) && HttpMethod.POST.matches(request.getMethod())) {
            val parameters = WebUtils.getHttpRequestParametersFromRequestBody(request);
            executionKey = parameters.get(DEFAULT_FLOW_EXECUTION_KEY_PARAMETER);
        }
        return executionKey;
    }

    @Override
    public String createFlowExecutionUrl(final String flowId, final String flowExecutionKey, final HttpServletRequest request) {
        val encoding = getEncodingScheme(request);
        val executionKey = encodeSingleParameter(DEFAULT_FLOW_EXECUTION_KEY_PARAMETER, flowExecutionKey, encoding);
        val url = request.getParameterMap().entrySet()
            .stream()
            .flatMap(entry -> encodeMultiParameter(entry.getKey(), entry.getValue(), encoding))
            .collect(Collectors.joining(DELIMITER, request.getRequestURI() + '?', DELIMITER + executionKey));
        LOGGER.trace("Final flow execution url is [{}]", url);
        return url;
    }

    @Override
    public String createFlowDefinitionUrl(final String flowId, final AttributeMap input, final HttpServletRequest request) {
        return HttpRequestUtils.getFullRequestUrl(request);
    }

    @Override
    public String getFlowId(final HttpServletRequest request) {
        var flowId = super.getFlowId(request);
        if (flowId.contains("#")) {
            flowId = RegExUtils.removePattern((CharSequence) flowId, "#.*");
        }
        for (val flowIdExtractor : flowIdExtractors) {
            flowId = flowIdExtractor.extract(request, flowId);
        }
        return flowId.trim();
    }
}
