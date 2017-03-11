package org.apereo.cas.ws.idp;

import org.apache.wss4j.common.util.DOM2Writer;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.ws.idp.api.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.api.RealmAwareIdentityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.w3c.dom.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This is {@link FederationMetadataServlet}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class FederationMetadataServlet extends HttpServlet {
    private static final long serialVersionUID = -6927484130511112872L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FederationMetadataServlet.class);

    private final String realm;

    public FederationMetadataServlet(final String realm) {
        this.realm = realm;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            final PrintWriter out = response.getWriter();

            final ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
            final IdentityProviderConfigurationService configService = ctx.getBean(IdentityProviderConfigurationService.class, "idpConfigService");
            final RealmAwareIdentityProvider idpConfig = configService.getIdentityProvider(this.realm);

            final FederationMetadataWriter mw = new FederationMetadataWriter();
            final Document metadata = mw.produceMetadataDocument(idpConfig);
            out.write(DOM2Writer.nodeToString(metadata));

        } catch (final Exception ex) {
            LOGGER.error("Failed to get metadata document", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
