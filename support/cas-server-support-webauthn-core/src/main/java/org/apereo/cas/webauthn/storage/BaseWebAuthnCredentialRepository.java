package org.apereo.cas.webauthn.storage;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link BaseWebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseWebAuthnCredentialRepository implements WebAuthnCredentialRepository {

    private static final Duration CREDENTIAL_INDEX_EXPIRATION = Duration.ofMinutes(1);

    private final CasConfigurationProperties properties;

    private final CipherExecutor<String, String> cipherExecutor;

    @Getter(AccessLevel.NONE)
    private final CasReentrantLock credentialIndexLock = new CasReentrantLock();

    @Getter(AccessLevel.NONE)
    private final AtomicLong credentialIndexVersion = new AtomicLong();

    @Getter(AccessLevel.NONE)
    private volatile long indexedVersion = -1;

    @Getter(AccessLevel.NONE)
    private volatile Instant credentialIndexExpiresAt = Instant.MIN;

    @Getter(AccessLevel.NONE)
    private volatile Map<ByteArray, Set<String>> usernamesByCredentialId = Map.of();

    @Getter(AccessLevel.NONE)
    private volatile Map<ByteArray, Set<String>> usernamesByUserHandle = Map.of();

    @Override
    public boolean addRegistrationByUsername(final String username, final CredentialRegistration credentialRegistration) {
        val registrations = getRegistrationsByUsername(username);
        registrations.add(credentialRegistration);
        update(username, new HashSet<>(registrations));
        invalidateCredentialIndex();
        return true;
    }

    @Override
    public Optional<CredentialRegistration> getRegistrationByUsernameAndCredentialId(final String username, final ByteArray id) {
        val registrations = getRegistrationsByUsername(username);
        return registrations.stream().filter(credReg -> id.equals(credReg.getCredential().getCredentialId())).findFirst();
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUserHandle(final ByteArray handle) {
        ensureCredentialIndex();
        return usernamesByUserHandle.getOrDefault(handle, Set.of()).stream()
            .flatMap(username -> getRegistrationsByUsername(username).stream())
            .filter(registration -> handle.equals(registration.getUserIdentity().getId()))
            .collect(Collectors.toList());
    }

    @Override
    public boolean removeRegistrationByUsername(final String username, final CredentialRegistration credentialRegistration) {
        val registrations = getRegistrationsByUsername(username);
        val result = registrations.remove(credentialRegistration);
        update(username, new HashSet<>(registrations));
        if (result) {
            invalidateCredentialIndex();
        }
        return result;
    }

    @Override
    public boolean removeRegistrationByUsernameAndCredentialId(final String username, final ByteArray credentialId) {
        val registrations = new HashSet<>(getRegistrationsByUsername(username));
        val removed = registrations.removeIf(registration -> registration.getCredential().getCredentialId().equals(credentialId));
        update(username, registrations);
        if (removed) {
            invalidateCredentialIndex();
        }
        return removed;
    }

    @Override
    public boolean removeAllRegistrations(final String username) {
        update(username, new HashSet<>());
        invalidateCredentialIndex();
        return true;
    }

    @Override
    public void updateSignatureCount(final AssertionResult result) {
        val username = result.getUsername();
        val registration = getRegistrationByUsernameAndCredentialId(username, result.getCredential().getCredentialId())
            .orElseThrow(() -> new NoSuchElementException(String.format("Credential \"%s\" is not registered to user \"%s\"",
                result.getCredential().getCredentialId(), username)));
        val registrations = getRegistrationsByUsername(username);
        registrations.remove(registration);
        registrations.add(registration.withCredential(registration.getCredential().toBuilder()
            .signatureCount(result.getSignatureCount())
            .build()));
        update(username, new HashSet<>(registrations));
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(final String username) {
        return getRegistrationsByUsername(username).stream()
            .map(registration -> PublicKeyCredentialDescriptor
                .builder()
                .id(registration.getCredential().getCredentialId())
                .build())
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(final String username) {
        return getRegistrationsByUsername(username)
            .stream()
            .findAny()
            .map(reg -> reg.getUserIdentity().getId());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(final ByteArray userHandle) {
        return getRegistrationsByUserHandle(userHandle).stream().findAny().map(CredentialRegistration::getUsername);
    }

    @Override
    public Optional<RegisteredCredential> lookup(final ByteArray credentialId, final ByteArray userHandle) {
        ensureCredentialIndex();
        val registration = usernamesByCredentialId.getOrDefault(credentialId, Set.of()).stream()
            .flatMap(username -> getRegistrationsByUsername(username).stream())
            .filter(Objects::nonNull)
            .filter(credReg -> credentialId.equals(credReg.getCredential().getCredentialId()))
            .filter(credReg -> userHandle.equals(credReg.getCredential().getUserHandle()))
            .findAny();

        return registration.flatMap(reg -> Optional.of(RegisteredCredential.builder()
            .credentialId(reg.getCredential().getCredentialId())
            .userHandle(reg.getCredential().getUserHandle())
            .publicKeyCose(reg.getCredential().getPublicKeyCose())
            .signatureCount(reg.getCredential().getSignatureCount())
            .build()));
    }

    @Override
    public Set<RegisteredCredential> lookupAll(final ByteArray credentialId) {
        ensureCredentialIndex();
        return usernamesByCredentialId.getOrDefault(credentialId, Set.of()).stream()
            .flatMap(username -> getRegistrationsByUsername(username).stream())
            .filter(Objects::nonNull)
            .filter(reg -> reg.getCredential().getCredentialId().equals(credentialId))
            .map(reg -> RegisteredCredential.builder()
                .credentialId(reg.getCredential().getCredentialId())
                .userHandle(reg.getCredential().getUserHandle())
                .publicKeyCose(reg.getCredential().getPublicKeyCose())
                .signatureCount(reg.getCredential().getSignatureCount())
                .build())
            .collect(Collectors.toSet());
    }

    @Override
    public void clean() {
        try {
            val webAuthn = properties.getAuthn().getMfa().getWebAuthn().getCore();
            val expirationDate = LocalDate.now(ZoneOffset.UTC)
                .minus(webAuthn.getExpireDevices(), DateTimeUtils.toChronoUnit(webAuthn.getExpireDevicesTimeUnit()));
            LOGGER.debug("Filtering devices based on device expiration date [{}]", expirationDate);

            val expInstant = expirationDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            val removingDevices = stream()
                .filter(Objects::nonNull)
                .filter(d -> d.getRegistrationTime() != null && d.getRegistrationTime().isBefore(expInstant)).toList();
            if (!removingDevices.isEmpty()) {
                LOGGER.debug("There are [{}] expired device(s) remaining in repository. Cleaning...", removingDevices.size());
                removingDevices.forEach(device -> removeRegistrationByUsername(device.getUsername(), device));
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    /**
     * Update records by user.
     *
     * @param username the username
     * @param records  the records
     */
    protected abstract void update(String username, Collection<CredentialRegistration> records);

    /**
     * Invalidate the local credential identifier index.
     */
    protected void invalidateCredentialIndex() {
        credentialIndexVersion.incrementAndGet();
        credentialIndexExpiresAt = Instant.MIN;
    }

    private void ensureCredentialIndex() {
        val currentVersion = credentialIndexVersion.get();
        val now = Instant.now();
        if (indexedVersion == currentVersion && credentialIndexExpiresAt.isAfter(now)) {
            return;
        }
        credentialIndexLock.tryLock(_ -> {
            val refreshVersion = credentialIndexVersion.get();
            val refreshTime = Instant.now();
            if (indexedVersion == refreshVersion && credentialIndexExpiresAt.isAfter(refreshTime)) {
                return;
            }
            val credentialIndex = new HashMap<ByteArray, Set<String>>();
            val userHandleIndex = new HashMap<ByteArray, Set<String>>();
            try (val registrations = stream()) {
                registrations
                    .filter(Objects::nonNull)
                    .filter(registration -> registration.getCredential() != null && registration.getUserIdentity() != null)
                    .filter(registration -> registration.getUsername() != null)
                    .forEach(registration -> {
                        credentialIndex.computeIfAbsent(registration.getCredential().getCredentialId(), _ -> new HashSet<>())
                            .add(registration.getUsername());
                        userHandleIndex.computeIfAbsent(registration.getUserIdentity().getId(), _ -> new HashSet<>())
                            .add(registration.getUsername());
                    });
            }
            if (credentialIndexVersion.get() == refreshVersion) {
                usernamesByCredentialId = immutableIndex(credentialIndex);
                usernamesByUserHandle = immutableIndex(userHandleIndex);
                indexedVersion = refreshVersion;
                credentialIndexExpiresAt = refreshTime.plus(CREDENTIAL_INDEX_EXPIRATION);
            }
        });
    }

    private static Map<ByteArray, Set<String>> immutableIndex(final Map<ByteArray, Set<String>> index) {
        index.replaceAll((_, usernames) -> Set.copyOf(usernames));
        return Map.copyOf(index);
    }
}
