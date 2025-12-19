package org.apereo.cas.multitenancy;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link TenantCasAuthenticationProtocolPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class TenantCasAuthenticationProtocolPolicy implements TenantAuthenticationProtocolPolicy {
    @Serial
    private static final long serialVersionUID = -9012299259747093234L;

    private @Nullable Set<String> supportedProtocols;
}
