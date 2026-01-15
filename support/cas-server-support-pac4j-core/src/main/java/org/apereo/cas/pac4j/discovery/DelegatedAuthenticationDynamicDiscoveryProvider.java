package org.apereo.cas.pac4j.discovery;

import module java.base;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.Ordered;

/**
 * This is {@link DelegatedAuthenticationDynamicDiscoveryProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
public class DelegatedAuthenticationDynamicDiscoveryProvider implements Serializable, Ordered {
    @Serial
    private static final long serialVersionUID = -4732358356149712715L;

    private String clientName;

    private int order;
}
