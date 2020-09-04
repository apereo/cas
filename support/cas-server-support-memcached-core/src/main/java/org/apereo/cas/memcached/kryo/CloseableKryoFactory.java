package org.apereo.cas.memcached.kryo;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.AttributeMergingStrategy;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.exceptions.MixedPrincipalException;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.memcached.kryo.serial.ImmutableNativeJavaListSerializer;
import org.apereo.cas.memcached.kryo.serial.ImmutableNativeJavaSetSerializer;
import org.apereo.cas.memcached.kryo.serial.RegisteredServiceSerializer;
import org.apereo.cas.memcached.kryo.serial.SimpleWebApplicationServiceSerializer;
import org.apereo.cas.memcached.kryo.serial.ThrowableSerializer;
import org.apereo.cas.memcached.kryo.serial.URLSerializer;
import org.apereo.cas.memcached.kryo.serial.ZonedDateTimeSerializer;
import org.apereo.cas.services.AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.AllowedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceContact;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.DefaultRegisteredServiceProxyTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceServiceTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.GroovyRegisteredServiceAccessStrategy;
import org.apereo.cas.services.GroovyRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.GroovyRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.GroovyRegisteredServiceUsernameProvider;
import org.apereo.cas.services.GroovyScriptAttributeReleasePolicy;
import org.apereo.cas.services.NotPreventedRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.RemoteEndpointServiceAccessStrategy;
import org.apereo.cas.services.RestfulRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnRestfulAttributeReleasePolicy;
import org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.ScriptedRegisteredServiceUsernameProvider;
import org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.services.support.RegisteredServiceChainingAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceScriptedAttributeFilter;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.BaseDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.EncodedTicket;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.apereo.cas.validation.ValidationResponseType;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyMapSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptySetSerializer;
import de.javakaffee.kryoserializers.DateSerializer;
import de.javakaffee.kryoserializers.EnumMapSerializer;
import de.javakaffee.kryoserializers.EnumSetSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.RegexSerializer;
import de.javakaffee.kryoserializers.URISerializer;
import de.javakaffee.kryoserializers.UUIDSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableListSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import de.javakaffee.kryoserializers.jodatime.JodaDateTimeSerializer;
import de.javakaffee.kryoserializers.jodatime.JodaLocalDateSerializer;
import de.javakaffee.kryoserializers.jodatime.JodaLocalDateTimeSerializer;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.objenesis.strategy.StdInstantiatorStrategy;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * This is {@link CloseableKryoFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class CloseableKryoFactory implements KryoFactory {

    private final CasKryoPool kryoPool;

    private Collection<Class> classesToRegister = new ArrayList<>(0);

    private boolean warnUnregisteredClasses = true;

    private boolean registrationRequired;

    private boolean replaceObjectsByReferences;

    private boolean autoReset;

    @Override
    public Kryo create() {
        val kryo = new CloseableKryo(this.kryoPool);
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
        registerCasServicesAttributeFiltersWithKryo(kryo);
        registerCasServicesUsernameAttributeProvidersWithKryo(kryo);
        registerCasServicesAccessStrategyWithKryo(kryo);
        registerImmutableOrEmptyCollectionsWithKryo(kryo);
        registerCasServicesProxyPolicyWithKryo(kryo);
        registerExceptionsWithKryo(kryo);
        registerMessageDescriptorsWithKryo(kryo);
        registerCasServicesPrincipalAttributeRepositoryWithKryo(kryo);
        registerCasServicesMultifactorPolicyWithKryo(kryo);
        registerCasServicesConsentPolicyWithKryo(kryo);
        registerCasServicesAttributeReleasePolicyWithKryo(kryo);
        registerCasServicesAuthenticationPolicy(kryo);

        classesToRegister.forEach(c -> {
            LOGGER.trace("Registering serializable class [{}] with Kryo", c.getName());
            kryo.register(c);
        });
        return kryo;
    }

    private static void registerImmutableOrEmptyCollectionsWithKryo(final Kryo kryo) {
        LOGGER.debug("Registering immutable/empty collections with Kryo");

        UnmodifiableCollectionsSerializer.registerSerializers(kryo);

        ImmutableListSerializer.registerSerializers(kryo);
        kryo.register(List.of().getClass(), new ImmutableNativeJavaListSerializer());
        kryo.register(List.of("1", "2").getClass(), new ImmutableNativeJavaListSerializer());
        kryo.register(List.of("1", "2", "3", "4").getClass(), new ImmutableNativeJavaListSerializer());

        ImmutableSetSerializer.registerSerializers(kryo);
        kryo.register(Set.of().getClass(), new ImmutableNativeJavaSetSerializer());
        kryo.register(Set.of("1", "2").getClass(), new ImmutableNativeJavaSetSerializer());
        kryo.register(Set.of("1", "2", "3", "4").getClass(), new ImmutableNativeJavaSetSerializer());

        ImmutableMapSerializer.registerSerializers(kryo);
        kryo.register(Map.of().getClass(), new ImmutableMapSerializer());
        kryo.register(Map.of("1", "2").getClass(), new ImmutableMapSerializer());
        kryo.register(Map.of("1", "2", "3", "4", "5", "6").getClass(), new ImmutableMapSerializer());

        ImmutableMultimapSerializer.registerSerializers(kryo);

        kryo.register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
        kryo.register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
        kryo.register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());

        /*
         * Can't directly access Collections classes (private class),
         * so instantiate one and do a getClass().
         */
        val singletonSet = Collections.singleton("key");
        kryo.register(singletonSet.getClass());

        val singletonMap = Collections.singletonMap("key", "value");
        kryo.register(singletonMap.getClass());

        val singletonList = Collections.singletonList("key");
        kryo.register(singletonList.getClass());

        val list = Arrays.asList("key");
        kryo.register(list.getClass(), new ArraysAsListSerializer());
    }

    private static void registerCasServicesWithKryo(final Kryo kryo) {
        kryo.register(RegexRegisteredService.class, new RegisteredServiceSerializer());
        kryo.register(RegisteredServiceLogoutType.class);
        kryo.register(RegisteredServicePublicKeyImpl.class);
        kryo.register(DefaultRegisteredServiceContact.class);
        kryo.register(DefaultRegisteredServiceProperty.class);
        kryo.register(DefaultRegisteredServiceDelegatedAuthenticationPolicy.class);
        kryo.register(DefaultRegisteredServiceExpirationPolicy.class);
        kryo.register(DefaultRegisteredServiceServiceTicketExpirationPolicy.class);
        kryo.register(DefaultRegisteredServiceProxyTicketExpirationPolicy.class);
        kryo.register(DefaultRegisteredServiceDelegatedAuthenticationPolicy.class);
        kryo.register(DefaultRegisteredServiceAcceptableUsagePolicy.class);
        kryo.register(DefaultRegisteredServiceAuthenticationPolicy.class);
        kryo.register(ShibbolethCompatiblePersistentIdGenerator.class);
    }

    private static void registerCasServicesProxyPolicyWithKryo(final Kryo kryo) {
        kryo.register(RegexMatchingRegisteredServiceProxyPolicy.class);
        kryo.register(RefuseRegisteredServiceProxyPolicy.class);
    }

    private static void registerCasServicesAccessStrategyWithKryo(final Kryo kryo) {
        kryo.register(DefaultRegisteredServiceAccessStrategy.class);
        kryo.register(GroovyRegisteredServiceAccessStrategy.class);
        kryo.register(RemoteEndpointServiceAccessStrategy.class);
        kryo.register(TimeBasedRegisteredServiceAccessStrategy.class);
    }

    private static void registerCasServicesUsernameAttributeProvidersWithKryo(final Kryo kryo) {
        kryo.register(PrincipalAttributeRegisteredServiceUsernameProvider.class);
        kryo.register(AnonymousRegisteredServiceUsernameAttributeProvider.class);
        kryo.register(GroovyRegisteredServiceUsernameProvider.class);
        kryo.register(DefaultRegisteredServiceUsernameProvider.class);
        kryo.register(ScriptedRegisteredServiceUsernameProvider.class);
    }

    private static void registerCasServicesAttributeFiltersWithKryo(final Kryo kryo) {
        kryo.register(RegisteredServiceRegexAttributeFilter.class);
        kryo.register(RegisteredServiceChainingAttributeFilter.class);
        kryo.register(RegisteredServiceMappedRegexAttributeFilter.class);
        kryo.register(RegisteredServiceScriptedAttributeFilter.class);
    }

    private static void registerCasAuthenticationWithKryo(final Kryo kryo) {
        kryo.register(SimpleWebApplicationServiceImpl.class, new SimpleWebApplicationServiceSerializer());
        kryo.register(BasicCredentialMetaData.class);
        kryo.register(BasicIdentifiableCredential.class);
        kryo.register(DefaultAuthenticationHandlerExecutionResult.class);
        kryo.register(DefaultAuthentication.class);
        kryo.register(UsernamePasswordCredential.class);
        kryo.register(RememberMeUsernamePasswordCredential.class);
        kryo.register(SimplePrincipal.class);
        kryo.register(HttpBasedServiceCredential.class);
        kryo.register(OneTimePasswordCredential.class);
        kryo.register(PublicKeyFactoryBean.class);
        kryo.register(ValidationResponseType.class);
    }

    private static void registerCasServicesAttributeReleasePolicyWithKryo(final Kryo kryo) {
        kryo.register(ChainingAttributeReleasePolicy.class);
        kryo.register(DenyAllAttributeReleasePolicy.class);
        kryo.register(ReturnAllowedAttributeReleasePolicy.class);
        kryo.register(ReturnAllAttributeReleasePolicy.class);
        kryo.register(ReturnMappedAttributeReleasePolicy.class);
        kryo.register(GroovyScriptAttributeReleasePolicy.class);
        kryo.register(ScriptedRegisteredServiceAttributeReleasePolicy.class);
        kryo.register(ReturnRestfulAttributeReleasePolicy.class);
    }

    private static void registerCasServicesConsentPolicyWithKryo(final Kryo kryo) {
        kryo.register(DefaultRegisteredServiceConsentPolicy.class);
    }

    private static void registerCasServicesMultifactorPolicyWithKryo(final Kryo kryo) {
        kryo.register(DefaultRegisteredServiceMultifactorPolicy.class);
        kryo.register(GroovyRegisteredServiceMultifactorPolicy.class);
        kryo.register(RegisteredServiceMultifactorPolicyFailureModes.class);
    }

    private static void registerCasServicesPrincipalAttributeRepositoryWithKryo(final Kryo kryo) {
        kryo.register(CachingPrincipalAttributesRepository.class);
        kryo.register(DefaultPrincipalAttributesRepository.class);
        kryo.register(AttributeMergingStrategy.class);
    }

    private static void registerExceptionsWithKryo(final Kryo kryo) {
        kryo.register(GeneralSecurityException.class, new ThrowableSerializer());
        kryo.register(PreventedException.class);
        kryo.register(AccountNotFoundException.class, new ThrowableSerializer());
        kryo.register(AccountDisabledException.class);
        kryo.register(AccountExpiredException.class);
        kryo.register(AccountLockedException.class);
        kryo.register(InvalidLoginLocationException.class);
        kryo.register(InvalidLoginTimeException.class);
        kryo.register(PrincipalException.class);
        kryo.register(MixedPrincipalException.class);
        kryo.register(UnauthorizedServiceException.class);
        kryo.register(UnauthorizedServiceForPrincipalException.class);
        kryo.register(UnauthorizedSsoServiceException.class);
    }

    private static void registerCasTicketsWithKryo(final Kryo kryo) {
        kryo.register(TicketGrantingTicketImpl.class);
        kryo.register(ServiceTicketImpl.class);
        kryo.register(ProxyGrantingTicketImpl.class);
        kryo.register(ProxyTicketImpl.class);
        kryo.register(EncodedTicket.class);
        kryo.register(TransientSessionTicketImpl.class);
    }

    private static void registerNativeJdkComponentsWithKryo(final Kryo kryo) {
        kryo.register(Class.class, new DefaultSerializers.ClassSerializer());
        kryo.register(ArrayList.class);
        kryo.register(LinkedList.class);
        kryo.register(HashMap.class);
        kryo.register(LinkedHashMap.class);
        kryo.register(LinkedHashSet.class);
        kryo.register(TreeMap.class);
        kryo.register(TreeSet.class);
        kryo.register(HashSet.class);
        kryo.register(EnumMap.class, new EnumMapSerializer());
        kryo.register(EnumSet.class, new EnumSetSerializer());

        kryo.register(Object[].class);
        kryo.register(String[].class);
        kryo.register(Long[].class);
        kryo.register(Integer[].class);
        kryo.register(Double[].class);
        kryo.register(double[].class);
        kryo.register(float[].class);
        kryo.register(long[].class);
        kryo.register(int[].class);
        kryo.register(byte[].class);
        kryo.register(ByteBuffer.class);

        kryo.register(URL.class, new URLSerializer());
        kryo.register(URI.class, new URISerializer());
        kryo.register(Pattern.class, new RegexSerializer());
        kryo.register(UUID.class, new UUIDSerializer());

        kryo.register(ZonedDateTime.class, new ZonedDateTimeSerializer());
        kryo.register(Date.class, new DateSerializer(Date.class));
        kryo.register(Calendar.class, new GregorianCalendarSerializer());
        kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
        kryo.register(LocalDate.class, new JodaLocalDateSerializer());
        kryo.register(DateTime.class, new JodaDateTimeSerializer());
        kryo.register(LocalDateTime.class, new JodaLocalDateTimeSerializer());
        kryo.register(Clock.systemUTC().getClass());
        kryo.register(ZoneOffset.class);
        kryo.register(EnumSet.class, new EnumSetSerializer());
    }

    private static void registerExpirationPoliciesWithKryo(final Kryo kryo) {
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

    private static void registerMessageDescriptorsWithKryo(final CloseableKryo kryo) {
        kryo.register(DefaultMessageDescriptor.class);
        kryo.register(PasswordExpiringWarningMessageDescriptor.class);
    }

    private static void registerCasServicesAuthenticationPolicy(final CloseableKryo kryo) {
        kryo.register(AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria.class);
        kryo.register(AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria.class);
        kryo.register(AllowedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria.class);
        kryo.register(GroovyRegisteredServiceAuthenticationPolicyCriteria.class);
        kryo.register(NotPreventedRegisteredServiceAuthenticationPolicyCriteria.class);
        kryo.register(RestfulRegisteredServiceAuthenticationPolicyCriteria.class);
    }
}
