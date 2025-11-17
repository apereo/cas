package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about a successful authentication produced by an {@link AuthenticationHandler}.
 * Handler results are naturally immutable since they contain sensitive information that should not be modified outside
 * the {@link AuthenticationHandler} that produced it.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@ToString
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class DefaultAuthenticationHandlerExecutionResult implements AuthenticationHandlerExecutionResult {

    @Serial
    private static final long serialVersionUID = -3113998493287982485L;

    /**
     * The name of the authentication handler that successfully authenticated a credential.
     */
    private String handlerName;

    /**
     * Credential meta data.
     */
    private Credential credential;

    /**
     * Resolved principal for authenticated credential.
     */
    private Principal principal;

    /**
     * List of warnings issued by the authentication source while authenticating the credential.
     */
    private List<MessageDescriptor> warnings = new ArrayList<>();

    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final Credential credential) {
        this(source, credential, null, new ArrayList<>());
    }

    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final Credential credential,
                                                       final Principal principal) {
        this(source, credential, principal, new ArrayList<>());
    }


    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final Credential credential,
                                                       final @NonNull List<MessageDescriptor> warnings) {
        this(source, credential, null, warnings);
    }

    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final Credential credential,
                                                       final Principal principal, final @NonNull List<MessageDescriptor> warnings) {
        this(StringUtils.isBlank(source.getName()) ? source.getClass().getSimpleName() : source.getName(), credential, principal, warnings);
    }

    public DefaultAuthenticationHandlerExecutionResult(final String source, final Principal principal) {
        this(source, new BasicIdentifiableCredential(principal.getId()), principal, new ArrayList<>());
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationHandlerExecutionResult addWarning(final MessageDescriptor message) {
        this.warnings.add(message);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationHandlerExecutionResult clearWarnings() {
        this.warnings.clear();
        return this;
    }
}
