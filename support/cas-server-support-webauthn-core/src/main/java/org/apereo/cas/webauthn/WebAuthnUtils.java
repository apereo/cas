package org.apereo.cas.webauthn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.RegisteredCredential;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.Base64Variants;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;
import tools.jackson.databind.json.JsonMapper;

/**
 * This is {@link WebAuthnUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@UtilityClass
public class WebAuthnUtils {
    private static final ObjectMapper MAPPER = JsonMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .defaultBase64Variant(Base64Variants.MODIFIED_FOR_URL)
        .addMixIn(CredentialRegistration.class, CredentialRegistrationMixin.class)
        .addMixIn(CredentialRegistration.CredentialRegistrationBuilder.class, CredentialRegistrationBuilderMixin.class)
        .addMixIn(RegisteredCredential.class, RegisteredCredentialMixin.class)
        .addMixIn(RegisteredCredential.RegisteredCredentialBuilder.class, RegisteredCredentialBuilderMixin.class)
        .findAndAddModules()
        .defaultPrettyPrinter(new DefaultPrettyPrinter())
        .changeDefaultPropertyInclusion(handler -> {
            handler.withValueInclusion(JsonInclude.Include.NON_NULL);
            handler.withContentInclusion(JsonInclude.Include.NON_NULL);
            return handler;
        })
        .build();

    /**
     * Gets mapper instance.
     *
     * @return the instance
     */
    public static ObjectMapper getObjectMapper() {
        return MAPPER;
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

}
