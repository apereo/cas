package org.apereo.cas.services;

import org.apereo.cas.util.model.TriStateBoolean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link DefaultRegisteredServiceWebflowInterruptPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultRegisteredServiceWebflowInterruptPolicy implements RegisteredServiceWebflowInterruptPolicy {
    private static final long serialVersionUID = -9011530431859480167L;

    private boolean enabled = true;

    private TriStateBoolean forceExecution = TriStateBoolean.UNDEFINED;
}
