package org.apereo.cas.support.saml.sts;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.cxf.common.util.ReflectionUtil;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.sts.cache.CacheUtils;
import org.apache.cxf.sts.token.provider.SAMLTokenProvider;
import org.apache.cxf.sts.token.provider.TokenProviderParameters;
import org.apache.cxf.sts.token.provider.TokenProviderResponse;
import org.apache.cxf.sts.token.provider.TokenProviderUtils;
import org.apache.wss4j.common.WSS4JConstants;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.opensaml.saml.common.SAMLVersion;
import org.w3c.dom.Document;

import java.time.Instant;

/**
 * This is {@link SamlTokenProvider}.
 * This class is an extension of the CXF class
 * that allows CAS to support OpenSAML v4 APIs
 * particularly around validation or handling of
 * Conditions. This should be removed in future versions
 * of CXF, v4, where support for OpenSAML v4 exists.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 * @deprecated Since 6.6.0, to be removed when CXF v4 is published.
 */
@Slf4j
@Deprecated(since = "6.6.0")
public class SamlTokenProvider extends SAMLTokenProvider {
    @Override
    public TokenProviderResponse createToken(final TokenProviderParameters tokenParameters) {
        return FunctionUtils.doUnchecked(() -> {
            val keyRequirements = tokenParameters.getKeyRequirements();
            val tokenRequirements = tokenParameters.getTokenRequirements();
            LOGGER.debug("Handling token of type: [{}]", tokenRequirements.getTokenType());

            val doc = DOMUtils.createDocument();
            val privateMethod = ReflectionUtil.getDeclaredMethod(SAMLTokenProvider.class,
                "createSamlToken", TokenProviderParameters.class, byte[].class, Document.class);
            privateMethod.setAccessible(true);
            val assertion = (SamlAssertionWrapper) privateMethod.invoke(this, tokenParameters, null, doc);
            var token = assertion.toDOM(doc);

            val signatureValue = assertion.getSignatureValue();
            if (tokenParameters.getTokenStore() != null && signatureValue != null
                && signatureValue.length > 0) {

                val securityToken = CacheUtils.createSecurityTokenForStorage(token, assertion.getId(),
                    assertion.getNotOnOrAfter(), tokenParameters.getPrincipal(), tokenParameters.getRealm(),
                    tokenParameters.getTokenRequirements().getRenewing());
                CacheUtils.storeTokenInCache(
                    securityToken, tokenParameters.getTokenStore(), signatureValue);
            }

            val response = new TokenProviderResponse();
            val tokenType = tokenRequirements.getTokenType();
            if (WSS4JConstants.WSS_SAML2_TOKEN_TYPE.equals(tokenType)
                || WSS4JConstants.SAML2_NS.equals(tokenType)) {
                response.setTokenId(token.getAttributeNS(null, "ID"));
            } else {
                response.setTokenId(token.getAttributeNS(null, "AssertionID"));
            }

            if (tokenParameters.isEncryptToken()) {
                token = TokenProviderUtils.encryptToken(token, response.getTokenId(),
                    tokenParameters.getStsProperties(),
                    tokenParameters.getEncryptionProperties(),
                    keyRequirements,
                    tokenParameters.getMessageContext());
            }
            response.setToken(token);

            final Instant validFrom;
            final Instant validTill;
            if (assertion.getSamlVersion().equals(SAMLVersion.VERSION_20)) {
                validFrom = assertion.getSaml2().getConditions().getNotBefore();
                validTill = assertion.getSaml2().getConditions().getNotOnOrAfter();
            } else {
                validFrom = assertion.getSaml1().getConditions().getNotBefore();
                validTill = assertion.getSaml1().getConditions().getNotOnOrAfter();
            }
            response.setCreated(validFrom);
            response.setExpires(validTill);
            response.setEntropy(null);
            response.setComputedKey(false);
            LOGGER.debug("SAML Token successfully created");
            return response;
        });
    }
}
