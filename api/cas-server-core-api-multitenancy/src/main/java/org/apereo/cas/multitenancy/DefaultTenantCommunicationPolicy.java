package org.apereo.cas.multitenancy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serial;

/**
 * This is {@link DefaultTenantCommunicationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class DefaultTenantCommunicationPolicy implements TenantCommunicationPolicy {
    @Serial
    private static final long serialVersionUID = 1810371962642100469L;

    private TenantEmailCommunicationPolicy emailCommunicationPolicy;
}
