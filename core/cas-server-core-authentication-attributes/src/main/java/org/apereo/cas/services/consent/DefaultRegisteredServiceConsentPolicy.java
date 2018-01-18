package org.apereo.cas.services.consent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.RegisteredServiceConsentPolicy;

import java.util.Set;

/**
 * This is {@link DefaultRegisteredServiceConsentPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class DefaultRegisteredServiceConsentPolicy implements RegisteredServiceConsentPolicy {

    private static final long serialVersionUID = -2771506941879419063L;

    private boolean enabled = true;

    private Set<String> excludedAttributes;

    private Set<String> includeOnlyAttributes;

    public DefaultRegisteredServiceConsentPolicy(final Set<String> excludedAttributes, final Set<String> includeOnlyAttributes) {
        this.excludedAttributes = excludedAttributes;
        this.includeOnlyAttributes = includeOnlyAttributes;
    }

}
