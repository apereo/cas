package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.crypto.Cipher;
import java.io.Serializable;
import java.security.PublicKey;

/**
 * Represents a public key for a CAS registered service.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServicePublicKey extends Serializable {

    /**
     * Gets location to the public key file.
     *
     * @return the location
     */
    String getLocation();

    /**
     * Gets algorithm for the public key.
     *
     * @return the algorithm
     */
    String getAlgorithm();

    /**
     * Create instance.
     *
     * @return the public key
     */
    @JsonIgnore
    PublicKey createInstance();

    /**
     * Convert the key into a cipher instance.
     *
     * @return the cipher
     */
    @JsonIgnore
    Cipher toCipher();
}
