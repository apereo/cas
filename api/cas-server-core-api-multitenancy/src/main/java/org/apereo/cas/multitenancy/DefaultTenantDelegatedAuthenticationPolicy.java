package org.apereo.cas.multitenancy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serial;
import java.util.List;

/**
 * This is {@link DefaultTenantDelegatedAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultTenantDelegatedAuthenticationPolicy implements TenantDelegatedAuthenticationPolicy {
    @Serial
    private static final long serialVersionUID = 1800371962642100469L;
    
    private List<String> allowedProviders;
}
