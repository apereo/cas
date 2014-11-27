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
package org.jasig.cas.util.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.util.AbstractJacksonBackedJsonSerializer;

import java.net.URL;

/**
 * Serializes registered services to JSON based on the Jackson JSON library.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class RegisteredServiceJsonSerializer extends AbstractJacksonBackedJsonSerializer<RegisteredService> {
    private static final long serialVersionUID = 7645698151115635245L;

    @Override
    protected ObjectMapper initializeObjectMapper() {
        final ObjectMapper mapper = super.initializeObjectMapper();
        mapper.addMixInAnnotations(RegisteredServiceProxyPolicy.class, RegisteredServiceProxyPolicyMixin.class);
        return mapper;
    }

    private interface RegisteredServiceProxyPolicyMixin {
        /**
         * Ignore method call.
         * @return allowed or not
         **/
        @JsonIgnore
        boolean isAllowedToProxy();

        /**
         * Ignore method call.
         * @param pgtUrl proxying url
         * @return allowed or not
         **/
        @JsonIgnore
        boolean isAllowedProxyCallbackUrl(URL pgtUrl);
    }

    @Override
    protected Class<RegisteredService> getTypeToSerialize() {
        return RegisteredService.class;
    }
}
