package org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLProtocolContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.RoleDescriptorResolver;
import org.opensaml.saml.saml2.binding.security.impl.SAML2HTTPRedirectDeflateSignatureSecurityHandler;
import org.opensaml.saml.saml2.core.RequestAbstractType;
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
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

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
    protected final List overrideSignatureReferenceDigestMethods;

    /**
     * The Override signature algorithms.
     */
    protected final List overrideSignatureAlgorithms;

    /**
     * The Override black listed signature algorithms.
     */
    protected final List overrideBlackListedSignatureAlgorithms;

    /**
     * The Override white listed signature signing algorithms.
     */
    protected final List overrideWhiteListedAlgorithms;

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
     * @throws Exception the exception
     */
    public void verifySamlProfileRequestIfNeeded(final RequestAbstractType profileRequest,
                                                 final MetadataResolver resolver,
                                                 final HttpServletRequest request,
                                                 final MessageContext context) throws Exception {

        val roleDescriptorResolver = getRoleDescriptorResolver(resolver, context, profileRequest);

        LOGGER.debug("Validating signature for [{}]", profileRequest.getClass().getName());

        val signature = profileRequest.getSignature();
        if (signature != null) {
            validateSignatureOnProfileRequest(profileRequest, signature, roleDescriptorResolver);
        } else {
            validateSignatureOnAuthenticationRequest(profileRequest, request, context, roleDescriptorResolver);
        }
    }

    /**
     * Validate authn request signature.
     *
     * @param profileRequest the authn request
     * @param adaptor        the adaptor
     * @param request        the request
     * @param context        the context
     * @throws Exception the exception
     */
    public void verifySamlProfileRequestIfNeeded(final RequestAbstractType profileRequest,
                                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                 final HttpServletRequest request,
                                                 final MessageContext context) throws Exception {

        verifySamlProfileRequestIfNeeded(profileRequest, adaptor.getMetadataResolver(), request, context);
    }

    /**
     * Gets role descriptor resolver.
     *
     * @param resolver       the resolver
     * @param context        the context
     * @param profileRequest the profile request
     * @return the role descriptor resolver
     * @throws Exception the exception
     */
    protected RoleDescriptorResolver getRoleDescriptorResolver(final MetadataResolver resolver,
                                                               final MessageContext context,
                                                               final RequestAbstractType profileRequest) throws Exception {
        val idp = casProperties.getAuthn().getSamlIdp();
        return SamlIdPUtils.getRoleDescriptorResolver(resolver, idp.getMetadata().isRequireValidMetadata());
    }

    private void validateSignatureOnAuthenticationRequest(final RequestAbstractType profileRequest, final HttpServletRequest request,
                                                          final MessageContext context,
                                                          final RoleDescriptorResolver roleDescriptorResolver) throws Exception {
        val handler = new SAML2HTTPRedirectDeflateSignatureSecurityHandler();
        val peer = context.getSubcontext(SAMLPeerEntityContext.class, true);
        peer.setEntityId(SamlIdPUtils.getIssuerFromSamlObject(profileRequest));

        val peerEntityId = peer.getEntityId();
        LOGGER.debug("Validating request signature for [{}] via [{}]...", peerEntityId, handler.getClass().getSimpleName());

        val roleDescriptor = roleDescriptorResolver.resolveSingle(
            new CriteriaSet(new EntityIdCriterion(peerEntityId),
                new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME)));
        peer.setRole(roleDescriptor.getElementQName());
        val protocol = context.getSubcontext(SAMLProtocolContext.class, true);
        protocol.setProtocol(SAMLConstants.SAML20P_NS);

        LOGGER.debug("Building security parameters context for signature validation of [{}]", peerEntityId);
        val secCtx = context.getSubcontext(SecurityParametersContext.class, true);
        val validationParams = new SignatureValidationParameters();

        if (overrideBlackListedSignatureAlgorithms != null && !overrideBlackListedSignatureAlgorithms.isEmpty()) {
            validationParams.setBlacklistedAlgorithms(this.overrideBlackListedSignatureAlgorithms);
            LOGGER.debug("Validation override blacklisted algorithms are [{}]", this.overrideWhiteListedAlgorithms);
        }

        if (overrideWhiteListedAlgorithms != null && !overrideWhiteListedAlgorithms.isEmpty()) {
            validationParams.setWhitelistedAlgorithms(this.overrideWhiteListedAlgorithms);
            LOGGER.debug("Validation override whitelisted algorithms are [{}]", this.overrideWhiteListedAlgorithms);
        }

        LOGGER.debug("Resolving signing credentials for [{}]", peerEntityId);
        val credentials = getSigningCredential(roleDescriptorResolver, profileRequest);
        if (credentials == null || credentials.isEmpty()) {
            throw new SamlException("Signing credentials for validation could not be resolved");
        }

        var foundValidCredential = false;
        val it = credentials.iterator();
        while (!foundValidCredential && it.hasNext()) {
            try {
                val c = it.next();

                val resolver = new StaticCredentialResolver(c);
                val keyResolver = new StaticKeyInfoCredentialResolver(c);
                val trustEngine = new ExplicitKeySignatureTrustEngine(resolver, keyResolver);
                validationParams.setSignatureTrustEngine(trustEngine);
                secCtx.setSignatureValidationParameters(validationParams);

                handler.setHttpServletRequest(request);
                LOGGER.debug("Initializing [{}] to execute signature validation for [{}]", handler.getClass().getSimpleName(), peerEntityId);
                handler.initialize();
                LOGGER.debug("Invoking [{}] to handle signature validation for [{}]", handler.getClass().getSimpleName(), peerEntityId);
                handler.invoke(context);
                LOGGER.debug("Successfully validated request signature for [{}].", profileRequest.getIssuer());

                foundValidCredential = true;
            } catch (final Exception e) {
                LOGGER.debug(e.getMessage(), e);
            } finally {
                handler.destroy();
            }
        }

        if (!foundValidCredential) {
            LOGGER.error("No valid credentials could be found to verify the signature for [{}]", profileRequest.getIssuer());
            throw new SamlException("No valid signing credentials for validation could not be resolved");
        }
    }

    private void validateSignatureOnProfileRequest(final RequestAbstractType profileRequest,
                                                   final Signature signature,
                                                   final RoleDescriptorResolver roleDescriptorResolver) throws Exception {
        val validator = new SAMLSignatureProfileValidator();
        LOGGER.debug("Validating profile signature for [{}] via [{}]...", profileRequest.getIssuer(),
            validator.getClass().getSimpleName());
        validator.validate(signature);
        LOGGER.debug("Successfully validated profile signature for [{}].", profileRequest.getIssuer());

        val credentials = getSigningCredential(roleDescriptorResolver, profileRequest);
        if (credentials.isEmpty()) {
            throw new SamlException("Signing credentials for validation could not be resolved based on the provided signature");
        }

        var foundValidCredential = false;
        val it = credentials.iterator();
        while (!foundValidCredential && it.hasNext()) {
            try {
                val c = it.next();
                LOGGER.debug("Validating signature using credentials for [{}]", c.getEntityId());
                SignatureValidator.validate(signature, c);
                LOGGER.info("Successfully validated the request signature.");
                foundValidCredential = true;
            } catch (final Exception e) {
                LOGGER.debug(e.getMessage(), e);
            }
        }

        if (!foundValidCredential) {
            LOGGER.error("No valid credentials could be found to verify the signature for [{}]", profileRequest.getIssuer());
            throw new SamlException("No valid signing credentials for validation could not be resolved");
        }
    }

    @SneakyThrows
    private Set<Credential> getSigningCredential(final RoleDescriptorResolver resolver, final RequestAbstractType profileRequest) {
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

    }

    /**
     * Build entity criteria for signing credential.
     *
     * @param profileRequest the profile request
     * @param criteriaSet    the criteria set
     */
    protected void buildEntityCriteriaForSigningCredential(final RequestAbstractType profileRequest, final CriteriaSet criteriaSet) {
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
        val samlIdp = casProperties.getAuthn().getSamlIdp();

        if (this.overrideBlackListedSignatureAlgorithms != null
            && !samlIdp.getAlgs().getOverrideBlackListedSignatureSigningAlgorithms().isEmpty()) {
            config.setBlacklistedAlgorithms(this.overrideBlackListedSignatureAlgorithms);
            config.setWhitelistMerge(true);
        }

        if (this.overrideWhiteListedAlgorithms != null && !this.overrideWhiteListedAlgorithms.isEmpty()) {
            config.setWhitelistedAlgorithms(this.overrideWhiteListedAlgorithms);
            config.setBlacklistMerge(true);
        }

        LOGGER.debug("Signature validation blacklisted algorithms: [{}]", config.getBlacklistedAlgorithms());
        LOGGER.debug("Signature validation whitelisted algorithms: [{}]", config.getWhitelistedAlgorithms());

        return config;
    }


}
