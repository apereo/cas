package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * Serialization support.
     */
    private static final long serialVersionUID = -3113998493287982485L;

    /**
     * The name of the authentication handler that successfully authenticated a credential.
     */
    private String handlerName;

    /**
     * Credential meta data.
     */
    private CredentialMetaData credentialMetaData;

    /**
     * Resolved principal for authenticated credential.
     */
    private Principal principal;

    /**
     * List of warnings issued by the authentication source while authenticating the credential.
     */
    private List<MessageDescriptor> warnings = new ArrayList<>(0);

    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final CredentialMetaData metaData) {
        this(source, metaData, null, new ArrayList<>(0));
    }


    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final CredentialMetaData metaData,
                                                       final Principal p) {
        this(source, metaData, p, new ArrayList<>(0));
    }


    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final CredentialMetaData metaData,
                                                       final @NonNull List<MessageDescriptor> warnings) {
        this(source, metaData, null, warnings);
    }

    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final CredentialMetaData metaData,
                                                       final Principal p, final @NonNull List<MessageDescriptor> warnings) {
        this(StringUtils.isBlank(source.getName()) ? source.getClass().getSimpleName() : source.getName(), metaData, p, warnings);
    }

    @Override
    public AuthenticationHandlerExecutionResult addWarning(final MessageDescriptor message) {
        this.warnings.add(message);
        return this;
    }

    @Override
    public AuthenticationHandlerExecutionResult clearWarnings() {
        this.warnings.clear();
        return this;
    }
}
