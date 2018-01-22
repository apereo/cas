package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import java.net.URL;
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
@DiscriminatorColumn(name = "expression_type", length = 50, discriminatorType = DiscriminatorType.STRING,
        columnDefinition = "VARCHAR(50) DEFAULT 'regex'")
@Table(name = "RegexRegisteredService")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public abstract class AbstractRegisteredService implements RegisteredService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegisteredService.class);

    private static final long serialVersionUID = 7645279151115635245L;

    /**
     * The unique identifier for this service.
     */
    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    protected String serviceId;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String name;

    @Column(length = 255, updatable = true, insertable = true, nullable = true)
    private String theme;

    @Column(length = 255, updatable = true, insertable = true, nullable = true)
    private String informationUrl;

    @Column(length = 255, updatable = true, insertable = true, nullable = true)
    private String privacyUrl;

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = RegisteredService.INITIAL_IDENTIFIER_VALUE;

    @Column(length = 255, updatable = true, insertable = true, nullable = true)
    private String description;

    @Lob
    @Column(name = "expiration_policy", nullable = true, length = Integer.MAX_VALUE)
    private RegisteredServiceExpirationPolicy expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();

    @Lob
    @Column(name = "proxy_policy", nullable = true, length = Integer.MAX_VALUE)
    private RegisteredServiceProxyPolicy proxyPolicy = new RefuseRegisteredServiceProxyPolicy();

    @Column(name = "evaluation_order", nullable = false)
    private int evaluationOrder;

    @Lob
    @Column(name = "username_attr", nullable = true, length = Integer.MAX_VALUE)
    private RegisteredServiceUsernameAttributeProvider usernameAttributeProvider = new DefaultRegisteredServiceUsernameProvider();

    @Column(name = "logout_type", nullable = true)
    private LogoutType logoutType = LogoutType.BACK_CHANNEL;

    @Lob
    @Column(name = "required_handlers", length = Integer.MAX_VALUE)
    private HashSet<String> requiredHandlers = new HashSet<>();

    @Lob
    @Column(name = "attribute_release", nullable = true, length = Integer.MAX_VALUE)
    private RegisteredServiceAttributeReleasePolicy attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();

    @Lob
    @Column(name = "mfa_policy", nullable = true, length = Integer.MAX_VALUE)
    private RegisteredServiceMultifactorPolicy multifactorPolicy = new DefaultRegisteredServiceMultifactorPolicy();

    @Column(length = 255, updatable = true, insertable = true, nullable = true)
    private String logo;

    @Column(name = "logout_url")
    private URL logoutUrl;

    @Lob
    @Column(name = "access_strategy", nullable = true, length = Integer.MAX_VALUE)
    private RegisteredServiceAccessStrategy accessStrategy = new DefaultRegisteredServiceAccessStrategy();

    @Lob
    @Column(name = "public_key", nullable = true, length = Integer.MAX_VALUE)
    private RegisteredServicePublicKey publicKey;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "RegisteredServiceImpl_Props")
    private Map<String, DefaultRegisteredServiceProperty> properties = new HashMap<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "RegisteredService_Contacts")
    @OrderColumn
    private List<DefaultRegisteredServiceContact> contacts = new ArrayList<>();


    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getInformationUrl() {
        return this.informationUrl;
    }

    @Override
    public String getPrivacyUrl() {
        return this.privacyUrl;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getServiceId() {
        return this.serviceId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getTheme() {
        return this.theme;
    }

    @Override
    public RegisteredServiceProxyPolicy getProxyPolicy() {
        return this.proxyPolicy;
    }

    @Override
    public RegisteredServiceAccessStrategy getAccessStrategy() {
        return this.accessStrategy;
    }

    @Override
    public URL getLogoutUrl() {
        return this.logoutUrl;
    }

    /**
     * Initializes the registered service with default values
     * for fields that are unspecified. Only triggered by JPA.
     *
     * @since 4.1
     */
    @PostLoad
    public void postLoad() {
        if (this.proxyPolicy == null) {
            this.proxyPolicy = new RefuseRegisteredServiceProxyPolicy();
        }
        if (this.usernameAttributeProvider == null) {
            this.usernameAttributeProvider = new DefaultRegisteredServiceUsernameProvider();
        }
        if (this.logoutType == null) {
            this.logoutType = LogoutType.BACK_CHANNEL;
        }
        if (this.requiredHandlers == null) {
            this.requiredHandlers = new HashSet<>();
        }
        if (this.accessStrategy == null) {
            this.accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        }
        if (this.multifactorPolicy == null) {
            this.multifactorPolicy = new DefaultRegisteredServiceMultifactorPolicy();
        }
        if (this.properties == null) {
            this.properties = new LinkedHashMap<>();
        }
        if (this.attributeReleasePolicy == null) {
            this.attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();
        }
        if (this.contacts == null) {
            this.contacts = new ArrayList<>();
        }
        if (this.expirationPolicy == null) {
            this.expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof AbstractRegisteredService)) {
            return false;
        }

        final AbstractRegisteredService that = (AbstractRegisteredService) o;

        final EqualsBuilder builder = new EqualsBuilder();
        return builder
                .append(this.proxyPolicy, that.proxyPolicy)
                .append(this.evaluationOrder, that.evaluationOrder)
                .append(this.description, that.description)
                .append(this.name, that.name)
                .append(this.serviceId, that.serviceId)
                .append(this.theme, that.theme)
                .append(this.usernameAttributeProvider, that.usernameAttributeProvider)
                .append(this.logoutType, that.logoutType)
                .append(this.attributeReleasePolicy, that.attributeReleasePolicy)
                .append(this.accessStrategy, that.accessStrategy)
                .append(this.logo, that.logo)
                .append(this.publicKey, that.publicKey)
                .append(this.logoutUrl, that.logoutUrl)
                .append(this.requiredHandlers, that.requiredHandlers)
                .append(this.proxyPolicy, that.proxyPolicy)
                .append(this.properties, that.properties)
                .append(this.multifactorPolicy, that.multifactorPolicy)
                .append(this.informationUrl, that.informationUrl)
                .append(this.privacyUrl, that.privacyUrl)
                .append(this.contacts, that.contacts)
                .append(this.expirationPolicy, that.expirationPolicy)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 31)
                .append(this.description)
                .append(this.serviceId)
                .append(this.name)
                .append(this.theme)
                .append(this.evaluationOrder)
                .append(this.usernameAttributeProvider)
                .append(this.accessStrategy)
                .append(this.logoutType)
                .append(this.attributeReleasePolicy)
                .append(this.accessStrategy)
                .append(this.logo)
                .append(this.publicKey)
                .append(this.logoutUrl)
                .append(this.requiredHandlers)
                .append(this.proxyPolicy)
                .append(this.properties)
                .append(this.multifactorPolicy)
                .append(this.informationUrl)
                .append(this.privacyUrl)
                .append(this.contacts)
                .append(this.expirationPolicy)
                .toHashCode();
    }

    public void setProxyPolicy(final RegisteredServiceProxyPolicy policy) {
        this.proxyPolicy = policy;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Sets the service identifier. Extensions are to define the format.
     *
     * @param id the new service id
     */
    public abstract void setServiceId(String id);

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setTheme(final String theme) {
        this.theme = theme;
    }

    @Override
    public void setEvaluationOrder(final int evaluationOrder) {
        this.evaluationOrder = evaluationOrder;
    }

    @Override
    public int getEvaluationOrder() {
        return this.evaluationOrder;
    }

    @Override
    public RegisteredServiceUsernameAttributeProvider getUsernameAttributeProvider() {
        return this.usernameAttributeProvider;
    }

    public void setAccessStrategy(final RegisteredServiceAccessStrategy accessStrategy) {
        this.accessStrategy = accessStrategy;
    }

    public void setLogoutUrl(final URL logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public void setInformationUrl(final String informationUrl) {
        this.informationUrl = informationUrl;
    }

    public void setPrivacyUrl(final String privacyUrl) {
        this.privacyUrl = privacyUrl;
    }

    /**
     * Sets the user attribute provider instance
     * when providing usernames to this registered service.
     *
     * @param usernameProvider the new username attribute
     */
    public void setUsernameAttributeProvider(final RegisteredServiceUsernameAttributeProvider usernameProvider) {
        this.usernameAttributeProvider = usernameProvider;
    }

    @Override
    public LogoutType getLogoutType() {
        return this.logoutType;
    }

    /**
     * Set the logout type of the service.
     *
     * @param logoutType the logout type of the service.
     */
    public void setLogoutType(final LogoutType logoutType) {
        this.logoutType = logoutType;
    }

    @Override
    public AbstractRegisteredService clone() {
        final AbstractRegisteredService clone = newInstance();
        clone.copyFrom(this);
        return clone;
    }

    /**
     * Copies the properties of the source service into this instance.
     *
     * @param source Source service from which to copy properties.
     */
    public void copyFrom(final RegisteredService source) {
        setId(source.getId());
        setProxyPolicy(source.getProxyPolicy());
        setDescription(source.getDescription());
        setName(source.getName());
        setServiceId(source.getServiceId());
        setTheme(source.getTheme());
        setEvaluationOrder(source.getEvaluationOrder());
        setUsernameAttributeProvider(source.getUsernameAttributeProvider());
        setLogoutType(source.getLogoutType());
        setAttributeReleasePolicy(source.getAttributeReleasePolicy());
        setAccessStrategy(source.getAccessStrategy());
        setLogo(source.getLogo());
        setLogoutUrl(source.getLogoutUrl());
        setPublicKey(source.getPublicKey());
        setRequiredHandlers(source.getRequiredHandlers());
        setProperties(source.getProperties());
        setMultifactorPolicy(source.getMultifactorPolicy());
        setInformationUrl(source.getInformationUrl());
        setPrivacyUrl(source.getPrivacyUrl());
        setContacts(source.getContacts());
        setExpirationPolicy(source.getExpirationPolicy());
    }

    /**
     * {@inheritDoc}
     * Compares this instance with the {@code other} registered service based on
     * evaluation order, name. The name comparison is case insensitive.
     *
     * @see #getEvaluationOrder()
     */
    @Override
    public int compareTo(final RegisteredService other) {
        return new CompareToBuilder()
                .append(getEvaluationOrder(), other.getEvaluationOrder())
                .append(StringUtils.defaultIfBlank(getName(), StringUtils.EMPTY).toLowerCase(),
                        StringUtils.defaultIfBlank(other.getName(), StringUtils.EMPTY).toLowerCase())
                .append(getServiceId(), other.getServiceId())
                .append(getId(), other.getId())
                .toComparison();
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(null, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("id", this.id);
        builder.append("name", this.name);
        builder.append("description", this.description);
        builder.append("serviceId", this.serviceId);
        builder.append("usernameAttributeProvider", this.usernameAttributeProvider);
        builder.append("theme", this.theme);
        builder.append("evaluationOrder", this.evaluationOrder);
        builder.append("logoutType", this.logoutType);
        builder.append("attributeReleasePolicy", this.attributeReleasePolicy);
        builder.append("accessStrategy", this.accessStrategy);
        builder.append("publicKey", this.publicKey);
        builder.append("proxyPolicy", this.proxyPolicy);
        builder.append("logo", this.logo);
        builder.append("logoutUrl", this.logoutUrl);
        builder.append("requiredHandlers", this.requiredHandlers);
        builder.append("properties", this.properties);
        builder.append("multifactorPolicy", this.multifactorPolicy);
        builder.append("informationUrl", this.informationUrl);
        builder.append("privacyUrl", this.privacyUrl);
        builder.append("contacts", this.contacts);
        builder.append("expirationPolicy", this.expirationPolicy);
        return builder.toString();
    }

    /**
     * Create a new service instance.
     *
     * @return the registered service
     */
    protected abstract AbstractRegisteredService newInstance();

    @Override
    public Set<String> getRequiredHandlers() {
        if (this.requiredHandlers == null) {
            this.requiredHandlers = new HashSet<>();
        }
        return this.requiredHandlers;
    }

    /**
     * Sets the required handlers for this service.
     *
     * @param handlers the new required handlers
     */
    public void setRequiredHandlers(final Set<String> handlers) {
        getRequiredHandlers().clear();
        if (handlers == null) {
            return;
        }
        getRequiredHandlers().addAll(handlers);
    }

    /**
     * Sets the attribute filtering policy.
     *
     * @param policy the new attribute filtering policy
     */
    public void setAttributeReleasePolicy(final RegisteredServiceAttributeReleasePolicy policy) {
        this.attributeReleasePolicy = policy;
    }

    @Override
    public RegisteredServiceAttributeReleasePolicy getAttributeReleasePolicy() {
        return this.attributeReleasePolicy;
    }

    @Override
    public String getLogo() {
        return this.logo;
    }

    public void setLogo(final String logo) {
        this.logo = logo;
    }

    @Override
    public RegisteredServicePublicKey getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(final RegisteredServicePublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public Map<String, RegisteredServiceProperty> getProperties() {
        return (Map) this.properties;
    }

    public void setProperties(final Map<String, RegisteredServiceProperty> properties) {
        this.properties = (Map) properties;
    }

    public RegisteredServiceMultifactorPolicy getMultifactorPolicy() {
        return this.multifactorPolicy;
    }

    public void setMultifactorPolicy(final RegisteredServiceMultifactorPolicy multifactorPolicy) {
        this.multifactorPolicy = multifactorPolicy;
    }

    @Override
    public List<RegisteredServiceContact> getContacts() {
        return (List) this.contacts;
    }

    public void setContacts(final List<RegisteredServiceContact> contacts) {
        this.contacts = (List) contacts;
    }

    @Override
    public RegisteredServiceExpirationPolicy getExpirationPolicy() {
        return expirationPolicy;
    }

    public void setExpirationPolicy(final RegisteredServiceExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
    }
}
