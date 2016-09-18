package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.authentication.principal.AbstractWebApplicationServiceResponseBuilder;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.apereo.cas.util.ApplicationContextProvider;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds the google accounts service response.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GoogleAccountsServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {

    private static final long serialVersionUID = -4584732364007702423L;
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAccountsServiceResponseBuilder.class);
    
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private GoogleSaml20ObjectBuilder samlObjectBuilder;
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
        Assert.notNull(privateKey);
        Assert.notNull(publicKey);
        Assert.notNull(samlObjectBuilder);
        
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.samlObjectBuilder = samlObjectBuilder;
    }

    @Override
    public Response build(final WebApplicationService webApplicationService, final String ticketId) {
        final GoogleAccountsService service = (GoogleAccountsService) webApplicationService;

        final Map<String, String> parameters = new HashMap<>();
        final String samlResponse = constructSamlResponse(service);
        final String signedResponse = this.samlObjectBuilder.signSamlResponse(samlResponse,
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
        final ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime notBeforeIssueInstant = ZonedDateTime.parse("2003-04-17T00:46:02Z");

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

        final org.opensaml.saml.saml2.core.Response response = this.samlObjectBuilder.newResponse(
                this.samlObjectBuilder.generateSecureRandomId(), currentDateTime, service.getId(), service);
        response.setStatus(this.samlObjectBuilder.newStatus(StatusCode.SUCCESS, null));

        final AuthnStatement authnStatement = this.samlObjectBuilder.newAuthnStatement(
            AuthnContext.PASSWORD_AUTHN_CTX, currentDateTime);
        final Assertion assertion = this.samlObjectBuilder.newAssertion(authnStatement,
            "https://www.opensaml.org/IDP",
            notBeforeIssueInstant, this.samlObjectBuilder.generateSecureRandomId());

        final Conditions conditions = this.samlObjectBuilder.newConditions(notBeforeIssueInstant,
                currentDateTime.plusSeconds(this.skewAllowance), service.getId());
        assertion.setConditions(conditions);

        final Subject subject = this.samlObjectBuilder.newSubject(NameID.EMAIL, userId,
            service.getId(), currentDateTime.plusSeconds(this.skewAllowance), service.getRequestId());
        assertion.setSubject(subject);

        response.getAssertions().add(assertion);

        final StringWriter writer = new StringWriter();
        this.samlObjectBuilder.marshalSamlXmlObject(response, writer);

        final String result = writer.toString();
        LOGGER.debug("Generated Google SAML response: {}", result);
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
        LOGGER.debug("Using {} seconds as skew allowance.", skewAllowance);
        this.skewAllowance = skewAllowance;
    }
}
