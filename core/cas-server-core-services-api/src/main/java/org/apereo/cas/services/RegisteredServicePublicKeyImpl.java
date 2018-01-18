package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.annotation.Transient;
import org.springframework.util.ResourceUtils;
import java.security.PublicKey;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a public key for a CAS registered service.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@ToString
@Getter
@Setter
public class RegisteredServicePublicKeyImpl implements RegisteredServicePublicKey {

    private static final long serialVersionUID = -8497658523695695863L;

    private String location;

    private String algorithm = "RSA";

    @JsonIgnore
    @Transient
    private Class<PublicKeyFactoryBean> publicKeyFactoryBeanClass = PublicKeyFactoryBean.class;

    /**
     * Instantiates a new Registered service public key impl.
     * Required for proper serialization.
     */
    public RegisteredServicePublicKeyImpl() {
    }

    /**
     * Instantiates a new Registered service public key impl.
     *
     * @param location the location
     * @param algorithm the algorithm
     */
    public RegisteredServicePublicKeyImpl(final String location, final String algorithm) {
        this.location = location;
        this.algorithm = algorithm;
    }

    @Override
    public PublicKey createInstance() {
        try {
            final PublicKeyFactoryBean factory = this.publicKeyFactoryBeanClass.getDeclaredConstructor().newInstance();
            if (this.location.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
                factory.setResource(new ClassPathResource(StringUtils.removeStart(this.location, ResourceUtils.CLASSPATH_URL_PREFIX)));
            } else {
                factory.setResource(new FileSystemResource(this.location));
            }
            factory.setAlgorithm(this.algorithm);
            factory.setSingleton(false);
            return factory.getObject();
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final RegisteredServicePublicKeyImpl rhs = (RegisteredServicePublicKeyImpl) obj;
        return new EqualsBuilder().append(this.location, rhs.location).append(this.algorithm, rhs.algorithm).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.location).append(this.algorithm).toHashCode();
    }
}
