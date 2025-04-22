package org.apereo.cas.oidc.federation;

import org.apereo.cas.services.DefaultRegisteredServiceContact;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceContact;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link OidcFederationDefaultTrustChainResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcFederationDefaultTrustChainResolver implements OidcFederationTrustChainResolver {
    private final List<TrustChainResolver> trustChainResolvers;

    @Override
    public Optional<OidcRegisteredService> resolveTrustChains(final String id) throws Exception {
        if (StringUtils.startsWithIgnoreCase(id, "http")) {
            val entityId = new EntityID(id);
            for (val trustChainResolver : trustChainResolvers) {
                val rpMetadataResult = fetchRelyingPartyMetadata(trustChainResolver, entityId);
                if (rpMetadataResult.isPresent()) {
                    val rpMetadata = rpMetadataResult.get();
                    val oidcRegisteredService = extractRegisteredService(entityId, rpMetadata);
                    return Optional.ofNullable(oidcRegisteredService);
                }
            }
        }
        return Optional.empty();
    }

    protected OidcRegisteredService extractRegisteredService(final EntityID entityId, final OIDCClientMetadata rpMetadata) throws Exception {
        val registeredService = new OidcRegisteredService();
        registeredService.assignIdIfNecessary();

        registeredService.setTlsClientAuthSanDns(rpMetadata.getTLSClientAuthSanDNS());
        registeredService.setTlsClientAuthSanEmail(rpMetadata.getTLSClientAuthSanEmail());
        registeredService.setTlsClientAuthSanIp(rpMetadata.getTLSClientAuthSanIP());
        registeredService.setTlsClientAuthSanUri(rpMetadata.getTLSClientAuthSanURI());
        registeredService.setTlsClientAuthSubjectDn(rpMetadata.getTLSClientAuthSubjectDN());

        registeredService.setName(rpMetadata.getName());
        registeredService.setDescription(rpMetadata.getOrganizationName());
        registeredService.setClientId(entityId.toClientID().getValue());

        registeredService.setScopes(new HashSet<>(rpMetadata.getScope().toStringList()));
        registeredService.setApplicationType(rpMetadata.getApplicationType().toString());
        registeredService.setSubjectType(rpMetadata.getSubjectType().toString());

        FunctionUtils.doIfNotNull(rpMetadata.getSectorIDURI(), value -> registeredService.setSectorIdentifierUri(value.toASCIIString()));

        FunctionUtils.doIfNotNull(rpMetadata.getIDTokenJWSAlg(), value -> registeredService.setIdTokenSigningAlg(value.getName()));
        FunctionUtils.doIfNotNull(rpMetadata.getIDTokenJWEAlg(), value -> registeredService.setIdTokenEncryptionAlg(value.getName()));
        FunctionUtils.doIfNotNull(rpMetadata.getIDTokenJWEEnc(), value -> registeredService.setIdTokenEncryptionEncoding(value.getName()));

        FunctionUtils.doIfNotNull(rpMetadata.getUserInfoJWSAlg(), value -> registeredService.setUserInfoSigningAlg(value.getName()));
        FunctionUtils.doIfNotNull(rpMetadata.getUserInfoJWEAlg(), value -> registeredService.setUserInfoEncryptedResponseAlg(value.getName()));
        FunctionUtils.doIfNotNull(rpMetadata.getUserInfoJWEEnc(), value -> registeredService.setUserInfoEncryptedResponseEncoding(value.getName()));

        if (rpMetadata.getEmailContacts() != null) {
            val contacts = rpMetadata.getEmailContacts()
                .stream()
                .map(email -> new DefaultRegisteredServiceContact().setEmail(email))
                .collect(Collectors.<RegisteredServiceContact>toList());
            registeredService.setContacts(contacts);
        }

        if (rpMetadata.getRedirectionURIStrings() != null) {
            val redirectUris = String.join("|", rpMetadata.getRedirectionURIStrings());
            registeredService.setServiceId(redirectUris);
        }

        FunctionUtils.doIfNotNull(rpMetadata.getLogoURI(), value -> registeredService.setLogo(value.toURL().toExternalForm()));
        FunctionUtils.doIfNotNull(rpMetadata.getPolicyURI(), value -> registeredService.setPrivacyUrl(value.toURL().toExternalForm()));
        FunctionUtils.doIfNotNull(rpMetadata.getTermsOfServiceURI(), value -> registeredService.setInformationUrl(value.toURL().toExternalForm()));

        if (rpMetadata.getGrantTypes() != null) {
            val grantTypes = rpMetadata.getGrantTypes().stream().map(GrantType::getShortName).collect(Collectors.toSet());
            registeredService.setSupportedGrantTypes(grantTypes);
        }

        if (rpMetadata.getResponseTypes() != null) {
            val responseTypes = rpMetadata.getResponseTypes().stream().map(ResponseType::toString).collect(Collectors.toSet());
            registeredService.setSupportedResponseTypes(responseTypes);
        }

        if (rpMetadata.getPostLogoutRedirectionURIs() != null) {
            val logoutUrls = rpMetadata.getPostLogoutRedirectionURIs()
                .stream()
                .map(Unchecked.function(url -> url.toURL().toExternalForm()))
                .collect(Collectors.joining(","));
            registeredService.setLogoutUrl(logoutUrls);
        }

        FunctionUtils.doIfNotNull(rpMetadata.getJWKSetURI(),
            value -> registeredService.setJwks(value.toURL().toExternalForm()));
        FunctionUtils.doIfNotNull(rpMetadata.getTokenEndpointAuthMethod(),
            method -> registeredService.setTokenEndpointAuthenticationMethod(method.getValue()));

        return registeredService;
    }

    protected Optional<OIDCClientMetadata> fetchRelyingPartyMetadata(final TrustChainResolver trustChainResolver,
                                                                     final EntityID entityId) {
        return FunctionUtils.doAndHandle(() -> {
            val resolvedChains = trustChainResolver.resolveTrustChains(entityId);
            val chain = resolvedChains.getShortest();
            val metadataPolicy = chain.resolveCombinedMetadataPolicy(EntityType.OPENID_RELYING_PARTY);

            val claims = chain.getLeafConfiguration().getSignedStatement().getJWTClaimsSet();
            val rawRp = (JSONObject) claims.getClaim(EntityType.OPENID_RELYING_PARTY.getValue());
            val clientMetadataJson = metadataPolicy.apply(rawRp);
            return Optional.of(OIDCClientMetadata.parse(clientMetadataJson));
        }, e -> Optional.<OIDCClientMetadata>empty()).get();
    }
}
