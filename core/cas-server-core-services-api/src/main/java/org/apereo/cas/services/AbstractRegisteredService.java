package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
import javax.persistence.PostLoad;
import javax.persistence.Table;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
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
@DiscriminatorColumn(name = "expression_type", length = 15, discriminatorType = DiscriminatorType.STRING,
        columnDefinition = "VARCHAR(15) DEFAULT 'ant'")
@Table(name = "RegexRegisteredService")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class AbstractRegisteredService implements RegisteredService {

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

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = RegisteredService.INITIAL_IDENTIFIER_VALUE;

    @Column(length = 255, updatable = true, insertable = true, nullable = true)
    private String description;
    
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

    @Column(name = "logo")
    private URL logo;

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
            this.properties = new HashMap<>();
        }
        if (this.attributeReleasePolicy == null) {
            this.attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();
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

    @JsonIgnore
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
                .append(getName().toLowerCase(), other.getName().toLowerCase())
                .append(getServiceId(), other.getServiceId())
                .append(getId(), other.getId())
                .toComparison();
    }

    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(null, ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.append("id", this.id);
        toStringBuilder.append("name", this.name);
        toStringBuilder.append("description", this.description);
        toStringBuilder.append("serviceId", this.serviceId);
        toStringBuilder.append("usernameAttributeProvider", this.usernameAttributeProvider);
        toStringBuilder.append("theme", this.theme);
        toStringBuilder.append("evaluationOrder", this.evaluationOrder);
        toStringBuilder.append("logoutType", this.logoutType);
        toStringBuilder.append("attributeReleasePolicy", this.attributeReleasePolicy);
        toStringBuilder.append("accessStrategy", this.accessStrategy);
        toStringBuilder.append("publicKey", this.publicKey);
        toStringBuilder.append("proxyPolicy", this.proxyPolicy);
        toStringBuilder.append("logo", this.logo);
        toStringBuilder.append("logoutUrl", this.logoutUrl);
        toStringBuilder.append("requiredHandlers", this.requiredHandlers);
        toStringBuilder.append("properties", this.properties);
        toStringBuilder.append("multifactorPolicy", this.multifactorPolicy);
        toStringBuilder.append("informationUrl", this.informationUrl);
        toStringBuilder.append("privacyUrl", this.privacyUrl);
        return toStringBuilder.toString();
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
    public URL getLogo() {
        return this.logo;
    }

    public void setLogo(final URL logo) {
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
}
