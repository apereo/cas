package org.jasig.cas.support.saml.authentication.principal;

import org.jasig.cas.authentication.principal.AbstractWebApplicationServiceResponseBuilder;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.jasig.cas.util.ApplicationContextProvider;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.springframework.context.ApplicationContext;

import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds the google accounts service response.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GoogleAccountsServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {

    private static final long serialVersionUID = -4584732364007702423L;

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final GoogleSaml20ObjectBuilder samlObjectBuilder;
    private int skewAllowance;

    /**
     * Instantiates a new Google accounts service response builder.
     *
     * @param privateKey the private key
     * @param publicKey the public key
     * @param samlObjectBuilder the saml object builder
     */
    public GoogleAccountsServiceResponseBuilder(final PrivateKey privateKey,
                                                final PublicKey publicKey,
                                                final GoogleSaml20ObjectBuilder samlObjectBuilder) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.samlObjectBuilder = samlObjectBuilder;
    }

    @Override
    public Response build(final WebApplicationService webApplicationService, final String ticketId) {
        final GoogleAccountsService service = (GoogleAccountsService) webApplicationService;

        final Map<String, String> parameters = new HashMap<>();
        final String samlResponse = constructSamlResponse(service);
        final String signedResponse = samlObjectBuilder.signSamlResponse(samlResponse,
            this.privateKey, this.publicKey);
        parameters.put(SamlProtocolConstants.PARAMETER_SAML_RESPONSE, signedResponse);
        parameters.put(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, service.getRelayState());

        return buildPost(service, parameters);
    }

    /**
     * Construct SAML response.
     * <a href="http://bit.ly/1uI8Ggu">See this reference for more info.</a>
     * @param service the service
     * @return the SAML response
     */
    protected String constructSamlResponse(final GoogleAccountsService service) {
        final DateTime currentDateTime = new DateTime();
        final DateTime notBeforeIssueInstant = DateTime.parse("2003-04-17T00:46:02Z");

        /*
         * Must be looked up directly from the context
         * because the services manager is not serializable
         * and cannot be class field.
         */
        final ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        final ServicesManager servicesManager = context.getBean("servicesManager", ServicesManager.class);
        final RegisteredService registeredService = servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }
        final String userId = registeredService.getUsernameAttributeProvider()
                    .resolveUsername(service.getPrincipal(), service);

        final org.opensaml.saml.saml2.core.Response response = samlObjectBuilder.newResponse(
            samlObjectBuilder.generateSecureRandomId(), currentDateTime, service.getId(), service);
        response.setStatus(samlObjectBuilder.newStatus(StatusCode.SUCCESS, null));

        final AuthnStatement authnStatement = samlObjectBuilder.newAuthnStatement(
            AuthnContext.PASSWORD_AUTHN_CTX, currentDateTime);
        final Assertion assertion = samlObjectBuilder.newAssertion(authnStatement,
            "https://www.opensaml.org/IDP",
            notBeforeIssueInstant, samlObjectBuilder.generateSecureRandomId());

        final Conditions conditions = samlObjectBuilder.newConditions(notBeforeIssueInstant,
                currentDateTime.plusSeconds(this.skewAllowance), service.getId());
        assertion.setConditions(conditions);

        final Subject subject = samlObjectBuilder.newSubject(NameID.EMAIL, userId,
            service.getId(), currentDateTime.plusSeconds(this.skewAllowance), service.getRequestId());
        assertion.setSubject(subject);

        response.getAssertions().add(assertion);

        final StringWriter writer = new StringWriter();
        samlObjectBuilder.marshalSamlXmlObject(response, writer);

        final String result = writer.toString();
        logger.debug("Generated Google SAML response: {}", result);
        return result;
    }

    /**
     * Sets the allowance for time skew in seconds
     * between CAS and the client server.  Default 0s.
     * This value will be subtracted from the current time when setting the SAML
     * {@code NotBeforeDate} attribute, thereby allowing for the
     * CAS server to be ahead of the client by as much as the value defined here.
     *
     * @param skewAllowance Number of seconds to allow for variance.
     */
    public void setSkewAllowance(final int skewAllowance) {
        logger.debug("Using {} seconds as skew allowance.", skewAllowance);
        this.skewAllowance = skewAllowance;
    }
}
