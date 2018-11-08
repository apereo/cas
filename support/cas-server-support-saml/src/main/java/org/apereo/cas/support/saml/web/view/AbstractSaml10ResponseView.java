package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml1.core.Response;

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

@Slf4j
public abstract class AbstractSaml10ResponseView extends AbstractCasView {


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

    public AbstractSaml10ResponseView(final boolean successResponse,
                                      final ProtocolAttributeEncoder protocolAttributeEncoder,
                                      final ServicesManager servicesManager,
                                      final Saml10ObjectBuilder samlObjectBuilder,
                                      final ArgumentExtractor samlArgumentExtractor,
                                      final String encoding,
                                      final int skewAllowance,
                                      final int issueLength,
                                      final AuthenticationAttributeReleasePolicy authAttrReleasePolicy,
                                      final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                      final CasProtocolAttributesRenderer attributesRenderer) {
        super(successResponse, protocolAttributeEncoder, servicesManager, authAttrReleasePolicy, serviceSelectionStrategy, attributesRenderer);
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
        try {
            response.setCharacterEncoding(this.encoding);
            val service = this.samlArgumentExtractor.extractService(request);
            val serviceId = getServiceIdFromRequest(service);

            LOGGER.debug("Using [{}] as the recipient of the SAML response for [{}]", serviceId, service);
            val samlResponse = this.samlObjectBuilder.newResponse(
                this.samlObjectBuilder.generateSecureRandomId(),
                ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(this.skewAllowance), serviceId, service);
            LOGGER.debug("Created SAML response for service [{}]", serviceId);
            prepareResponse(samlResponse, model);
            LOGGER.debug("Starting to encode SAML response for service [{}]", serviceId);
            this.samlObjectBuilder.encodeSamlResponse(response, request, samlResponse);
        } catch (final Exception e) {
            LOGGER.error("Error generating SAML response for service", e);
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

    private static String getServiceIdFromRequest(final Service service) {
        if (service == null || StringUtils.isBlank(service.getId())) {
            return "UNKNOWN";
        }
        try {
            return new URL(service.getId()).getHost();
        } catch (final MalformedURLException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }
}
