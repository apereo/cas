package org.apereo.cas.ws.idp.metadata;

import org.apache.wss4j.common.util.DOM2Writer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * This is {@link WSFederationMetadataController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Controller("WSFederationMetadataController")
public class WSFederationMetadataController {
    private static final long serialVersionUID = -6927484130511112872L;
    private static final Logger LOGGER = LoggerFactory.getLogger(WSFederationMetadataController.class);
    private final CasConfigurationProperties casProperties;

    public WSFederationMetadataController(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

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
            final PrintWriter out = response.getWriter();
            final WSFederationMetadataWriter mw = new WSFederationMetadataWriter();

            final Document metadata = mw.produceMetadataDocument(casProperties);
            out.write(DOM2Writer.nodeToString(metadata));
        } catch (final Exception ex) {
            LOGGER.error("Failed to get metadata document", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
