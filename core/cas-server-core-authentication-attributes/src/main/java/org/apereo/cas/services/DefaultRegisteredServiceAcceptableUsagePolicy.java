package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link DefaultRegisteredServiceAcceptableUsagePolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultRegisteredServiceAcceptableUsagePolicy implements RegisteredServiceAcceptableUsagePolicy {

    private static final long serialVersionUID = -1441506976879419151L;

    private boolean enabled = true;

    private String messageCode;

    private String text;
}
