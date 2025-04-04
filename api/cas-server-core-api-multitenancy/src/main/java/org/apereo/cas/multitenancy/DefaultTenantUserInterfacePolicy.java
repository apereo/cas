package org.apereo.cas.multitenancy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serial;

/**
 * This is {@link DefaultTenantUserInterfacePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultTenantUserInterfacePolicy implements TenantUserInterfacePolicy {
    @Serial
    private static final long serialVersionUID = 3238689336491450327L;
}
