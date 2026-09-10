package org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.util.function.FunctionUtils;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.security.impl.BaseSAMLSimpleSignatureSecurityHandler;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLProtocolContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.RoleDescriptorResolver;
import org.opensaml.saml.saml2.binding.security.impl.SAML2HTTPPostSimpleSignSecurityHandler;
import org.opensaml.saml.saml2.binding.security.impl.SAML2HTTPRedirectDeflateSignatureSecurityHandler;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.SignatureValidationConfiguration;
import org.opensaml.xmlsec.SignatureValidationParameters;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.criterion.SignatureValidationConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureValidationParametersResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xmlsec.signature.support.impl.SignatureAlgorithmValidator;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link SamlObjectSignatureValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SamlObjectSignatureValidator {
    /**
     * The Override signature reference digest methods.
     */
    protected final List<String> overrideSignatureReferenceDigestMethods;

    /**
     * The Override signature algorithms.
     */
    protected final List<String> overrideSignatureAlgorithms;

    /**
     * The Override black listed signature algorithms.
     */
    protected final List<String> overrideBlockedSignatureAlgorithms;

    /**
     * The Override allowed signature signing algorithms.
     */
    protected final List<String> overrideAllowedAlgorithms;

    /**
     * CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * Verify saml profile request if needed.
     *
     * @param profileRequest the profile request
     * @param resolver       the resolver
     * @param request        the request
     * @param context        the context
     * @return true or false
     * @throws Throwable the throwable
     */
    public boolean verifySamlProfileRequest(final SignableSAMLObject profileRequest,
                                            final MetadataResolver resolver,
                                            final HttpServletRequest request,
                                            final MessageContext context) throws Throwable {

        val roleDescriptorResolver = getRoleDescriptorResolver(resolver, context, profileRequest);
        LOGGER.debug("Validating signature for [{}]", profileRequest.getClass().getName());
        val validationParameters = resolveSignatureValidationParameters();

        val signature = profileRequest.getSignature();
        if (signature != null) {
            val signatureValid = validateSignatureOnProfileRequest(
                profileRequest, signature, roleDescriptorResolver, validationParameters);
            context.ensureSubcontext(SAMLPeerEntityContext.class).setAuthenticated(signatureValid);
            return signatureValid;
        }
        return validateSignatureOnAuthenticationRequest(
            profileRequest, request, context, roleDescriptorResolver, validationParameters);
    }

    /**
     * Validate authn request signature.
     *
     * @param profileRequest the authn request
     * @param adaptor        the adaptor
     * @param request        the request
     * @param context        the context
     * @return true or false
     * @throws Throwable the throwable
     */
    public boolean verifySamlProfileRequest(final SignableSAMLObject profileRequest,
                                         final SamlRegisteredServiceMetadataAdaptor adaptor,
                                         final HttpServletRequest request,
                                         final MessageContext context) throws Throwable {

        return verifySamlProfileRequest(profileRequest, adaptor.getMetadataResolver(), request, context);
    }

    protected RoleDescriptorResolver getRoleDescriptorResolver(final MetadataResolver resolver,
                                                               final MessageContext context,
                                                               final SignableSAMLObject profileRequest) throws Exception {
        val idp = casProperties.getAuthn().getSamlIdp();
        return SamlIdPUtils.getRoleDescriptorResolver(resolver, idp.getMetadata().getCore().isRequireValidMetadata());
    }

    private boolean validateSignatureOnAuthenticationRequest(final SignableSAMLObject profileRequest,
                                                          final HttpServletRequest request,
                                                          final MessageContext context,
                                                          final RoleDescriptorResolver roleDescriptorResolver,
                                                          final SignatureValidationParameters validationParameters) throws Throwable {
        val peer = context.ensureSubcontext(SAMLPeerEntityContext.class);
        peer.setEntityId(SamlIdPUtils.getIssuerFromSamlObject(profileRequest));
        peer.setAuthenticated(false);

        val peerEntityId = Objects.requireNonNull(peer.getEntityId());
        LOGGER.debug("Validating request signature for [{}]...", peerEntityId);

        val roleDescriptor = roleDescriptorResolver.resolveSingle(
            new CriteriaSet(new EntityIdCriterion(peerEntityId),
                new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME)));
        peer.setRole(roleDescriptor.getElementQName());
        val protocol = context.ensureSubcontext(SAMLProtocolContext.class);
        protocol.setProtocol(SAMLConstants.SAML20P_NS);

        LOGGER.debug("Building security parameters context for signature validation of [{}]", peerEntityId);
        val secCtx = context.ensureSubcontext(SecurityParametersContext.class);

        LOGGER.debug("Resolving signing credentials for [{}]", peerEntityId);
        val credentials = getSigningCredential(roleDescriptorResolver, profileRequest);
        if (credentials.isEmpty()) {
            throw new SamlException("Signing credentials for validation could not be resolved");
        }

        var foundValidCredential = false;
        val it = credentials.iterator();
        while (!foundValidCredential && it.hasNext()) {
            foundValidCredential = FunctionUtils.doAndHandle(() -> {
                val credential = it.next();
                val resolver = new StaticCredentialResolver(credential);
                val keyResolver = new StaticKeyInfoCredentialResolver(credential);
                val handler = buildSignatureSecurityHandler(context, keyResolver);
                val trustEngine = new ExplicitKeySignatureTrustEngine(resolver, keyResolver);
                validationParameters.setSignatureTrustEngine(trustEngine);
                secCtx.setSignatureValidationParameters(validationParameters);

                handler.setHttpServletRequestSupplier(() -> request);
                LOGGER.debug("Initializing [{}] to execute signature validation for [{}]", handler.getClass().getSimpleName(), peerEntityId);
                handler.initialize();
                LOGGER.debug("Invoking [{}] to handle signature validation for [{}]", handler.getClass().getSimpleName(), peerEntityId);
                handler.invoke(context);
                val signatureValid = peer.isAuthenticated();
                if (signatureValid) {
                    LOGGER.debug("Successfully validated request signature for [{}].",
                        SamlIdPUtils.getIssuerFromSamlObject(profileRequest));
                }
                handler.destroy();
                return signatureValid;
            }, e -> {
                LOGGER.debug(e.getMessage(), e);
                return false;
            }).get();
        }

        FunctionUtils.throwIf(!foundValidCredential, () -> {
            LOGGER.error("No valid credentials could be found to verify the signature for [{}]",
                SamlIdPUtils.getIssuerFromSamlObject(profileRequest));
            return new SamlException("No valid signing credentials for authentication request validation could be resolved");
        });
        return true;
    }

    private static BaseSAMLSimpleSignatureSecurityHandler buildSignatureSecurityHandler(
        final MessageContext context, final StaticKeyInfoCredentialResolver keyInfoCredentialResolver) {
        val bindingContext = context.getSubcontext(SAMLBindingContext.class);
        if (bindingContext != null
            && SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI.equals(bindingContext.getBindingUri())) {
            val handler = new SAML2HTTPPostSimpleSignSecurityHandler();
            handler.setParser(Objects.requireNonNull(XMLObjectProviderRegistrySupport.getParserPool()));
            handler.setKeyInfoResolver(keyInfoCredentialResolver);
            return handler;
        }
        return new SAML2HTTPRedirectDeflateSignatureSecurityHandler();
    }

    private boolean validateSignatureOnProfileRequest(final SignableSAMLObject profileRequest,
                                                   final Signature signature,
                                                   final RoleDescriptorResolver roleDescriptorResolver,
                                                   final SignatureValidationParameters validationParameters) throws Throwable {
        val validator = new SAMLSignatureProfileValidator();
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(profileRequest);
        LOGGER.debug("Validating profile signature for [{}] via [{}]...", issuer,
            validator.getClass().getSimpleName());
        validator.validate(signature);
        LOGGER.debug("Successfully validated profile signature for [{}].", issuer);

        val algorithmValidator = new SignatureAlgorithmValidator(validationParameters);
        LOGGER.debug("Validating signature algorithms for [{}] via [{}]...", issuer,
            algorithmValidator.getClass().getSimpleName());
        algorithmValidator.validate(signature);
        LOGGER.debug("Successfully validated signature algorithms for [{}].", issuer);

        val credentials = getSigningCredential(roleDescriptorResolver, profileRequest);
        if (credentials.isEmpty()) {
            throw new SamlException("Signing credentials for validation could not be resolved based on the provided signature");
        }

        var foundValidCredential = false;
        val it = credentials.iterator();
        while (!foundValidCredential && it.hasNext()) {
            try {
                val credential = it.next();
                LOGGER.debug("Validating signature using credentials for [{}]", credential.getEntityId());
                SignatureValidator.validate(signature, credential);
                LOGGER.info("Successfully validated the request signature.");
                foundValidCredential = true;
            } catch (final Exception e) {
                LOGGER.debug(e.getMessage(), e);
            }
        }

        FunctionUtils.throwIf(!foundValidCredential, () -> {
            LOGGER.error("No valid credentials could be found to verify the signature for [{}]", issuer);
            return new SamlException("No valid signing credentials for profile request validation could be resolved");
        });
        return true;
    }

    private Set<Credential> getSigningCredential(final RoleDescriptorResolver resolver,
                                                 final SignableSAMLObject profileRequest) {
        return FunctionUtils.doUnchecked(() -> {
            val kekCredentialResolver = new MetadataCredentialResolver();
            val config = getSignatureValidationConfiguration();
            kekCredentialResolver.setRoleDescriptorResolver(resolver);
            kekCredentialResolver.setKeyInfoCredentialResolver(
                DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
            kekCredentialResolver.initialize();
            val criteriaSet = new CriteriaSet();
            criteriaSet.add(new SignatureValidationConfigurationCriterion(config));
            criteriaSet.add(new UsageCriterion(UsageType.SIGNING));

            buildEntityCriteriaForSigningCredential(profileRequest, criteriaSet);

            return Sets.newLinkedHashSet(kekCredentialResolver.resolve(criteriaSet));
        });

    }

    /**
     * Build entity criteria for signing credential.
     *
     * @param profileRequest the profile request
     * @param criteriaSet    the criteria set
     */
    protected void buildEntityCriteriaForSigningCredential(final SignableSAMLObject profileRequest, final CriteriaSet criteriaSet) {
        criteriaSet.add(new EntityIdCriterion(SamlIdPUtils.getIssuerFromSamlObject(profileRequest)));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
    }


    /**
     * Gets signature validation configuration.
     *
     * @return the signature validation configuration
     */
    protected SignatureValidationConfiguration getSignatureValidationConfiguration() {
        val config = DefaultSecurityConfigurationBootstrap.buildDefaultSignatureValidationConfiguration();

        if (this.overrideBlockedSignatureAlgorithms != null && !this.overrideBlockedSignatureAlgorithms.isEmpty()) {
            config.setExcludedAlgorithms(this.overrideBlockedSignatureAlgorithms);
            config.setExcludeMerge(true);
        }

        if (this.overrideAllowedAlgorithms != null && !this.overrideAllowedAlgorithms.isEmpty()) {
            config.setIncludedAlgorithms(this.overrideAllowedAlgorithms);
            config.setIncludeMerge(true);
        }

        LOGGER.debug("Signature validation blocked algorithms: [{}]", config.getExcludedAlgorithms());
        LOGGER.debug("Signature validation allowed algorithms: [{}]", config.getIncludedAlgorithms());

        return config;
    }

    /**
     * Resolve signature validation parameters.
     *
     * @return the signature validation parameters
     * @throws ResolverException the resolver exception
     */
    protected SignatureValidationParameters resolveSignatureValidationParameters() throws ResolverException {
        val criteria = new CriteriaSet(
            new SignatureValidationConfigurationCriterion(getSignatureValidationConfiguration()));
        val resolver = new BasicSignatureValidationParametersResolver();
        return Objects.requireNonNull(resolver.resolveSingle(criteria));
    }


}
