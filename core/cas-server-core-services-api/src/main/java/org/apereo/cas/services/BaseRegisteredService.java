package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.util.Assert;
import jakarta.persistence.Transient;

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
@EqualsAndHashCode(exclude = {"patternServiceId", "id"})
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Accessors(chain = true)
public abstract class BaseRegisteredService implements RegisteredService {

    private static final Comparator<RegisteredService> INTERNAL_COMPARATOR = Comparator
        .comparingInt(RegisteredService::getEvaluationPriority)
        .thenComparingInt(RegisteredService::getEvaluationOrder)
        .thenComparing(service -> StringUtils.defaultString(service.getName()), String.CASE_INSENSITIVE_ORDER)
        .thenComparing(RegisteredService::getServiceId)
        .thenComparingLong(RegisteredService::getId);

    @Serial
    private static final long serialVersionUID = 7645279151115635245L;

    /**
     * The unique identifier for this service.
     */
    @RegularExpressionCapable
    private String serviceId;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Transient
    private transient Pattern patternServiceId;

    private String name;

    private String theme;

    private String locale;

    private String informationUrl;

    private String privacyUrl;

    private String templateName;

    @Id
    private long id = RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE;

    private String description;

    private RegisteredServiceExpirationPolicy expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();

    private RegisteredServiceTicketGrantingTicketExpirationPolicy ticketGrantingTicketExpirationPolicy;

    private int evaluationOrder;

    private RegisteredServiceUsernameAttributeProvider usernameAttributeProvider = new DefaultRegisteredServiceUsernameProvider();

    private RegisteredServiceLogoutType logoutType = RegisteredServiceLogoutType.BACK_CHANNEL;

    private Set<String> environments = new LinkedHashSet<>();

    private RegisteredServiceAttributeReleasePolicy attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();

    @JsonProperty("multifactorPolicy")
    private RegisteredServiceMultifactorPolicy multifactorAuthenticationPolicy = new DefaultRegisteredServiceMultifactorPolicy();

    private RegisteredServicePublicKey publicKey;

    private RegisteredServiceMatchingStrategy matchingStrategy = new FullRegexRegisteredServiceMatchingStrategy();

    private String logo;

    private String logoutUrl;

    private RegisteredServiceAccessStrategy accessStrategy = new DefaultRegisteredServiceAccessStrategy();

    private RegisteredServiceAuthenticationPolicy authenticationPolicy = new DefaultRegisteredServiceAuthenticationPolicy();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, RegisteredServiceProperty> properties = new HashMap<>();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<RegisteredServiceContact> contacts = new ArrayList<>();

    @Override
    public int compareTo(@NonNull final RegisteredService other) {
        return INTERNAL_COMPARATOR.compare(this, other);
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

    /**
     * Mark as internal base registered service.
     *
     * @return the base registered service
     */
    @CanIgnoreReturnValue
    @JsonIgnore
    public BaseRegisteredService markAsInternal() {
        getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.INTERNAL_SERVICE_DEFINITION.getPropertyName(),
            new DefaultRegisteredServiceProperty("true"));
        return this;
    }

    /**
     * Set the service identifier and pre-compute its regex pattern.
     *
     * @param serviceId the service id
     */
    @CanIgnoreReturnValue
    public BaseRegisteredService setServiceId(final String serviceId) {
        Assert.notNull(serviceId, "Service id cannot be null");
        this.serviceId = serviceId;
        this.patternServiceId = RegexUtils.createPattern(serviceId);
        return this;
    }

    @Override
    public Pattern compileServiceIdPattern() {
        if (this.patternServiceId == null) {
            setServiceId(this.serviceId);
        }
        return this.patternServiceId;
    }
}
