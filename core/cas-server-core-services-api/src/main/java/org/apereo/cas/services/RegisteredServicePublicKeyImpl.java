package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;

import javax.crypto.Cipher;

import java.io.Serial;
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
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@AllArgsConstructor
@Accessors(chain = true)
public class RegisteredServicePublicKeyImpl implements RegisteredServicePublicKey {

    @Serial
    private static final long serialVersionUID = -8497658523695695863L;

    @ExpressionLanguageCapable
    private String location;

    private String algorithm;

    @Override
    public PublicKey createInstance() {
        return Unchecked.supplier(() -> {
            if (StringUtils.isNotBlank(this.location)) {
                LOGGER.trace("Attempting to read public key from [{}]", this.location);
                val factory = initializePublicKeyFactoryBean();
                return factory.getObject();
            }
            LOGGER.warn("No public key location is defined");
            return null;
        }).get();
    }


    @Override
    public Cipher toCipher() {
        return Unchecked.supplier(() -> {
            if (StringUtils.isNotBlank(location)) {
                LOGGER.trace("Attempting to initialize the cipher for public key [{}]", location);
                return initializePublicKeyFactoryBean().toCipher();
            }
            LOGGER.warn("NO public key location is defined");
            return null;
        }).get();
    }

    @JsonIgnore
    private PublicKeyFactoryBean initializePublicKeyFactoryBean() throws Exception {
        val resolved = SpringExpressionLanguageValueResolver.getInstance().resolve(this.location);
        val resource = ResourceUtils.getResourceFrom(resolved);
        val factory = new PublicKeyFactoryBean(resource, StringUtils.defaultIfBlank(this.algorithm, "RSA"));
        factory.setSingleton(false);
        return factory;
    }
}
