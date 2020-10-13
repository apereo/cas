package org.apereo.cas.services;

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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

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
@Entity
@Inheritance
@DiscriminatorColumn(name = "expression_type", length = 50,
    discriminatorType = DiscriminatorType.STRING,
    columnDefinition = "VARCHAR(50) DEFAULT 'regex'")
@Table(name = "RegexRegisteredService")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public abstract class AbstractRegisteredService implements RegisteredService {

    private static final long serialVersionUID = 7645279151115635245L;

    /**
     * The unique identifier for this service.
     */
    @Column(nullable = false)
    protected String serviceId;

    @Column(nullable = false)
    private String name;

    @Column
    private String theme;

    @Column
    private String informationUrl;

    @Column
    private String privacyUrl;

    @Column
    private String responseType;

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    private long id = RegisteredService.INITIAL_IDENTIFIER_VALUE;

    @Column
    private String description;

    @Lob
    @Column(name = "expiration_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceExpirationPolicy expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();

    @Lob
    @Column(name = "acceptable_usage_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceAcceptableUsagePolicy acceptableUsagePolicy = new DefaultRegisteredServiceAcceptableUsagePolicy();

    @Lob
    @Column(name = "proxy_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceProxyPolicy proxyPolicy = new RefuseRegisteredServiceProxyPolicy();

    @Lob
    @Column(name = "proxy_ticket_expiration_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceProxyTicketExpirationPolicy proxyTicketExpirationPolicy;

    @Lob
    @Column(name = "proxy_granting_ticket_expiration_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceProxyGrantingTicketExpirationPolicy proxyGrantingTicketExpirationPolicy;

    @Lob
    @Column(name = "service_ticket_expiration_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceServiceTicketExpirationPolicy serviceTicketExpirationPolicy;

    @Lob
    @Column(name = "sso_participation_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceSingleSignOnParticipationPolicy singleSignOnParticipationPolicy;

    @Column(name = "evaluation_order", nullable = false)
    private int evaluationOrder;

    @Lob
    @Column(name = "username_attr", length = Integer.MAX_VALUE)
    private RegisteredServiceUsernameAttributeProvider usernameAttributeProvider = new DefaultRegisteredServiceUsernameProvider();

    @Column(name = "logout_type")
    private RegisteredServiceLogoutType logoutType = RegisteredServiceLogoutType.BACK_CHANNEL;

    @Lob
    @Column(name = "environments", length = Integer.MAX_VALUE)
    private HashSet<String> environments = new HashSet<>(0);

    @Lob
    @Column(name = "attribute_release", length = Integer.MAX_VALUE)
    private RegisteredServiceAttributeReleasePolicy attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();

    @Lob
    @Column(name = "mfa_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceMultifactorPolicy multifactorPolicy = new DefaultRegisteredServiceMultifactorPolicy();

    @Column
    private String logo;

    @Column(name = "logout_url")
    private String logoutUrl;

    @Column(name = "redirect_url")
    private String redirectUrl;

    @Lob
    @Column(name = "access_strategy", length = Integer.MAX_VALUE)
    private RegisteredServiceAccessStrategy accessStrategy = new DefaultRegisteredServiceAccessStrategy();

    @Lob
    @Column(name = "public_key", length = Integer.MAX_VALUE)
    private RegisteredServicePublicKey publicKey;

    @Lob
    @Column(name = "authn_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceAuthenticationPolicy authenticationPolicy = new DefaultRegisteredServiceAuthenticationPolicy();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "RegexRegisteredService_RegexRegisteredServiceProperty")
    @MapKeyColumn(name = "RegexRegisteredServiceProperty_name")
    @Column(name = "RegexRegisteredServiceProperty_value")
    private Map<String, DefaultRegisteredServiceProperty> properties = new HashMap<>(0);

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "RegexRegisteredService_RegisteredServiceImplContact")
    @OrderColumn
    private List<DefaultRegisteredServiceContact> contacts = new ArrayList<>(0);

    /**
     * Sets the service identifier. Extensions are to define the format.
     *
     * @param id the new service id
     */
    public abstract void setServiceId(String id);

    @Override
    public int compareTo(final RegisteredService other) {
        return new CompareToBuilder()
            .append(getEvaluationOrder(), other.getEvaluationOrder())
            .append(StringUtils.defaultIfBlank(getName(), StringUtils.EMPTY).toLowerCase(),
                StringUtils.defaultIfBlank(other.getName(), StringUtils.EMPTY).toLowerCase())
            .append(getServiceId(), other.getServiceId()).append(getId(), other.getId())
            .toComparison();
    }

    /**
     * Create a new service instance.
     *
     * @return the registered service
     */
    protected abstract AbstractRegisteredService newInstance();


    @Override
    @Deprecated(since = "6.2.0")
    public Set<String> getRequiredHandlers() {
        LOGGER.warn("Assigning a collection of required authentication handlers to a registered service is deprecated. "
            + "This field is scheduled to be removed in the future. If you need to, consider defining an authentication policy "
            + "for the registered service instead to specify required authentication handlers");
        return getAuthenticationPolicy().getRequiredAuthenticationHandlers();
    }

    @Deprecated(since = "6.2.0")
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
    public Map<String, RegisteredServiceProperty> getProperties() {
        return (Map) this.properties;
    }

    public void setProperties(final Map<String, RegisteredServiceProperty> properties) {
        this.properties = (Map) properties;
    }

    @Override
    public List<RegisteredServiceContact> getContacts() {
        return (List) this.contacts;
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
    }

    public void setContacts(final List<RegisteredServiceContact> contacts) {
        this.contacts = (List) contacts;
    }
}
