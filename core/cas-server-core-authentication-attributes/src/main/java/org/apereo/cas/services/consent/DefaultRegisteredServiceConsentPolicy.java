package org.apereo.cas.services.consent;

import org.apereo.cas.services.RegisteredServiceConsentPolicy;
import org.apereo.cas.util.model.TriStateBoolean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * This is {@link DefaultRegisteredServiceConsentPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Accessors(chain = true)
public class DefaultRegisteredServiceConsentPolicy implements RegisteredServiceConsentPolicy {

    private static final long serialVersionUID = -2771506941879419063L;

    private TriStateBoolean status = TriStateBoolean.UNDEFINED;

    private Set<String> excludedAttributes;

    private Set<String> includeOnlyAttributes;

    private int order;

    public DefaultRegisteredServiceConsentPolicy(final Set<String> excludedAttributes,
                                                 final Set<String> includeOnlyAttributes) {
        this.excludedAttributes = excludedAttributes;
        this.includeOnlyAttributes = includeOnlyAttributes;
    }

}
