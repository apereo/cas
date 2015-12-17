package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.ImmutableSet;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredServicePublicKey;
import org.jasig.cas.services.RegisteredServiceUsernameAttributeProvider;

import java.net.URL;

/**
 * Serializier for {@link org.jasig.cas.services.RegisteredService} instances.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RegisteredServiceSerializer  extends Serializer<RegisteredService> {

    @Override
    public void write(final Kryo kryo, final Output output, final RegisteredService service) {
        kryo.writeObject(output, service.getServiceId());
        kryo.writeObject(output, service.getName());
        kryo.writeObject(output, service.getDescription());
        kryo.writeObject(output, service.getId());
        kryo.writeObject(output, service.getEvaluationOrder());
        kryo.writeObject(output, service.getLogo());
        kryo.writeObject(output, service.getLogoutType());
        kryo.writeObject(output, service.getLogoutUrl());
        kryo.writeObject(output, ImmutableSet.copyOf(service.getRequiredHandlers()));
        kryo.writeObject(output, service.getTheme());

        writeObjectByReflection(kryo, output, service.getPublicKey());
        writeObjectByReflection(kryo, output, service.getProxyPolicy());
        writeObjectByReflection(kryo, output, service.getAttributeReleasePolicy());
        writeObjectByReflection(kryo, output, service.getUsernameAttributeProvider());
        writeObjectByReflection(kryo, output, service.getAccessStrategy());
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
        svc.setAttributeReleasePolicy(readObjectByReflection(kryo, input, RegisteredServiceAttributeReleasePolicy.class));
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
