package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.saml.saml1.core.AuthenticationStatement;
import org.opensaml.saml.saml1.core.Conditions;
import org.opensaml.saml.saml1.core.Response;
import org.opensaml.saml.saml1.core.StatusCode;
import org.opensaml.saml.saml1.core.Subject;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a view to return a SAML SOAP response and assertion, based on
 * the SAML 1.1 specification.
 * <p>
 * If an AttributePrincipal is supplied, then the assertion will include the
 * attributes from it (assuming a String key/Object value pair). The only
 * Authentication attribute it will look at is the authMethod (if supplied).
 * <p>
 * Note that this class will currently not handle proxy authentication.
 * <p>
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public class Saml10SuccessResponseView extends AbstractSaml10ResponseView {
    private static final int DEFAULT_ISSUE_LENGTH = 30000;

    /** The issuer, generally the hostname. */
    
    private String issuer;

    /**
     * The amount of time in milliseconds this is valid for.
     * Defaults to {@value}.
     **/
    private long issueLength = DEFAULT_ISSUE_LENGTH;

    private String rememberMeAttributeName = CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME;
    
    private String defaultAttributeNamespace;

    @Override
    protected void prepareResponse(final Response response, final Map<String, Object> model) {

        final ZonedDateTime issuedAt = DateTimeUtils.zonedDateTimeOf(response.getIssueInstant());
        final Service service = getAssertionFrom(model).getService();

        final Authentication authentication = getPrimaryAuthenticationFrom(model);
        final Collection<Object> authnMethods = CollectionUtils.convertValueToCollection(authentication.getAttributes()
                .get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
                
        final AuthenticationStatement authnStatement = this.samlObjectBuilder.newAuthenticationStatement(
                authentication.getAuthenticationDate(), authnMethods, getPrincipal(model).getId());

        final Assertion assertion = this.samlObjectBuilder.newAssertion(authnStatement, this.issuer, issuedAt,
                this.samlObjectBuilder.generateSecureRandomId());
        final Conditions conditions = this.samlObjectBuilder.newConditions(issuedAt, service.getId(), this.issueLength);
        assertion.setConditions(conditions);

        final Subject subject = this.samlObjectBuilder.newSubject(getPrincipal(model).getId());
        final Map<String, Object> attributesToSend = prepareSamlAttributes(model, service);

        if (!attributesToSend.isEmpty()) {
            assertion.getAttributeStatements().add(this.samlObjectBuilder.newAttributeStatement(
                    subject, attributesToSend, this.defaultAttributeNamespace));
        }

        response.setStatus(this.samlObjectBuilder.newStatus(StatusCode.SUCCESS, null));
        response.getAssertions().add(assertion);
    }


    /**
     * Prepare saml attributes. Combines both principal and authentication
     * attributes. If the authentication is to be remembered, uses {@link #setRememberMeAttributeName(String)}
     * for the remember-me attribute name.
     *
     * @param model the model
     * @return the final map
     * @since 4.1.0
     */
    private Map<String, Object> prepareSamlAttributes(final Map<String, Object> model, final Service service) {
        final Map<String, Object> authnAttributes = new HashMap<>(getAuthenticationAttributesAsMultiValuedAttributes(model));
        if (isRememberMeAuthentication(model)) {
            authnAttributes.remove(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
            authnAttributes.put(this.rememberMeAttributeName, Boolean.TRUE.toString());
        }
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        final Map<String, Object> attributesToReturn = new HashMap<>();
        attributesToReturn.putAll(getPrincipalAttributesAsMultiValuedAttributes(model));
        attributesToReturn.putAll(authnAttributes);

        decideIfCredentialPasswordShouldBeReleasedAsAttribute(attributesToReturn, model, registeredService);
        decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(attributesToReturn, model, registeredService);

        final Map<String, Object> finalAttributes = this.casAttributeEncoder.encodeAttributes(attributesToReturn, service);
        return finalAttributes;
    }

    public void setIssueLength(final long issueLength) {
        this.issueLength = issueLength;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public void setRememberMeAttributeName(final String rememberMeAttributeName) {
        this.rememberMeAttributeName = rememberMeAttributeName;
    }

    public void setDefaultAttributeNamespace(final String defaultAttributeNamespace) {
        this.defaultAttributeNamespace = defaultAttributeNamespace;
    }
}
