package org.apereo.cas.support.saml.web.view;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.opensaml.saml.saml1.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Base class for all views that render SAML1 SOAP messages directly to the HTTP response stream.
 *
 * @author Marvin S.Addison
 * @since 3.5.1
 */

public abstract class AbstractSaml10ResponseView extends AbstractCasView {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSaml10ResponseView.class);
    
    /**
     * The Saml object builder.
     */
    protected final Saml10ObjectBuilder samlObjectBuilder;

    /**
     * Skew time.
     **/
    protected final int skewAllowance;
    
    /**
     * Assertion validity period length.
     **/
    protected final int issueLength;

    private final ArgumentExtractor samlArgumentExtractor;

    private final String encoding;


    /**
     * Instantiates a new Abstract saml 10 response view.
     *
     * @param successResponse                the success response
     * @param protocolAttributeEncoder       the protocol attribute encoder
     * @param servicesManager                the services manager
     * @param authenticationContextAttribute the authentication context attribute
     * @param samlObjectBuilder              the saml object builder
     * @param samlArgumentExtractor          the saml argument extractor
     * @param encoding                       Sets the character encoding in the HTTP response.
     * @param skewAllowance                  Sets the allowance for time skew in seconds
     *                                       between CAS and the client server.  Default 0s. This value will be
     *                                       subtracted from the current time when setting the SAML
     *                                       {@code NotBeforeDate} attribute, thereby allowing for the
     *                                       CAS server to be ahead of the client by as much as the value defined here.
     *                                       Skewing of the issue instant via setting this property
     *                                       applies to all saml assertions that are issued by CAS and it
     *                                       currently cannot be controlled on a per relying party basis.
     *                                       Before configuring this, it is recommended that each service provider
     *                                       attempt to correctly sync their system time with an NTP server
     *                                       so as to match the CAS server's issue instant config and to
     *                                       avoid applying this setting globally. This should only
     *                                       be used in situations where the NTP server is unresponsive to
     *                                       sync time on the client, or the client is simply unable
     *                                       to adjust their server time configuration.
     * @param issueLength                    Sets the length of time in seconds between the {@code NotBefore}
     *                                       and {@code NotOnOrAfter} attributes in the SAML assertion. Default 30s.
     * @param authAttrReleasePolicy          This policy controls which authentication attributes get released in a
     *                                       validation response.
     */
    public AbstractSaml10ResponseView(final boolean successResponse,
                                      final ProtocolAttributeEncoder protocolAttributeEncoder,
                                      final ServicesManager servicesManager,
                                      final String authenticationContextAttribute,
                                      final Saml10ObjectBuilder samlObjectBuilder,
                                      final ArgumentExtractor samlArgumentExtractor,
                                      final String encoding,
                                      final int skewAllowance,
                                      final int issueLength,
                                      final AuthenticationAttributeReleasePolicy authAttrReleasePolicy) {
        super(successResponse, protocolAttributeEncoder, servicesManager, authenticationContextAttribute,
                authAttrReleasePolicy);
        this.samlObjectBuilder = samlObjectBuilder;
        this.samlArgumentExtractor = samlArgumentExtractor;
        this.encoding = encoding;
        this.issueLength = issueLength;

        LOGGER.trace("Using [{}] seconds as skew allowance.", skewAllowance);
        this.skewAllowance = skewAllowance;
    }

    @Override
    protected void renderMergedOutputModel(final Map<String, Object> model, 
                                           final HttpServletRequest request, 
                                           final HttpServletResponse response) throws Exception {

        String serviceId = null;
        try {
            response.setCharacterEncoding(this.encoding);
            final WebApplicationService service = this.samlArgumentExtractor.extractService(request);
            if (service == null || StringUtils.isBlank(service.getId())) {
                serviceId = "UNKNOWN";
            } else {
                try {
                    serviceId = new URL(service.getId()).getHost();
                } catch (final MalformedURLException e) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }

            LOGGER.debug("Using [{}] as the recipient of the SAML response for [{}]", serviceId, service);
            final Response samlResponse = this.samlObjectBuilder.newResponse(
                    this.samlObjectBuilder.generateSecureRandomId(),
                    ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(this.skewAllowance), serviceId, service);
            LOGGER.debug("Created SAML response for service [{}]", serviceId);
            
            prepareResponse(samlResponse, model);

            LOGGER.debug("Starting to encode SAML response for service [{}]", serviceId);
            this.samlObjectBuilder.encodeSamlResponse(response, request, samlResponse);
        } catch (final Exception e) {
            LOGGER.error("Error generating SAML response for service [{}].", serviceId, e);
            throw e;
        }
    }

    /**
     * Subclasses must implement this method by adding child elements (status, assertion, etc) to
     * the given empty SAML 1 response message.  Implementers need not be concerned with error handling.
     *
     * @param response SAML 1 response message to be filled.
     * @param model    Spring MVC model map containing data needed to prepare response.
     */
    protected abstract void prepareResponse(Response response, Map<String, Object> model);

}
