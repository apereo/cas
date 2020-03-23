package org.apereo.cas.memcached.kryo.serial;

import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServicePublicKey;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Serializer for {@link RegisteredService} instances.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RegisteredServiceSerializer extends Serializer<RegisteredService> {

    /**
     * In case the url object is null in the service,
     * we need to be able to return a default/mock url.
     *
     * @return mock url
     */
    @SneakyThrows
    private static URL getEmptyUrl() {
        return new URL("https://");
    }

    /**
     * Write object by reflection.
     *
     * @param kryo   the kryo
     * @param output the output
     * @param obj    the obj
     */
    private static void writeObjectByReflection(final Kryo kryo, final Output output, final Object obj) {
        val className = obj.getClass().getCanonicalName();
        kryo.writeObject(output, className);
        kryo.writeObject(output, obj);
    }

    /**
     * Read object by reflection.
     *
     * @param <T>   the type parameter
     * @param kryo  the kryo
     * @param input the input
     * @param clazz the clazz
     * @return the t
     */
    @SneakyThrows
    private static <T> T readObjectByReflection(final Kryo kryo, final Input input, final Class<T> clazz) {
        val className = kryo.readObject(input, String.class);
        val foundClass = (Class<T>) Class.forName(className);
        val result = kryo.readObject(input, foundClass);

        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [" + result
                + " is of type " + result.getClass()
                + " when we were expecting " + clazz);
        }
        return result;
    }

    @Override
    public void write(final Kryo kryo, final Output output, final RegisteredService service) {
        kryo.writeObject(output, service.getServiceId());
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getName(), StringUtils.EMPTY));
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getDescription(), StringUtils.EMPTY));
        kryo.writeObject(output, service.getId());
        kryo.writeObject(output, service.getEvaluationOrder());
        kryo.writeObject(output, ObjectUtils.defaultIfNull(service.getLogo(), getEmptyUrl()));
        kryo.writeObject(output, service.getLogoutType());
        kryo.writeObject(output, ObjectUtils.defaultIfNull(service.getLogoutUrl(), StringUtils.EMPTY));
        
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getTheme(), StringUtils.EMPTY));
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getResponseType(), StringUtils.EMPTY));

        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getPublicKey(), new RegisteredServicePublicKeyImpl()));
        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getProxyPolicy(), new RefuseRegisteredServiceProxyPolicy()));
        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getAttributeReleasePolicy(), new ReturnAllowedAttributeReleasePolicy()));
        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getUsernameAttributeProvider(), new DefaultRegisteredServiceUsernameProvider()));
        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getAccessStrategy(), new DefaultRegisteredServiceAccessStrategy()));
        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getMultifactorPolicy(), new DefaultRegisteredServiceMultifactorPolicy()));
        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getAuthenticationPolicy(), new DefaultRegisteredServiceAuthenticationPolicy()));
        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getContacts(), new ArrayList<>(0)));

        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getInformationUrl(), StringUtils.EMPTY));
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getPrivacyUrl(), StringUtils.EMPTY));
        kryo.writeObject(output, new HashMap<>(service.getProperties()));
    }

    @Override
    public RegisteredService read(final Kryo kryo, final Input input, final Class<RegisteredService> type) {
        val svc = new RegexRegisteredService();
        svc.setServiceId(kryo.readObject(input, String.class));
        svc.setName(kryo.readObject(input, String.class));
        svc.setDescription(kryo.readObject(input, String.class));
        svc.setId(kryo.readObject(input, Long.class));
        svc.setEvaluationOrder(kryo.readObject(input, Integer.class));
        svc.setLogo(kryo.readObject(input, String.class));
        svc.setLogoutType(kryo.readObject(input, RegisteredServiceLogoutType.class));
        svc.setLogoutUrl(kryo.readObject(input, String.class));
       
        svc.setTheme(kryo.readObject(input, String.class));
        svc.setResponseType(StringUtils.defaultIfBlank(kryo.readObject(input, String.class), null));

        svc.setPublicKey(readObjectByReflection(kryo, input, RegisteredServicePublicKey.class));
        svc.setProxyPolicy(readObjectByReflection(kryo, input, RegisteredServiceProxyPolicy.class));
        svc.setAttributeReleasePolicy(readObjectByReflection(kryo, input, RegisteredServiceAttributeReleasePolicy.class));
        svc.setUsernameAttributeProvider(readObjectByReflection(kryo, input, RegisteredServiceUsernameAttributeProvider.class));
        svc.setAccessStrategy(readObjectByReflection(kryo, input, RegisteredServiceAccessStrategy.class));
        svc.setMultifactorPolicy(readObjectByReflection(kryo, input, RegisteredServiceMultifactorPolicy.class));
        svc.setAuthenticationPolicy(readObjectByReflection(kryo, input, RegisteredServiceAuthenticationPolicy.class));
        svc.setContacts(readObjectByReflection(kryo, input, List.class));

        svc.setInformationUrl(StringUtils.defaultIfBlank(kryo.readObject(input, String.class), null));
        svc.setPrivacyUrl(StringUtils.defaultIfBlank(kryo.readObject(input, String.class), null));
        svc.setProperties(kryo.readObject(input, HashMap.class));
        return svc;
    }
}
