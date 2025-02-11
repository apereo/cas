package org.apereo.cas.multitenancy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serial;
import java.util.Set;

/**
 * This is {@link DefaultTenantMultifactorAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class DefaultTenantMultifactorAuthenticationPolicy implements TenantMultifactorAuthenticationPolicy {
    @Serial
    private static final long serialVersionUID = -9012299259747093234L;

    private Set<String> globalProviderIds;
}
