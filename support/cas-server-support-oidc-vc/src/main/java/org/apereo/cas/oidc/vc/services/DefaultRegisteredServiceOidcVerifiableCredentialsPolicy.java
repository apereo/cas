package org.apereo.cas.oidc.vc.services;

import module java.base;
import org.apereo.cas.services.RegisteredServiceOidcVerifiableCredentialsPolicy;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * This is {@link DefaultRegisteredServiceOidcVerifiableCredentialsPolicy}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultRegisteredServiceOidcVerifiableCredentialsPolicy implements RegisteredServiceOidcVerifiableCredentialsPolicy {
    @Serial
    private static final long serialVersionUID = 1246436756392637728L;

    private Set<String> allowedCredentialTypes = new HashSet<>();

    private Set<String> credentialSigningAlgValuesSupported = new HashSet<>();

    public DefaultRegisteredServiceOidcVerifiableCredentialsPolicy(final Set<String> allowedCredentialTypes) {
        this(allowedCredentialTypes, new HashSet<>());
    }
}
