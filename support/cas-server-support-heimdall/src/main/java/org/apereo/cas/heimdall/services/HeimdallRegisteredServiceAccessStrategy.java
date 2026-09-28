package org.apereo.cas.heimdall.services;

import module java.base;
import org.apereo.cas.services.BaseRegisteredServiceAccessStrategy;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * This is {@link HeimdallRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class HeimdallRegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {
    @Serial
    private static final long serialVersionUID = 8304436941944570482L;

    private boolean allowed = true;
}
