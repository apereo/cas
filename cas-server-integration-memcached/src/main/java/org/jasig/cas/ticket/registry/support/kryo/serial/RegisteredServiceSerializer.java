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
package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.AttributeReleasePolicy;
import org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.jasig.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredServicePublicKey;
import org.jasig.cas.services.RegisteredServicePublicKeyImpl;
import org.jasig.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;

import java.net.URL;

/**
 * Serializer for {@link org.jasig.cas.services.RegisteredService} instances.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RegisteredServiceSerializer  extends Serializer<RegisteredService> {

    /**
     * In case the url object is null in the service,
     * we need to be able to return a default/mock url.
     * @return mock url
     */
    private URL getEmptyUrl() {
        try {
            return new URL("https://");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(final Kryo kryo, final Output output, final RegisteredService service) {
        kryo.writeObject(output, service.getServiceId());
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getName(), ""));
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getDescription(), ""));
        kryo.writeObject(output, service.getId());
        kryo.writeObject(output, service.getEvaluationOrder());
        kryo.writeObject(output, ObjectUtils.defaultIfNull(service.getLogo(), getEmptyUrl()));
        kryo.writeObject(output, service.getLogoutType());
        kryo.writeObject(output, ObjectUtils.defaultIfNull(service.getLogoutUrl(), getEmptyUrl()));
        kryo.writeObject(output, ImmutableSet.copyOf(service.getRequiredHandlers()));
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getTheme(), ""));

        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getPublicKey(),
                new RegisteredServicePublicKeyImpl()));
        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getProxyPolicy(),
                new RefuseRegisteredServiceProxyPolicy()));
        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getAttributeReleasePolicy(),
                new ReturnAllowedAttributeReleasePolicy()));
        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getUsernameAttributeProvider(),
                new DefaultRegisteredServiceUsernameProvider()));
        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getAccessStrategy(),
                new DefaultRegisteredServiceAccessStrategy()));
    }

    @Override
    public RegisteredService read(final Kryo kryo, final Input input, final Class<RegisteredService> type) {
        final AbstractRegisteredService svc = new RegexRegisteredService();
        svc.setServiceId(kryo.readObject(input, String.class));
        svc.setName(kryo.readObject(input, String.class));
        svc.setDescription(kryo.readObject(input, String.class));
        svc.setId(kryo.readObject(input, Long.class));
        svc.setEvaluationOrder(kryo.readObject(input, Integer.class));
        svc.setLogo(kryo.readObject(input, URL.class));
        svc.setLogoutType(kryo.readObject(input, LogoutType.class));
        svc.setLogoutUrl(kryo.readObject(input, URL.class));
        svc.setRequiredHandlers(kryo.readObject(input, ImmutableSet.class));
        svc.setTheme(kryo.readObject(input, String.class));

        svc.setPublicKey(readObjectByReflection(kryo, input, RegisteredServicePublicKey.class));
        svc.setProxyPolicy(readObjectByReflection(kryo, input, RegisteredServiceProxyPolicy.class));
        svc.setAttributeReleasePolicy(readObjectByReflection(kryo, input, AttributeReleasePolicy.class));
        svc.setUsernameAttributeProvider(readObjectByReflection(kryo, input, RegisteredServiceUsernameAttributeProvider.class));
        svc.setAccessStrategy(readObjectByReflection(kryo, input, RegisteredServiceAccessStrategy.class));

        return svc;
    }


    /**
     * Write object by reflection.
     *
     * @param kryo   the kryo
     * @param output the output
     * @param obj    the obj
     */
    private static void writeObjectByReflection(final Kryo kryo, final Output output, final Object obj) {
        final String className = obj.getClass().getCanonicalName();
        kryo.writeObject(output, className);
        kryo.writeObject(output, obj);
    }

    /**
     * Read object by reflection.
     *
     * @param <T>   the type parameter
     * @param kryo  the kryo
     * @param input the input
     * @param type  the type
     * @return the t
     */
    private static <T> T readObjectByReflection(final Kryo kryo, final Input input, final Class<? extends T> type) {
        try {
            final String className = kryo.readObject(input, String.class);
            final Class<T> clazz = (Class<T>) Class.forName(className);
            return kryo.readObject(input, clazz);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
