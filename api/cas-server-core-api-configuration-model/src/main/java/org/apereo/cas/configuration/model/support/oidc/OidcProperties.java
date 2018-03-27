package org.apereo.cas.configuration.model.support.oidc;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link OidcProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Slf4j
@Getter
@Setter
public class OidcProperties implements Serializable {

    private static final long serialVersionUID = 813028615694269276L;

    /**
     * Timeout that indicates how long should the JWKS file be kept in cache.
     */
    private int jwksCacheInMinutes = 60;

    /**
     * OIDC issuer.
     */
    @RequiredProperty
    private String issuer = "http://localhost:8080/cas/oidc";

    /**
     * Skew value used to massage the authentication issue instance.
     */
    private int skew = 5;

    /**
     * Path to the JWKS file resource used to handle signing/encryption of authentication tokens.
     */
    @RequiredProperty
    private Resource jwksFile = new FileSystemResource("/etc/cas/keystore.jwks");

    /**
     * Whether dynamic registration operates in {@code OPEN} or {@code PROTECTED} mode.
     */
    private String dynamicClientRegistrationMode;

    /**
     * List of supported scopes.
     */
    private List<String> scopes = Stream.of("openid", "profile", "email", "address", "phone", "offline_access").collect(Collectors.toList());

    /**
     * List of supported claims.
     */
    private List<String> claims = Stream.of("sub", "name", "preferred_username", "family_name",
        "given_name", "middle_name", "given_name", "profile", "picture", "nickname", "website",
        "zoneinfo", "locale", "updated_at", "birthdate", "email", "email_verified", "phone_number",
        "phone_number_verified", "address").collect(Collectors.toList());

    /**
     * List of supported subject types.
     */
    private List<String> subjectTypes = Stream.of("public", "pairwise").collect(Collectors.toList());

    /**
     * Mapping of user-defined scopes. Key is the new scope name
     * and value is a comma-separated list of claims mapped to the scope.
     */
    private Map<String, String> userDefinedScopes = new HashMap<>();

    /**
     * Map fixed claims to CAS attributes.
     * Key is the existing claim name for a scope and value is the new attribute
     * that should take its place and value.
     */
    private Map<String, String> claimsMap = new HashMap<>();
}
