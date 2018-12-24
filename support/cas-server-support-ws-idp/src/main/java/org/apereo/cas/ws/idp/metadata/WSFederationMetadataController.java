package org.apereo.cas.ws.idp.metadata;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.wss4j.common.util.DOM2Writer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link WSFederationMetadataController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Controller("WSFederationMetadataController")
@Slf4j
@RequiredArgsConstructor
public class WSFederationMetadataController {
    private final CasConfigurationProperties casProperties;

    /**
     * Get Metadata.
     *
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    @GetMapping(path = WSFederationConstants.ENDPOINT_FEDERATION_METADATA)
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        try {
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            val out = response.getWriter();
            val metadata = WSFederationMetadataWriter.produceMetadataDocument(casProperties);
            out.write(DOM2Writer.nodeToString(metadata));
        } catch (final Exception ex) {
            LOGGER.error("Failed to get metadata document", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
