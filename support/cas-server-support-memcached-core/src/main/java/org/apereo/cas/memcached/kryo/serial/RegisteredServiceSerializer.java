package org.apereo.cas.memcached.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredService.LogoutType;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Serializer for {@link RegisteredService} instances.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RegisteredServiceSerializer extends Serializer<RegisteredService> {

    /**
     * In case the url object is null in the service,
     * we need to be able to return a default/mock url.
     * @return mock url
     */
    private static URL getEmptyUrl() {
        try {
            return new URL("https://");
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void write(final Kryo kryo, final Output output, final RegisteredService service) {
        kryo.writeObject(output, service.getServiceId());
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getName(), StringUtils.EMPTY));
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getDescription(), StringUtils.EMPTY));
        kryo.writeObject(output, service.getId());
        kryo.writeObject(output, service.getEvaluationOrder());
        final URL emptyUrl = getEmptyUrl();
        kryo.writeObject(output, ObjectUtils.defaultIfNull(service.getLogo(), emptyUrl));
        kryo.writeObject(output, service.getLogoutType());
        kryo.writeObject(output, ObjectUtils.defaultIfNull(service.getLogoutUrl(), emptyUrl));
        kryo.writeObject(output, new HashSet<>(service.getRequiredHandlers()));
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getTheme(), StringUtils.EMPTY));

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

        writeObjectByReflection(kryo, output, ObjectUtils.defaultIfNull(service.getMultifactorPolicy(),
                new DefaultRegisteredServiceMultifactorPolicy()));

        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getInformationUrl(), StringUtils.EMPTY));
        kryo.writeObject(output, StringUtils.defaultIfEmpty(service.getPrivacyUrl(), StringUtils.EMPTY));
        kryo.writeObject(output, new HashMap<>(service.getProperties()));
    }

    @Override
    public RegisteredService read(final Kryo kryo, final Input input, final Class<RegisteredService> type) {
        final AbstractRegisteredService svc = new RegexRegisteredService();
        svc.setServiceId(kryo.readObject(input, String.class));
        svc.setName(kryo.readObject(input, String.class));
        svc.setDescription(kryo.readObject(input, String.class));
        svc.setId(kryo.readObject(input, Long.class));
        svc.setEvaluationOrder(kryo.readObject(input, Integer.class));
        svc.setLogo(kryo.readObject(input, String.class));
        svc.setLogoutType(kryo.readObject(input, LogoutType.class));
        svc.setLogoutUrl(kryo.readObject(input, URL.class));
        svc.setRequiredHandlers(kryo.readObject(input, HashSet.class));
        svc.setTheme(kryo.readObject(input, String.class));
        svc.setPublicKey(readObjectByReflection(kryo, input));
        svc.setProxyPolicy(readObjectByReflection(kryo, input));
        svc.setAttributeReleasePolicy(readObjectByReflection(kryo, input));
        svc.setUsernameAttributeProvider(readObjectByReflection(kryo, input));
        svc.setAccessStrategy(readObjectByReflection(kryo, input));
        svc.setMultifactorPolicy(readObjectByReflection(kryo, input));
        svc.setInformationUrl(StringUtils.defaultIfBlank(kryo.readObject(input, String.class), null));
        svc.setPrivacyUrl(StringUtils.defaultIfBlank(kryo.readObject(input, String.class), null));
        svc.setProperties(kryo.readObject(input, HashMap.class));
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
     * @return the t
     */
    private static <T> T readObjectByReflection(final Kryo kryo, final Input input) {
        try {
            final String className = kryo.readObject(input, String.class);
            final Class<T> clazz = (Class<T>) Class.forName(className);
            return kryo.readObject(input, clazz);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
