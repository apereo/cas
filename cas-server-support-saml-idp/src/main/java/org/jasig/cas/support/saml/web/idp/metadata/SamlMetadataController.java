package org.jasig.cas.support.saml.web.idp.metadata;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.support.saml.SamlIdPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The {@link SamlMetadataController} will attempt
 * to produce saml metadata for CAS as an identity provider.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@Controller("samlMetadataController")
public final class SamlMetadataController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${cas.samlidp.metadata.location:}")
    private File metadataLocation;

    @Value("${cas.samlidp.entityid:}")
    private String entityId;

    @Value("${cas.samlidp.hostname:}")
    private String hostName;

    @Value("${cas.samlidp.scope:}")
    private String scope;

    @Autowired
    @Qualifier("templateSpMetadata")
    private Resource templateSpMetadata;

    /**
     * Instantiates a new Saml metadata controller.
     * Required for bean initialization.
     */
    public SamlMetadataController() {
    }

    /**
     * Post constructor placeholder for additional
     * extensions. This method is called after
     * the object has completely initialized itself.
     */
    @PostConstruct
    public void postConstruct() {
    }

    /**
     * Generate metadata for a relying party.
     *
     * @param request  the request
     * @param response servlet response
     * @throws IOException the iO exception
     */
    @RequestMapping(method = RequestMethod.POST, value = SamlIdPConstants.ENDPOINT_GENERATE_RP_METADATA)
    public void generateMetadataForService(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final String entityID = request.getParameter("entityId");
        final String authnRequestSigned = StringUtils.defaultString(request.getParameter("authnRequestSigned"), "false");
        final String wantAssertionsSigned = StringUtils.defaultString(request.getParameter("wantAssertionsSigned"), "false");
        final String x509Certificate = request.getParameter("x509Certificate");
        final String acsUrl = request.getParameter("acsUrl");

        try (final PrintWriter writer = response.getWriter()) {
            if (StringUtils.isBlank(entityID) || StringUtils.isBlank(acsUrl) || StringUtils.isBlank(x509Certificate)) {
                logger.warn("Missing entityID, ACS url or X509 signing certificate");
                response.setContentType("text/plain;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write("Missing entityID, ACS url or X509 signing certificate");
            } else {
                final String contents = IOUtils.toString(templateSpMetadata.getInputStream());
                response.setContentType("text/xml;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);
                writer.write(contents.replace("$entityId", entityID).replace("$acsUrl", acsUrl)
                        .replace("$x509Certificate", x509Certificate)
                        .replace("$authnRequestSigned", authnRequestSigned)
                        .replace("$wantAssertionsSigned", wantAssertionsSigned));
            }
            writer.flush();
        }
    }

    /**
     * Displays the identity provider metadata.
     * Checks to make sure metadata exists, and if not, generates it first.
     *
     * @param response servlet response
     * @throws IOException the iO exception
     */
    @RequestMapping(method = RequestMethod.GET, value = SamlIdPConstants.ENDPOINT_IDP_METADATA)
    public void generateMetadataForIdp(final HttpServletResponse response) throws IOException {

        if (StringUtils.isBlank(this.entityId)
                || StringUtils.isBlank(this.hostName)
                || StringUtils.isAllLowerCase(this.scope)) {
            logger.warn("Metadata cannot be generated, because the SAML Identity Provider is not configured."
                    + " Examine settings and ensure metadata location, entityId, hostName and scope are all defined");
            return;
        }

        logger.debug("Preparing to generate metadata for entityId [{}]", this.entityId);
        final GenerateSamlIdpMetadata generator = new GenerateSamlIdpMetadata(this.metadataLocation,
                this.hostName, this.entityId, this.scope);
        if (generator.isMetadataMissing()) {
            logger.debug("Metadata does not exist at [{}]. Creating...", this.metadataLocation);
            generator.generate();
        }
        logger.debug("Metadata is available at [{}]", generator.getMetadataFile());

        final String contents = FileUtils.readFileToString(generator.getMetadataFile());
        response.setContentType("text/xml;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        final PrintWriter writer = response.getWriter();
        logger.debug("Producing metadata for the response");
        writer.write(contents);
        writer.flush();
        writer.close();
    }
}
