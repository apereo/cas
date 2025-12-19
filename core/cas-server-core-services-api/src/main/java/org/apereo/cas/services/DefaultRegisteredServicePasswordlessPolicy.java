package org.apereo.cas.services;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link DefaultRegisteredServicePasswordlessPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultRegisteredServicePasswordlessPolicy implements RegisteredServicePasswordlessPolicy {
    @Serial
    private static final long serialVersionUID = -1123641531859480167L;

    private boolean enabled = true;
}
