package org.apereo.cas.multitenancy;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link DefaultTenantAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultTenantAuthenticationPolicy implements TenantAuthenticationPolicy {
    @Serial
    private static final long serialVersionUID = -9012299259747093234L;

    private @Nullable List<String> authenticationHandlers;
    
    private @Nullable List<String> attributeRepositories;

    private @Nullable TenantAuthenticationProtocolPolicy authenticationProtocolPolicy;
}
