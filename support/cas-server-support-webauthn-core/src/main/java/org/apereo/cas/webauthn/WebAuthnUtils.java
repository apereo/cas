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
import org.apache.commons.lang3.Strings;
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
     * Passkey provider, such as a password manager or platform authenticator, that uses the given AAGUID,
     * taken from the bundled snapshot of the community passkey provider AAGUID list.
     *
     * @param aaguid the AAGUID as a UUID string
     * @return the passkey provider
     */
    public static Optional<PasskeyProvider> getPasskeyProvider(final @Nullable String aaguid) {
        return Optional.ofNullable(aaguid)
            .map(id -> PasskeyProviders.PROVIDERS.get(id.toLowerCase(Locale.ROOT)));
    }

    /**
     * Name of the passkey provider that uses the given AAGUID.
     *
     * @param aaguid the AAGUID as a UUID string
     * @return the provider name
     */
    public static Optional<String> getPasskeyProviderName(final @Nullable String aaguid) {
        return getPasskeyProvider(aaguid).map(PasskeyProvider::name);
    }

    /**
     * Passkey provider known by its AAGUID.
     *
     * @param name the provider name for display
     * @param icon the provider icon as an SVG data URI, for light backgrounds when the list offers one
     */
    public record PasskeyProvider(String name, @Nullable String icon) {
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
        private static final String SVG_DATA_URI = "data:image/svg+xml;base64,";

        private static final Map<String, PasskeyProvider> PROVIDERS = load();

        /**
         * Load passkey providers keyed by AAGUID from {@code webauthn-passkey-providers.json}, a snapshot of
         * <a href="https://github.com/passkeydeveloper/passkey-authenticator-aaguids">passkey-authenticator-aaguids</a>.
         * The light icon is preferred over the dark one, and only SVG data URIs are kept, so the page never
         * fetches an icon from elsewhere.
         *
         * @return the passkey providers
         */
        private static Map<String, PasskeyProvider> load() {
            try (val input = new ClassPathResource("webauthn-passkey-providers.json").getInputStream()) {
                val entries = MAPPER.readValue(input, new TypeReference<Map<String, Map<String, String>>>() {
                });
                val providers = new HashMap<String, PasskeyProvider>();
                entries.forEach((aaguid, entry) -> {
                    val name = entry.get("name");
                    if (StringUtils.isNotBlank(name)) {
                        val icon = Stream.of(entry.get("icon_light"), entry.get("icon_dark"))
                            .filter(value -> Strings.CS.startsWith(value, SVG_DATA_URI))
                            .findFirst()
                            .orElse(null);
                        providers.put(aaguid.toLowerCase(Locale.ROOT), new PasskeyProvider(name, icon));
                    }
                });
                return Map.copyOf(providers);
            } catch (final Exception e) {
                throw new IllegalStateException("Unable to load passkey providers", e);
            }
        }
    }

}
