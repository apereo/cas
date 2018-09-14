package org.apereo.cas.memcached.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyMapSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptySetSerializer;
import de.javakaffee.kryoserializers.EnumMapSerializer;
import de.javakaffee.kryoserializers.EnumSetSerializer;
import de.javakaffee.kryoserializers.RegexSerializer;
import de.javakaffee.kryoserializers.URISerializer;
import de.javakaffee.kryoserializers.UUIDSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableListSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.memcached.kryo.serial.RegisteredServiceSerializer;
import org.apereo.cas.memcached.kryo.serial.SimpleWebApplicationServiceSerializer;
import org.apereo.cas.memcached.kryo.serial.ThrowableSerializer;
import org.apereo.cas.memcached.kryo.serial.URLSerializer;
import org.apereo.cas.memcached.kryo.serial.ZonedDateTimeSerializer;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceContact;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.GroovyScriptAttributeReleasePolicy;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnRestfulAttributeReleasePolicy;
import org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.registry.EncodedTicket;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.BaseDelegatingExpirationPolicy;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicy;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.objenesis.strategy.StdInstantiatorStrategy;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.Setter;

/**
 * This is {@link CloseableKryoFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Setter
public class CloseableKryoFactory implements KryoFactory {

    private Collection<Class> classesToRegister = new ArrayList<>();

    private boolean warnUnregisteredClasses = true;

    private boolean registrationRequired;

    private boolean replaceObjectsByReferences;

    private boolean autoReset;

    private final CasKryoPool kryoPool;

    public CloseableKryoFactory(final CasKryoPool kryoPool) {
        this.kryoPool = kryoPool;
    }

    @Override
    public Kryo create() {
        final Kryo kryo = new CloseableKryo(this.kryoPool);
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        kryo.setWarnUnregisteredClasses(this.warnUnregisteredClasses);
        kryo.setAutoReset(this.autoReset);
        kryo.setReferences(this.replaceObjectsByReferences);
        kryo.setRegistrationRequired(this.registrationRequired);
        LOGGER.debug("Constructing a kryo instance with the following settings:");
        LOGGER.debug("warnUnregisteredClasses: [{}]", this.warnUnregisteredClasses);
        LOGGER.debug("autoReset: [{}]", this.autoReset);
        LOGGER.debug("replaceObjectsByReferences: [{}]", this.replaceObjectsByReferences);
        LOGGER.debug("registrationRequired: [{}]", this.registrationRequired);
        registerCasAuthenticationWithKryo(kryo);
        registerExpirationPoliciesWithKryo(kryo);
        registerCasTicketsWithKryo(kryo);
        registerNativeJdkComponentsWithKryo(kryo);
        registerCasServicesWithKryo(kryo);
        registerImmutableOrEmptyCollectionsWithKryo(kryo);
        classesToRegister.forEach(c -> {
            LOGGER.debug("Registering serializable class [{}] with Kryo", c.getName());
            kryo.register(c);
        });
        return kryo;
    }

    private void registerImmutableOrEmptyCollectionsWithKryo(final Kryo kryo) {
        LOGGER.debug("Registering immutable/empty collections with Kryo");
        UnmodifiableCollectionsSerializer.registerSerializers(kryo);
        ImmutableListSerializer.registerSerializers(kryo);
        ImmutableSetSerializer.registerSerializers(kryo);
        ImmutableMapSerializer.registerSerializers(kryo);
        ImmutableMultimapSerializer.registerSerializers(kryo);
        kryo.register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
        kryo.register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
        kryo.register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());
        // Can't directly access Collections classes (private class), so instantiate one and do a getClass().
        final Set singletonSet = Collections.singleton("key");
        kryo.register(singletonSet.getClass());
        final Map singletonMap = Collections.singletonMap("key", "value");
        kryo.register(singletonMap.getClass());
        final List list = Arrays.asList("key");
        kryo.register(list.getClass(), new ArraysAsListSerializer());
    }

    private void registerCasServicesWithKryo(final Kryo kryo) {
        kryo.register(RegexRegisteredService.class, new RegisteredServiceSerializer());
        kryo.register(RegisteredService.LogoutType.class);
        kryo.register(RegisteredServicePublicKeyImpl.class);
        kryo.register(DefaultRegisteredServiceContact.class);
        kryo.register(DefaultRegisteredServiceDelegatedAuthenticationPolicy.class);
        kryo.register(DefaultRegisteredServiceExpirationPolicy.class);
        kryo.register(RegisteredServiceRegexAttributeFilter.class);
        kryo.register(PrincipalAttributeRegisteredServiceUsernameProvider.class);
        kryo.register(DefaultRegisteredServiceAccessStrategy.class);
        kryo.register(RegexMatchingRegisteredServiceProxyPolicy.class);
        kryo.register(RegisteredServiceMultifactorPolicy.FailureModes.class);

    }

    private void registerCasAuthenticationWithKryo(final Kryo kryo) {
        kryo.register(SimpleWebApplicationServiceImpl.class, new SimpleWebApplicationServiceSerializer());
        kryo.register(BasicCredentialMetaData.class);
        kryo.register(BasicIdentifiableCredential.class);
        kryo.register(DefaultAuthenticationHandlerExecutionResult.class);
        kryo.register(DefaultAuthentication.class);
        kryo.register(UsernamePasswordCredential.class);
        kryo.register(RememberMeUsernamePasswordCredential.class);
        kryo.register(SimplePrincipal.class);
        kryo.register(PublicKeyFactoryBean.class);
        kryo.register(ReturnAllowedAttributeReleasePolicy.class);
        kryo.register(ReturnAllAttributeReleasePolicy.class);
        kryo.register(ReturnMappedAttributeReleasePolicy.class);
        kryo.register(GroovyScriptAttributeReleasePolicy.class);
        kryo.register(ScriptedRegisteredServiceAttributeReleasePolicy.class);
        kryo.register(ReturnRestfulAttributeReleasePolicy.class);
        kryo.register(CachingPrincipalAttributesRepository.class);
        kryo.register(AbstractPrincipalAttributesRepository.class);
        kryo.register(AbstractPrincipalAttributesRepository.MergingStrategy.class);
        kryo.register(DefaultRegisteredServiceConsentPolicy.class);
        kryo.register(DefaultRegisteredServiceMultifactorPolicy.class);
        kryo.register(GeneralSecurityException.class, new ThrowableSerializer());
        kryo.register(PreventedException.class);
        kryo.register(AccountNotFoundException.class, new ThrowableSerializer());
        kryo.register(AccountDisabledException.class);
        kryo.register(AccountExpiredException.class);
        kryo.register(AccountLockedException.class);
        kryo.register(InvalidLoginLocationException.class);
        kryo.register(InvalidLoginTimeException.class);
    }

    private void registerCasTicketsWithKryo(final Kryo kryo) {
        kryo.register(TicketGrantingTicketImpl.class);
        kryo.register(ServiceTicketImpl.class);
        kryo.register(ProxyGrantingTicketImpl.class);
        kryo.register(ProxyTicketImpl.class);
        kryo.register(EncodedTicket.class);
        kryo.register(TransientSessionTicketImpl.class);
    }

    private void registerNativeJdkComponentsWithKryo(final Kryo kryo) {
        kryo.register(Class.class, new DefaultSerializers.ClassSerializer());
        kryo.register(ZonedDateTime.class, new ZonedDateTimeSerializer());
        kryo.register(ArrayList.class);
        kryo.register(HashMap.class);
        kryo.register(LinkedHashMap.class);
        kryo.register(byte[].class);
        kryo.register(LinkedHashSet.class);
        kryo.register(ByteBuffer.class);
        kryo.register(HashSet.class);
        kryo.register(URL.class, new URLSerializer());
        kryo.register(URI.class, new URISerializer());
        kryo.register(Pattern.class, new RegexSerializer());
        kryo.register(UUID.class, new UUIDSerializer());
        kryo.register(EnumMap.class, new EnumMapSerializer());
        kryo.register(EnumSet.class, new EnumSetSerializer());
    }

    private void registerExpirationPoliciesWithKryo(final Kryo kryo) {
        kryo.register(MultiTimeUseOrTimeoutExpirationPolicy.class);
        kryo.register(MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy.class);
        kryo.register(MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy.class);
        kryo.register(NeverExpiresExpirationPolicy.class);
        kryo.register(RememberMeDelegatingExpirationPolicy.class);
        kryo.register(TimeoutExpirationPolicy.class);
        kryo.register(HardTimeoutExpirationPolicy.class);
        kryo.register(AlwaysExpiresExpirationPolicy.class);
        kryo.register(ThrottledUseAndTimeoutExpirationPolicy.class);
        kryo.register(TicketGrantingTicketExpirationPolicy.class);
        kryo.register(BaseDelegatingExpirationPolicy.class);
    }
}
