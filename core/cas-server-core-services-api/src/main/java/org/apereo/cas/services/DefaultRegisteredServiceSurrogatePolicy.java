package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link DefaultRegisteredServiceSurrogatePolicy}.
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
public class DefaultRegisteredServiceSurrogatePolicy implements RegisteredServiceSurrogatePolicy {
    @Serial
    private static final long serialVersionUID = -7623641531859480167L;

    private boolean enabled = true;
}
