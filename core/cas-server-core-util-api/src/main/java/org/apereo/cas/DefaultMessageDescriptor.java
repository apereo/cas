package org.apereo.cas;

import module java.base;
import org.apereo.cas.authentication.MessageDescriptor;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Simple parameterized message descriptor with a code that refers to a message bundle key and a default
 * message string to use if no message code can be resolved.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Getter
@AllArgsConstructor(onConstructor_ = @JsonCreator)
@EqualsAndHashCode(of = {"code", "defaultMessage"})
public class DefaultMessageDescriptor implements MessageDescriptor {

    @Serial
    private static final long serialVersionUID = 1227390629186486032L;

    private final String code;

    private final String defaultMessage;

    private final Serializable[] params;

    public DefaultMessageDescriptor(final String code) {
        this(code, code, ArrayUtils.EMPTY_STRING_ARRAY);
    }


}
