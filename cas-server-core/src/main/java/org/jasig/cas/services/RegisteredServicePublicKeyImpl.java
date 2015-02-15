/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.services;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.util.PublicKeyFactoryBean;
import org.reflections.util.ClasspathHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.security.PublicKey;

/**
 * Represents a public key for a CAS registered service.
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class RegisteredServicePublicKeyImpl implements Serializable, RegisteredServicePublicKey {
    private static final long serialVersionUID = -8497658523695695863L;

    private String location;

    private String algorithm;

    /**
     * Instantiates a new Registered service public key impl.
     * Required for proper serialization.
     */
    private RegisteredServicePublicKeyImpl() {}

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

    public void setLocation(final String location) {
        this.location = location;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String getLocation() {
        return this.location;
    }

    @Override
    public String getAlgorithm() {
        return this.algorithm;
    }


    @Override
    public PublicKey createInstance() throws Exception {
        final PublicKeyFactoryBean factory = new PublicKeyFactoryBean();
        if (this.location.startsWith("classpath:")) {
            factory.setLocation(new ClassPathResource(StringUtils.removeStart(this.location, "classpath:")));
        } else {
            factory.setLocation(new FileSystemResource(this.location));
        }
        factory.setAlgorithm(this.algorithm);
        factory.setSingleton(false);
        return (PublicKey) factory.getObject();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("location", this.location)
                .append("algorithm", this.algorithm)
                .toString();
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
        return new EqualsBuilder()
                .append(this.location, rhs.location)
                .append(this.algorithm, rhs.algorithm)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(location)
                .append(algorithm)
                .toHashCode();
    }
}
