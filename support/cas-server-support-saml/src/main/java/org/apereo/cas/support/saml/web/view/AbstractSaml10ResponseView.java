package org.apereo.cas.support.saml.web.view;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.opensaml.saml.saml1.core.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Base class for all views that render SAML1 SOAP messages directly to the HTTP response stream.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public abstract class AbstractSaml10ResponseView extends AbstractCasView {
    /**
     * The Saml object builder.
     */
    protected Saml10ObjectBuilder samlObjectBuilder;

    private ArgumentExtractor samlArgumentExtractor;
    
    private String encoding = StandardCharsets.UTF_8.name();

    /** Defaults to 0. */
    private int skewAllowance;

    /**
     * Instantiates a new saml 10 response view.
     */
    public AbstractSaml10ResponseView() {
        this.samlArgumentExtractor = new DefaultArgumentExtractor(new SamlServiceFactory());
    }

    /**
     * Sets the character encoding in the HTTP response.
     *
     * @param encoding Response character encoding.
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    /**
    * Sets the allowance for time skew in seconds
    * between CAS and the client server.  Default 0s.
    * This value will be subtracted from the current time when setting the SAML
    * {@code NotBeforeDate} attribute, thereby allowing for the
    * CAS server to be ahead of the client by as much as the value defined here.
    *
    * <p><strong>Note:</strong> Skewing of the issue instant via setting this property
    * applies to all saml assertions that are issued by CAS and it
    * currently cannot be controlled on a per relying party basis.
    * Before configuring this, it is recommended that each service provider
    * attempt to correctly sync their system time with an NTP server
    * so as to match the CAS server's issue instant config and to
    * avoid applying this setting globally. This should only
    * be used in situations where the NTP server is unresponsive to
    * sync time on the client, or the client is simply unable
    * to adjust their server time configuration.</p>
    *
    * @param skewAllowance Number of seconds to allow for variance.
    */
    public void setSkewAllowance(final int skewAllowance) {
        logger.debug("Using {} seconds as skew allowance.", skewAllowance);
        this.skewAllowance = skewAllowance;
    }

    @Override
    protected void renderMergedOutputModel(
            final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {

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
                    logger.debug(e.getMessage(), e);
                }
            }

            logger.debug("Using {} as the recipient of the SAML response for {}", serviceId, service);
            final Response samlResponse = this.samlObjectBuilder.newResponse(
                    this.samlObjectBuilder.generateSecureRandomId(),
                    ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(this.skewAllowance), serviceId, service);

            prepareResponse(samlResponse, model);
            this.samlObjectBuilder.encodeSamlResponse(response, request, samlResponse);
        } catch (final Exception e) {
            logger.error("Error generating SAML response for service {}.", serviceId, e);
            throw e;
        }
    }

    public void setSamlObjectBuilder(final Saml10ObjectBuilder samlObjectBuilder) {
        this.samlObjectBuilder = samlObjectBuilder;
    }

    public void setSamlArgumentExtractor(final ArgumentExtractor samlArgumentExtractor) {
        this.samlArgumentExtractor = samlArgumentExtractor;
    }

    /**
     * Subclasses must implement this method by adding child elements (status, assertion, etc) to
     * the given empty SAML 1 response message.  Implementers need not be concerned with error handling.
     *
     * @param response SAML 1 response message to be filled.
     * @param model Spring MVC model map containing data needed to prepare response.
     */
    protected abstract void prepareResponse(Response response, Map<String, Object> model);

}
