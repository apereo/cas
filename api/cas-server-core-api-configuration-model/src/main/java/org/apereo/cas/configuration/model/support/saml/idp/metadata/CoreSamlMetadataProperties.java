package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link CoreSamlMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)

public class CoreSamlMetadataProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -8116473583467202828L;

    /**
     * Whether invalid metadata should eagerly fail quickly on startup
     * once the resource is parsed.
     */
    private boolean failFast = true;

    /**
     * Specifies the maximum number of entries the cache may contain. Note that the cache <b>may evict
     * an entry before this limit is exceeded or temporarily exceed the threshold while evicting</b>.
     * As the cache size grows close to the maximum, the cache evicts entries that are less likely to
     * be used again. For example, the cache may evict an entry because it hasn't been used recently
     * or very often.
     */
    private long cacheMaximumSize = 10_000L;

    /**
     * How long should metadata be cached.
     */
    @DurationCapable
    private String cacheExpiration = "PT24H";

    /**
     * Whether valid metadata is required.
     */
    private boolean requireValidMetadata = true;

    /**
     * Whether metadata generation process
     * should support SSO service POST binding.
     */
    private boolean ssoServicePostBindingEnabled = true;

    /**
     * Whether metadata generation process
     * should support SSO service POST SimpleSign binding.
     */

    private boolean ssoServicePostSimpleSignBindingEnabled = true;

    /**
     * Whether metadata generation process
     * should support SSO service REDIRECT binding.
     */
    private boolean ssoServiceRedirectBindingEnabled = true;

    /**
     * Whether metadata generation process
     * should support SSO service SOAP binding.
     */
    private boolean ssoServiceSoapBindingEnabled = true;

    /**
     * Whether metadata generation process
     * should support SLO service POST binding.
     */
    private boolean sloServicePostBindingEnabled = true;

    /**
     * Whether metadata generation process
     * should support SLO service REDIRECT binding.
     */
    private boolean sloServiceRedirectBindingEnabled = true;

    /**
     * This is the key size that is used when generating the initial keypair
     * that would hold the private/public key for the SAML2 metadata.
     * This setting is only relevant when artifacts needs to be generated.
     */
    private int keySize = 4096;

    /**
     * When attempting to resolve metadata from sources, particularly URLs,
     * this setting controls the number of retry attempts that CAS should execute
     * when metadata resolution fails. Setting this value to a zero or negative value
     * will disable the retry policy.
     */
    private int maximumRetryAttempts = 3;

    /**
     * The algorithm type/name that is used when generating certificates
     * for the SAML2 identity provider.
     * This setting is only relevant when artifacts needs to be generated.
     */
    private String certificateAlgorithm = "SHA512withRSA";
}
