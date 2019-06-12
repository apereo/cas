package org.apereo.cas.services;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.annotation.Transient;

import java.security.PublicKey;

/**
 * Represents a public key for a CAS registered service.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"location", "algorithm"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisteredServicePublicKeyImpl implements RegisteredServicePublicKey {

    private static final long serialVersionUID = -8497658523695695863L;

    private String location;

    private String algorithm = "RSA";

    @JsonIgnore
    @Transient
    @javax.persistence.Transient
    private transient Class<PublicKeyFactoryBean> publicKeyFactoryBeanClass = PublicKeyFactoryBean.class;

    public RegisteredServicePublicKeyImpl(final String location, final String algorithm) {
        this.location = location;
        this.algorithm = algorithm;
    }

    @SneakyThrows
    @Override
    public PublicKey createInstance() {
        val factory = this.publicKeyFactoryBeanClass.getDeclaredConstructor().newInstance();
        LOGGER.trace("Attempting to read public key from [{}]", this.location);
        val resource = ResourceUtils.getResourceFrom(this.location);
        factory.setResource(resource);
        factory.setAlgorithm(this.algorithm);
        factory.setSingleton(false);
        return factory.getObject();
    }

}
