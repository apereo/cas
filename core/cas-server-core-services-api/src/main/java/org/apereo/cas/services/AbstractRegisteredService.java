package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for mutable, persistable registered services.
 *
 * @author Marvin S. Addison
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Slf4j
public abstract class AbstractRegisteredService implements RegisteredService {

    private static final long serialVersionUID = 7645279151115635245L;

    /**
     * The unique identifier for this service.
     */
    protected String serviceId;

    private String name;

    private String theme;

    private String locale;
    
    private String informationUrl;

    private String privacyUrl;

    private String responseType;

    @Id
    private long id = RegisteredService.INITIAL_IDENTIFIER_VALUE;

    private String description;

    private RegisteredServiceExpirationPolicy expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();

    private RegisteredServiceAcceptableUsagePolicy acceptableUsagePolicy = new DefaultRegisteredServiceAcceptableUsagePolicy();

    private RegisteredServiceProxyPolicy proxyPolicy = new RefuseRegisteredServiceProxyPolicy();

    private RegisteredServiceProxyTicketExpirationPolicy proxyTicketExpirationPolicy;

    private RegisteredServiceProxyGrantingTicketExpirationPolicy proxyGrantingTicketExpirationPolicy;

    private RegisteredServiceTicketGrantingTicketExpirationPolicy ticketGrantingTicketExpirationPolicy;

    private RegisteredServiceServiceTicketExpirationPolicy serviceTicketExpirationPolicy;

    private RegisteredServiceSingleSignOnParticipationPolicy singleSignOnParticipationPolicy;

    private RegisteredServiceWebflowInterruptPolicy webflowInterruptPolicy = new DefaultRegisteredServiceWebflowInterruptPolicy();
    
    private int evaluationOrder;

    private RegisteredServiceUsernameAttributeProvider usernameAttributeProvider = new DefaultRegisteredServiceUsernameProvider();

    private RegisteredServiceLogoutType logoutType = RegisteredServiceLogoutType.BACK_CHANNEL;

    private HashSet<String> environments = new HashSet<>(0);

    private RegisteredServiceAttributeReleasePolicy attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();

    private RegisteredServiceMultifactorPolicy multifactorPolicy = new DefaultRegisteredServiceMultifactorPolicy();

    private RegisteredServiceMatchingStrategy matchingStrategy = new FullRegexRegisteredServiceMatchingStrategy();

    private String logo;

    private String logoutUrl;

    private String redirectUrl;

    private RegisteredServiceAccessStrategy accessStrategy = new DefaultRegisteredServiceAccessStrategy();

    private RegisteredServicePublicKey publicKey;

    private RegisteredServiceAuthenticationPolicy authenticationPolicy = new DefaultRegisteredServiceAuthenticationPolicy();

    private Map<String, RegisteredServiceProperty> properties = new HashMap<>(0);

    private List<RegisteredServiceContact> contacts = new ArrayList<>(0);

    /**
     * Sets the service identifier. Extensions are to define the format.
     *
     * @param id the new service id
     */
    public abstract void setServiceId(String id);

    @Override
    public int compareTo(final RegisteredService other) {
        return new CompareToBuilder()
            .append(getEvaluationPriority(), other.getEvaluationPriority())
            .append(getEvaluationOrder(), other.getEvaluationOrder())
            .append(StringUtils.defaultIfBlank(getName(), StringUtils.EMPTY).toLowerCase(),
                StringUtils.defaultIfBlank(other.getName(), StringUtils.EMPTY).toLowerCase())
            .append(getServiceId(), other.getServiceId()).append(getId(), other.getId())
            .toComparison();
    }

    @Override
    @Deprecated(since = "6.2.0")
    @JsonIgnore
    public Set<String> getRequiredHandlers() {
        LOGGER.debug("Assigning a collection of required authentication handlers to a registered service is deprecated. "
            + "This field is scheduled to be removed in the future. If you need to, consider defining an authentication policy "
            + "for the registered service instead to specify required authentication handlers");
        return getAuthenticationPolicy().getRequiredAuthenticationHandlers();
    }

    /**
     * Sets required handlers.
     *
     * @param requiredHandlers the required handlers
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2.0")
    @JsonIgnore
    public void setRequiredHandlers(final Set<String> requiredHandlers) {
        if (requiredHandlers != null) {
            LOGGER.debug("Assigning a collection of required authentication handlers to a registered service is deprecated. "
                + "This field is scheduled to be removed in the future. If you need to, consider defining an authentication policy "
                + "for the registered service instead to specify required authentication handlers [{}]", requiredHandlers);
            initialize();
            getAuthenticationPolicy().getRequiredAuthenticationHandlers().addAll(requiredHandlers);
        }
    }

    @Override
    public void initialize() {
        this.proxyPolicy = ObjectUtils.defaultIfNull(this.proxyPolicy, new RefuseRegisteredServiceProxyPolicy());
        this.usernameAttributeProvider = ObjectUtils.defaultIfNull(this.usernameAttributeProvider, new DefaultRegisteredServiceUsernameProvider());
        this.logoutType = ObjectUtils.defaultIfNull(this.logoutType, RegisteredServiceLogoutType.BACK_CHANNEL);
        this.accessStrategy = ObjectUtils.defaultIfNull(this.accessStrategy, new DefaultRegisteredServiceAccessStrategy());
        this.multifactorPolicy = ObjectUtils.defaultIfNull(this.multifactorPolicy, new DefaultRegisteredServiceMultifactorPolicy());
        this.properties = ObjectUtils.defaultIfNull(this.properties, new LinkedHashMap<>(0));
        this.attributeReleasePolicy = ObjectUtils.defaultIfNull(this.attributeReleasePolicy, new ReturnAllowedAttributeReleasePolicy());
        this.contacts = ObjectUtils.defaultIfNull(this.contacts, new ArrayList<>(0));
        this.expirationPolicy = ObjectUtils.defaultIfNull(this.expirationPolicy, new DefaultRegisteredServiceExpirationPolicy());
        this.acceptableUsagePolicy = ObjectUtils.defaultIfNull(this.acceptableUsagePolicy, new DefaultRegisteredServiceAcceptableUsagePolicy());
        this.authenticationPolicy = ObjectUtils.defaultIfNull(this.authenticationPolicy, new DefaultRegisteredServiceAuthenticationPolicy());
        this.matchingStrategy = ObjectUtils.defaultIfNull(this.matchingStrategy, new FullRegexRegisteredServiceMatchingStrategy());
        this.webflowInterruptPolicy = ObjectUtils.defaultIfNull(this.webflowInterruptPolicy, new DefaultRegisteredServiceWebflowInterruptPolicy());
    }

    /**
     * Create a new service instance.
     *
     * @return the registered service
     */
    protected abstract AbstractRegisteredService newInstance();
}
