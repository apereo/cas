package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.annotation.Transient;
import org.springframework.util.ResourceUtils;

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
public class RegisteredServicePublicKeyImpl implements RegisteredServicePublicKey {

    private static final long serialVersionUID = -8497658523695695863L;

    private String location;

    private String algorithm = "RSA";

    @JsonIgnore
    @Transient
    private Class<PublicKeyFactoryBean> publicKeyFactoryBeanClass = PublicKeyFactoryBean.class;

    /**
     * Instantiates a new Registered service public key impl.
     *
     * @param location  the location
     * @param algorithm the algorithm
     */
    public RegisteredServicePublicKeyImpl(final String location, final String algorithm) {
        this.location = location;
        this.algorithm = algorithm;
    }

    @SneakyThrows
    @Override
    public PublicKey createInstance() {
        final PublicKeyFactoryBean factory = this.publicKeyFactoryBeanClass.getDeclaredConstructor().newInstance();
        if (this.location.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            factory.setResource(new ClassPathResource(StringUtils.removeStart(this.location, ResourceUtils.CLASSPATH_URL_PREFIX)));
        } else {
            factory.setResource(new FileSystemResource(this.location));
        }
        factory.setAlgorithm(this.algorithm);
        factory.setSingleton(false);
        return factory.getObject();
    }

}
