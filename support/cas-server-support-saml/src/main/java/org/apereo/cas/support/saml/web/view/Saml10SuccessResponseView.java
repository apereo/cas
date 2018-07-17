package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml1.core.Response;
import org.opensaml.saml.saml1.core.StatusCode;

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
@Slf4j
public class Saml10SuccessResponseView extends AbstractSaml10ResponseView {


    private final String issuer;
    private final String rememberMeAttributeName;
    private final String defaultAttributeNamespace;

    public Saml10SuccessResponseView(final ProtocolAttributeEncoder protocolAttributeEncoder,
                                     final ServicesManager servicesManager,
                                     final String authenticationContextAttribute,
                                     final Saml10ObjectBuilder samlObjectBuilder,
                                     final ArgumentExtractor samlArgumentExtractor,
                                     final String encoding,
                                     final int skewAllowance,
                                     final int issueLength,
                                     final String issuer,
                                     final String defaultAttributeNamespace,
                                     final AuthenticationAttributeReleasePolicy authAttrReleasePolicy) {
        super(true, protocolAttributeEncoder, servicesManager, authenticationContextAttribute, samlObjectBuilder,
            samlArgumentExtractor, encoding, skewAllowance, issueLength, authAttrReleasePolicy);
        this.issuer = issuer;
        this.rememberMeAttributeName = CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME;
        this.defaultAttributeNamespace = defaultAttributeNamespace;
    }

    @Override
    protected void prepareResponse(final Response response, final Map<String, Object> model) {

        val issuedAt = DateTimeUtils.zonedDateTimeOf(response.getIssueInstant());
        val service = getAssertionFrom(model).getService();
        LOGGER.debug("Preparing SAML response for service [{}]", service);

        val authentication = getPrimaryAuthenticationFrom(model);
        final Collection<Object> authnMethods = CollectionUtils.toCollection(authentication.getAttributes()
            .get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
        LOGGER.debug("Authentication methods found are [{}]", authnMethods);

        val principal = getPrincipal(model);
        val authnStatement = this.samlObjectBuilder.newAuthenticationStatement(
            authentication.getAuthenticationDate(), authnMethods, principal.getId());
        LOGGER.debug("Built authentication statement for [{}] dated at [{}]", principal, authentication.getAuthenticationDate());

        val assertion = this.samlObjectBuilder.newAssertion(authnStatement, this.issuer, issuedAt,
            this.samlObjectBuilder.generateSecureRandomId());
        LOGGER.debug("Built assertion for issuer [{}] dated at [{}]", this.issuer, issuedAt);

        val conditions = this.samlObjectBuilder.newConditions(issuedAt, service.getId(), this.issueLength);
        assertion.setConditions(conditions);
        LOGGER.debug("Built assertion conditions for issuer [{}] and service [{}] ", this.issuer, service.getId());

        val subject = this.samlObjectBuilder.newSubject(principal.getId());
        LOGGER.debug("Built subject for principal [{}]", principal);

        val attributesToSend = prepareSamlAttributes(model, service);
        LOGGER.debug("Authentication statement shall include these attributes [{}]", attributesToSend);

        if (!attributesToSend.isEmpty()) {
            assertion.getAttributeStatements().add(this.samlObjectBuilder.newAttributeStatement(
                subject, attributesToSend, this.defaultAttributeNamespace));
        }

        response.setStatus(this.samlObjectBuilder.newStatus(StatusCode.SUCCESS, null));
        LOGGER.debug("Set response status code to [{}]", response.getStatus());

        response.getAssertions().add(assertion);
    }


    /**
     * Prepare saml attributes. Combines both principal and authentication
     * attributes. If the authentication is to be remembered, uses {@link #rememberMeAttributeName}
     * for the remember-me attribute name.
     *
     * @param model the model
     * @return the final map
     * @since 4.1.0
     */
    private Map<String, Object> prepareSamlAttributes(final Map<String, Object> model, final Service service) {
        val authnAttributes = authenticationAttributeReleasePolicy
            .getAuthenticationAttributesForRelease(getPrimaryAuthenticationFrom(model));
        if (isRememberMeAuthentication(model)) {
            authnAttributes.put(this.rememberMeAttributeName, Boolean.TRUE.toString());
        }
        LOGGER.debug("Retrieved authentication attributes [{}] from the model", authnAttributes);

        val registeredService = this.servicesManager.findServiceBy(service);
        val attributesToReturn = new HashMap<String, Object>();
        attributesToReturn.putAll(getPrincipalAttributesAsMultiValuedAttributes(model));
        attributesToReturn.putAll(authnAttributes);

        decideIfCredentialPasswordShouldBeReleasedAsAttribute(attributesToReturn, model, registeredService);
        decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(attributesToReturn, model, registeredService);

        LOGGER.debug("Beginning to encode attributes [{}] for service [{}]", attributesToReturn, registeredService.getServiceId());
        val finalAttributes = this.protocolAttributeEncoder.encodeAttributes(attributesToReturn, registeredService);
        LOGGER.debug("Final collection of attributes are [{}]", finalAttributes);

        return finalAttributes;
    }

}
