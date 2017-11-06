package org.apereo.cas.services.web.view;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.AbstractView;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract class to handle retrieving the Assertion from the model.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public abstract class AbstractCasView extends AbstractView {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCasView.class);

    /**
     * Indicate whether this view will be generating the success response or not.
     * By default, the view is treated as a failure.
     */
    protected final boolean successResponse;

    /**
     * The attribute encoder instance.
     */
    protected final ProtocolAttributeEncoder protocolAttributeEncoder;

    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;


    /**
     * authentication context attribute name.
     */
    protected final String authenticationContextAttribute;

    /**
     * Authentication attribute release policy.
     */
    protected final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    public AbstractCasView(final boolean successResponse,
                           final ProtocolAttributeEncoder protocolAttributeEncoder,
                           final ServicesManager servicesManager,
                           final String authenticationContextAttribute,
                           final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
        this.successResponse = successResponse;
        this.protocolAttributeEncoder = protocolAttributeEncoder;
        this.servicesManager = servicesManager;
        this.authenticationContextAttribute = authenticationContextAttribute;
        this.authenticationAttributeReleasePolicy = authenticationAttributeReleasePolicy;
    }

    /**
     * Gets the assertion from the model.
     *
     * @param model the model
     * @return the assertion from
     */
    protected Assertion getAssertionFrom(final Map<String, Object> model) {
        return (Assertion) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION);
    }

    /**
     * Gets error code from.
     *
     * @param model the model
     * @return the error code from
     */
    protected String getErrorCodeFrom(final Map<String, Object> model) {
        return model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_CODE).toString();
    }

    /**
     * Gets error description from.
     *
     * @param model the model
     * @return the error description from
     */
    protected String getErrorDescriptionFrom(final Map<String, Object> model) {
        return model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_DESCRIPTION).toString();
    }

    /**
     * Gets the PGT from the model.
     *
     * @param model the model
     * @return the pgt id
     */
    protected String getProxyGrantingTicketId(final Map<String, Object> model) {
        return (String) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
    }

    /**
     * Gets the PGT-IOU from the model.
     *
     * @param model the model
     * @return the pgt-iou id
     */
    protected String getProxyGrantingTicketIou(final Map<String, Object> model) {
        return (String) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU);
    }


    /**
     * Gets the authentication from the model.
     *
     * @param model the model
     * @return the assertion from
     * @since 4.1.0
     */
    protected Authentication getPrimaryAuthenticationFrom(final Map<String, Object> model) {
        return getAssertionFrom(model).getPrimaryAuthentication();
    }

    /**
     * Gets model attributes.
     *
     * @param model the model
     * @return the model attributes
     */
    protected Map<String, Object> getModelAttributes(final Map<String, Object> model) {
        return (Map<String, Object>) model.get(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES);
    }

    /**
     * Gets authentication attributes from the primary authentication object.
     *
     * @param model the model
     * @return the authentication attribute
     */
    protected Map<String, Object> getAuthenticationAttributes(final Map<String, Object> model) {
        final Authentication authn = getPrimaryAuthenticationFrom(model);
        return authn.getAttributes();
    }

    /**
     * Gets an authentication attribute from the primary authentication object.
     *
     * @param model         the model
     * @param attributeName the attribute name
     * @return the authentication attribute
     */
    protected String getAuthenticationAttribute(final Map<String, Object> model, final String attributeName) {
        final Authentication authn = getPrimaryAuthenticationFrom(model);
        return (String) authn.getAttributes().get(attributeName);
    }

    /**
     * Gets the principal from the model.
     *
     * @param model the model
     * @return the assertion from
     * @since 4.1.0
     */
    protected Principal getPrincipal(final Map<String, Object> model) {
        return getPrimaryAuthenticationFrom(model).getPrincipal();
    }

    /**
     * Gets principal attributes.
     * Single-valued attributes are converted to a collection
     * so the review can easily loop through all.
     *
     * @param model the model
     * @return the attributes
     * @see #convertAttributeValuesToMultiValuedObjects(java.util.Map)
     * @since 4.1.0
     */
    protected Map<String, Object> getPrincipalAttributesAsMultiValuedAttributes(final Map<String, Object> model) {
        return convertAttributeValuesToMultiValuedObjects(getPrincipal(model).getAttributes());
    }

    /**
     * Gets authentication attributes.
     * Single-valued attributes are converted to a collection
     * so the review can easily loop through all.
     *
     * @param model the model
     * @return the attributes
     * @see #convertAttributeValuesToMultiValuedObjects(java.util.Map)
     * @since 4.1.0
     */
    protected Map<String, Object> getAuthenticationAttributesAsMultiValuedAttributes(final Map<String, Object> model) {
        return convertAttributeValuesToMultiValuedObjects(getPrimaryAuthenticationFrom(model).getAttributes());
    }

    /**
     * Is remember me authentication?
     * looks at the authentication object to find {@link RememberMeCredential#AUTHENTICATION_ATTRIBUTE_REMEMBER_ME}
     * and expects the assertion to also note a new login session.
     *
     * @param model the model
     * @return true if remember-me, false if otherwise.
     */
    protected boolean isRememberMeAuthentication(final Map<String, Object> model) {
        final Map<String, Object> authnAttributes = getAuthenticationAttributesAsMultiValuedAttributes(model);
        final Collection authnMethod = (Collection) authnAttributes.get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
        return authnMethod != null && authnMethod.contains(Boolean.TRUE) && isAssertionBackedByNewLogin(model);
    }


    /**
     * Gets satisfied multifactor authentication provider.
     *
     * @param model the model
     * @return the satisfied multifactor authentication provider
     */
    protected String getSatisfiedMultifactorAuthenticationProviderId(final Map<String, Object> model) {
        if (StringUtils.isNotBlank(authenticationContextAttribute) && model.containsKey(this.authenticationContextAttribute)) {
            return model.get(this.authenticationContextAttribute).toString();
        }
        return null;
    }

    /**
     * Is assertion backed by new login?
     *
     * @param model the model
     * @return true/false.
     */
    protected boolean isAssertionBackedByNewLogin(final Map<String, Object> model) {
        return getAssertionFrom(model).isFromNewLogin();
    }

    /**
     * Convert attribute values to multi valued objects.
     *
     * @param attributes the attributes
     * @return the map of attributes to return
     */
    private static Map<String, Object> convertAttributeValuesToMultiValuedObjects(final Map<String, Object> attributes) {
        final Set<Map.Entry<String, Object>> entries = attributes.entrySet();
        return entries.stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            final Object value = entry.getValue();
            if (value instanceof Collection || value instanceof Map || value instanceof Object[]
                    || value instanceof Iterator || value instanceof Enumeration) {
                return value;
            }
            return CollectionUtils.wrap(value);

        }));
    }

    /**
     * Gets authentication date.
     *
     * @param model the model
     * @return the authentication date
     * @since 4.1.0
     */
    protected ZonedDateTime getAuthenticationDate(final Map<String, Object> model) {
        return getPrimaryAuthenticationFrom(model).getAuthenticationDate();
    }

    /**
     * Gets validated service from the model.
     *
     * @param model the model
     * @return the validated service from
     */
    protected Service getServiceFrom(final Map<String, Object> model) {
        return (Service) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE);
    }

    /**
     * Gets chained authentications.
     * Note that the last index in the list always describes the primary authentication
     * event. All others in the chain should denote proxies. Per the CAS protocol,
     * when authentication has proceeded through multiple proxies,
     * the order in which the proxies were traversed MUST be reflected in the response.
     * The most recently-visited proxy MUST be the first proxy listed, and all the
     * other proxies MUST be shifted down as new proxies are added.
     *
     * @param model the model
     * @return the chained authentications
     */
    protected Collection<Authentication> getChainedAuthentications(final Map<String, Object> model) {
        final Assertion assertion = getAssertionFrom(model);
        final List<Authentication> chainedAuthentications = assertion.getChainedAuthentications();
        return chainedAuthentications.stream().limit(chainedAuthentications.size() - 1).collect(Collectors.toList());
    }

    /**
     * Decide if credential password should be released as attribute.
     * The credential must have been cached as an authentication attribute
     * and the attribute release policy must be allowed to release the
     * attribute.
     *
     * @param attributes the attributes
     * @param model      the model
     * @param service    the service
     */
    protected void decideIfCredentialPasswordShouldBeReleasedAsAttribute(final Map<String, Object> attributes,
                                                                         final Map<String, Object> model,
                                                                         final RegisteredService service) {

        final RegisteredServiceAttributeReleasePolicy policy = service.getAttributeReleasePolicy();
        final boolean isAuthorized = policy != null && policy.isAuthorizedToReleaseCredentialPassword();

        decideAttributeReleaseBasedOnServiceAttributePolicy(attributes,
                getAuthenticationAttribute(model, CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL),
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL,
                service, isAuthorized);
    }

    /**
     * Decide if PGT should be released as attribute.
     * The PGT must have been cached as an authentication attribute
     * and the attribute release policy must be allowed to release the
     * attribute.
     *
     * @param attributes the attributes
     * @param model      the model
     * @param service    the service
     */
    protected void decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(final Map<String, Object> attributes,
                                                                          final Map<String, Object> model,
                                                                          final RegisteredService service) {
        final RegisteredServiceAttributeReleasePolicy policy = service.getAttributeReleasePolicy();
        final boolean isAuthorized = policy != null && policy.isAuthorizedToReleaseProxyGrantingTicket();

        decideAttributeReleaseBasedOnServiceAttributePolicy(attributes,
                getProxyGrantingTicketId(model),
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET,
                service, isAuthorized);
    }

    /**
     * Decide attribute release based on service attribute policy.
     *
     * @param attributes               the attributes
     * @param attributeValue           the attribute value
     * @param attributeName            the attribute name
     * @param service                  the service
     * @param doesAttributePolicyAllow does attribute policy allow release of this attribute?
     */
    protected void decideAttributeReleaseBasedOnServiceAttributePolicy(final Map<String, Object> attributes,
                                                                       final String attributeValue,
                                                                       final String attributeName,
                                                                       final RegisteredService service,
                                                                       final boolean doesAttributePolicyAllow) {
        if (StringUtils.isNotBlank(attributeValue)) {
            LOGGER.debug("Obtained [{}] as an authentication attribute", attributeName);

            if (doesAttributePolicyAllow) {
                LOGGER.debug("Obtained [{}] is passed to the CAS validation payload", attributeName);
                attributes.put(attributeName, CollectionUtils.wrap(attributeValue));
            } else {
                LOGGER.debug("Attribute release policy for [{}] does not authorize the release of [{}]",
                        service.getServiceId(), attributeName);
                attributes.remove(attributeName);
            }
        } else {
            LOGGER.trace("[{}] is not available and will not be released to the validation response.", attributeName);
        }
    }


    /**
     * Put into model.
     *
     * @param model the model
     * @param key   the key
     * @param value the value
     */
    protected void putIntoModel(final Map<String, Object> model, final String key, final Object value) {
        model.put(key, value);
    }

    /**
     * Put all into model.
     *
     * @param model  the model
     * @param values the values
     */
    protected void putAllIntoModel(final Map<String, Object> model, final Map<String, Object> values) {
        model.putAll(values);
    }

    public ProtocolAttributeEncoder getProtocolAttributeEncoder() {
        return this.protocolAttributeEncoder;
    }

    public ServicesManager getServicesManager() {
        return this.servicesManager;
    }

    public String getAuthenticationContextAttribute() {
        return authenticationContextAttribute;
    }

}
