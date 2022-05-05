package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@ToString
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Slf4j
public abstract class BaseRegisteredService implements RegisteredService {

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

    private RegisteredServiceTicketGrantingTicketExpirationPolicy ticketGrantingTicketExpirationPolicy;


    private int evaluationOrder;

    private RegisteredServiceUsernameAttributeProvider usernameAttributeProvider = new DefaultRegisteredServiceUsernameProvider();

    private RegisteredServiceLogoutType logoutType = RegisteredServiceLogoutType.BACK_CHANNEL;

    private HashSet<String> environments = new HashSet<>(0);

    private RegisteredServiceAttributeReleasePolicy attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();

    @JsonProperty("multifactorPolicy")
    private RegisteredServiceMultifactorPolicy multifactorAuthenticationPolicy = new DefaultRegisteredServiceMultifactorPolicy();

    private RegisteredServicePublicKey publicKey;

    private RegisteredServiceMatchingStrategy matchingStrategy = new FullRegexRegisteredServiceMatchingStrategy();

    private String logo;

    private String logoutUrl;
    
    private RegisteredServiceAccessStrategy accessStrategy = new DefaultRegisteredServiceAccessStrategy();
    
    private RegisteredServiceAuthenticationPolicy authenticationPolicy = new DefaultRegisteredServiceAuthenticationPolicy();

    private Map<String, RegisteredServiceProperty> properties = new HashMap<>(0);

    private List<RegisteredServiceContact> contacts = new ArrayList<>(0);

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
        this.usernameAttributeProvider = ObjectUtils.defaultIfNull(this.usernameAttributeProvider, new DefaultRegisteredServiceUsernameProvider());
        this.logoutType = ObjectUtils.defaultIfNull(this.logoutType, RegisteredServiceLogoutType.BACK_CHANNEL);
        this.accessStrategy = ObjectUtils.defaultIfNull(this.accessStrategy, new DefaultRegisteredServiceAccessStrategy());
        this.multifactorAuthenticationPolicy = ObjectUtils.defaultIfNull(this.multifactorAuthenticationPolicy, new DefaultRegisteredServiceMultifactorPolicy());
        this.properties = ObjectUtils.defaultIfNull(this.properties, new LinkedHashMap<>(0));
        this.attributeReleasePolicy = ObjectUtils.defaultIfNull(this.attributeReleasePolicy, new ReturnAllowedAttributeReleasePolicy());
        this.contacts = ObjectUtils.defaultIfNull(this.contacts, new ArrayList<>(0));
        this.expirationPolicy = ObjectUtils.defaultIfNull(this.expirationPolicy, new DefaultRegisteredServiceExpirationPolicy());
        this.authenticationPolicy = ObjectUtils.defaultIfNull(this.authenticationPolicy, new DefaultRegisteredServiceAuthenticationPolicy());
        this.matchingStrategy = ObjectUtils.defaultIfNull(this.matchingStrategy, new FullRegexRegisteredServiceMatchingStrategy());
    }

    @Override
    public boolean matches(final Service service) {
        return service != null && matches(service.getId());
    }

    @Override
    public boolean matches(final String serviceId) {
        configureMatchingStrategy();
        return !StringUtils.isBlank(serviceId) && getMatchingStrategy().matches(this, serviceId);
    }

    /**
     * Configure matching strategy.
     * If the strategy is undefined, it will default to {@link FullRegexRegisteredServiceMatchingStrategy}.
     */
    protected void configureMatchingStrategy() {
        if (getMatchingStrategy() == null) {
            setMatchingStrategy(new FullRegexRegisteredServiceMatchingStrategy());
        }
    }
}
