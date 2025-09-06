package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link SurrogatePrincipal}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Getter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class SurrogatePrincipal implements Principal {
    @Serial
    private static final long serialVersionUID = 5672386093026290631L;

    private final Principal primary;
    private final Principal surrogate;

    @Override
    public String getId() {
        return Objects.requireNonNull(surrogate).getId();
    }

    @Override
    public Map<String, List<Object>> getAttributes() {
        return Objects.requireNonNull(surrogate).getAttributes();
    }

    @Override
    @CanIgnoreReturnValue
    public Principal getOwner() {
        return this.primary;
    }
}
