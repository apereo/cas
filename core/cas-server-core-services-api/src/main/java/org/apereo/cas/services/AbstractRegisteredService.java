package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.annotations.GenericGenerator;

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
@DiscriminatorColumn(name = "expression_type", length = 50, discriminatorType = DiscriminatorType.STRING, columnDefinition = "VARCHAR(50) DEFAULT 'regex'")
@Table(name = "RegexRegisteredService")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@ToString
@Getter
@Setter
@EqualsAndHashCode(exclude = {"id"})
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
    @GenericGenerator(name = "native", strategy = "native")
    private long id = RegisteredService.INITIAL_IDENTIFIER_VALUE;

    @Column
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

    @Column
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
    
    /**
     * Initializes the registered service with default values
     * for fields that are unspecified. Only triggered by JPA.
     *
     * @since 4.1
     */
    @PostLoad
    public void postLoad() {
        this.proxyPolicy = ObjectUtils.defaultIfNull(this.proxyPolicy, new RefuseRegisteredServiceProxyPolicy());
        this.usernameAttributeProvider = ObjectUtils.defaultIfNull(this.usernameAttributeProvider, new DefaultRegisteredServiceUsernameProvider());
        this.logoutType = ObjectUtils.defaultIfNull(this.logoutType, LogoutType.BACK_CHANNEL);
        this.requiredHandlers = ObjectUtils.defaultIfNull(this.requiredHandlers, new HashSet<>());
        this.accessStrategy = ObjectUtils.defaultIfNull(this.accessStrategy, new DefaultRegisteredServiceAccessStrategy());
        this.multifactorPolicy = ObjectUtils.defaultIfNull(this.multifactorPolicy, new DefaultRegisteredServiceMultifactorPolicy());
        this.properties = ObjectUtils.defaultIfNull(this.properties, new LinkedHashMap());
        this.attributeReleasePolicy = ObjectUtils.defaultIfNull(this.attributeReleasePolicy, new ReturnAllowedAttributeReleasePolicy());
        this.contacts = ObjectUtils.defaultIfNull(this.contacts, new ArrayList());
        this.expirationPolicy = ObjectUtils.defaultIfNull(this.expirationPolicy, new DefaultRegisteredServiceExpirationPolicy());
    }

    /**
     * Sets the service identifier. Extensions are to define the format.
     *
     * @param id the new service id
     */
    public abstract void setServiceId(String id);

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
        setResponseType(source.getResponseType());
        setEvaluationOrder(source.getEvaluationOrder());
        setUsernameAttributeProvider(source.getUsernameAttributeProvider());
        setLogoutType(source.getLogoutType());
        setAttributeReleasePolicy(source.getAttributeReleasePolicy());
        setAccessStrategy(source.getAccessStrategy());
        setLogo(source.getLogo());
        setLogoutUrl(source.getLogoutUrl());
        setPublicKey(source.getPublicKey());
        setRequiredHandlers(new HashSet<>(source.getRequiredHandlers()));
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
        return new CompareToBuilder().append(getEvaluationOrder(),
            other.getEvaluationOrder()).append(StringUtils.defaultIfBlank(getName(), StringUtils.EMPTY).toLowerCase(),
            StringUtils.defaultIfBlank(other.getName(), StringUtils.EMPTY).toLowerCase())
            .append(getServiceId(), other.getServiceId()).append(getId(), other.getId()).toComparison();
    }

    /**
     * Create a new service instance.
     *
     * @return the registered service
     */
    protected abstract AbstractRegisteredService newInstance();

    @Override
    public Map<String, RegisteredServiceProperty> getProperties() {
        return (Map) this.properties;
    }

    @Override
    public List<RegisteredServiceContact> getContacts() {
        return (List) this.contacts;
    }

    public void setProperties(final Map<String, RegisteredServiceProperty> properties) {
        this.properties = (Map) properties;
    }
    
    public void setContacts(final List<RegisteredServiceContact> contacts) {
        this.contacts = (List) contacts;
    }
}
