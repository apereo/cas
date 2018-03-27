package org.apereo.cas.authentication;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;

import java.util.ArrayList;
import java.util.List;
import lombok.ToString;
import lombok.Getter;

/**
 * Contains information about a successful authentication produced by an {@link AuthenticationHandler}.
 * Handler results are naturally immutable since they contain sensitive information that should not be modified outside
 * the {@link AuthenticationHandler} that produced it.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
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
    private List<MessageDescriptor> warnings;

    /**
     * Instantiates a new handler result.
     *
     * @param source   the source
     * @param metaData the meta data
     */
    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final CredentialMetaData metaData) {
        this(source, metaData, null, new ArrayList<>());
    }

    /**
     * Instantiates a new handler result.
     *
     * @param source   the source
     * @param metaData the meta data
     * @param p        the p
     */
    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final CredentialMetaData metaData,
                                                       final Principal p) {
        this(source, metaData, p, new ArrayList<>());
    }

    /**
     * Instantiates a new handler result.
     *
     * @param source   the source
     * @param metaData the meta data
     * @param warnings the warnings
     */
    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final CredentialMetaData metaData,
                                                       final List<MessageDescriptor> warnings) {
        this(source, metaData, null, warnings);
    }

    /**
     * Instantiates a new handler result.
     *
     * @param source   the source
     * @param metaData the meta data
     * @param p        the p
     * @param warnings the warnings
     */
    public DefaultAuthenticationHandlerExecutionResult(final AuthenticationHandler source, final CredentialMetaData metaData,
                                                       final Principal p, @NonNull final List<MessageDescriptor> warnings) {
        this(StringUtils.isBlank(source.getName()) ? source.getClass().getSimpleName() : source.getName(), metaData, p, warnings);
    }

}
