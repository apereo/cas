package org.apereo.cas.webauthn;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.yubico.data.CredentialRegistration;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ClassPathResource;


/**
 * This is {@link WebAuthnUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@UtilityClass
public class WebAuthnUtils {

    private static final ObjectMapper MAPPER = JacksonCodecs
        .json()
        .addMixIn(CredentialRegistration.class, CredentialRegistrationMixin.class)
        .addMixIn(CredentialRegistration.CredentialRegistrationBuilder.class, CredentialRegistrationBuilderMixin.class)
        .addMixIn(RegisteredCredential.class, RegisteredCredentialMixin.class)
        .addMixIn(RegisteredCredential.RegisteredCredentialBuilder.class, RegisteredCredentialBuilderMixin.class)
        .findAndRegisterModules()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Gets mapper instance.
     *
     * @return the instance
     */
    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    /**
     * Format an authenticator AAGUID as a lowercase UUID string. The all-zero AAGUID, which authenticators
     * send when they do not identify themselves, is reported as absent.
     *
     * @param aaguid the AAGUID from the authenticator data
     * @return the AAGUID as a UUID string
     */
    public static Optional<String> toAaguid(final @Nullable ByteArray aaguid) {
        if (aaguid == null || aaguid.size() != 16 || aaguid.equals(new ByteArray(new byte[16]))) {
            return Optional.empty();
        }
        val buffer = ByteBuffer.wrap(aaguid.getBytes());
        return Optional.of(new UUID(buffer.getLong(), buffer.getLong()).toString());
    }

    /**
     * Name of the passkey provider, such as a password manager or platform authenticator, that uses the given
     * AAGUID, taken from the bundled snapshot of the community passkey provider AAGUID list.
     *
     * @param aaguid the AAGUID as a UUID string
     * @return the provider name
     */
    public static Optional<String> getPasskeyProviderName(final @Nullable String aaguid) {
        return Optional.ofNullable(aaguid)
            .map(id -> PasskeyProviders.NAMES.get(id.toLowerCase(Locale.ROOT)));
    }

    @JsonDeserialize(builder = CredentialRegistration.CredentialRegistrationBuilder.class)
    private static final class CredentialRegistrationMixin {
    }

    @JsonPOJOBuilder(withPrefix = StringUtils.EMPTY)
    private static final class CredentialRegistrationBuilderMixin {
    }

    @JsonDeserialize(builder = RegisteredCredential.RegisteredCredentialBuilder.class)
    private static final class RegisteredCredentialMixin {
    }

    @JsonPOJOBuilder(withPrefix = StringUtils.EMPTY)
    private static final class RegisteredCredentialBuilderMixin {
    }

    private static final class PasskeyProviders {
        private static final Map<String, String> NAMES = load();

        /**
         * Load provider names keyed by AAGUID from {@code webauthn-passkey-providers.json}, a names-only snapshot of
         * <a href="https://github.com/passkeydeveloper/passkey-authenticator-aaguids">passkey-authenticator-aaguids</a>.
         *
         * @return the provider names
         */
        private static Map<String, String> load() {
            try (val input = new ClassPathResource("webauthn-passkey-providers.json").getInputStream()) {
                val entries = MAPPER.readValue(input, new TypeReference<Map<String, Map<String, String>>>() {
                });
                val names = new HashMap<String, String>();
                entries.forEach((aaguid, entry) -> {
                    val name = entry.get("name");
                    if (StringUtils.isNotBlank(name)) {
                        names.put(aaguid.toLowerCase(Locale.ROOT), name);
                    }
                });
                return Map.copyOf(names);
            } catch (final Exception e) {
                throw new IllegalStateException("Unable to load passkey provider names", e);
            }
        }
    }

}
