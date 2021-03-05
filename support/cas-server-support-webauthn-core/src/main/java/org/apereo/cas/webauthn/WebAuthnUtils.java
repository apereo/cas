package org.apereo.cas.webauthn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.yubico.data.CredentialRegistration;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.webauthn.RegisteredCredential;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

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
        .setDefaultPrettyPrinter(new DefaultPrettyPrinter())
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

    @JsonDeserialize(builder = CredentialRegistration.CredentialRegistrationBuilder.class)
    private static class CredentialRegistrationMixin {
    }

    @JsonPOJOBuilder(withPrefix = StringUtils.EMPTY)
    private static class CredentialRegistrationBuilderMixin {
    }

    @JsonDeserialize(builder = RegisteredCredential.RegisteredCredentialBuilder.class)
    private static class RegisteredCredentialMixin {
    }

    @JsonPOJOBuilder(withPrefix = StringUtils.EMPTY)
    private static class RegisteredCredentialBuilderMixin {
    }

}
