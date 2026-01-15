package org.apereo.cas.support.wsfederation.authentication.crypto;

import module java.base;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.util.EncodingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.jooq.lambda.Unchecked;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Data;
import org.springframework.core.io.Resource;

/**
 * This is {@link WsFederationMetadataCertificateProvider}.
 * OpenSAML does not yet support parsing out the WSFED metadata.
 * So instead we rely on the presence of {@link IDPSSODescriptor} and
 * its signing x509 certificate in the metadata to locate the certificate.
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class WsFederationMetadataCertificateProvider implements WsFederationCertificateProvider {
    private final Resource metadataResource;

    private final WsFederationConfiguration configuration;

    private final OpenSamlConfigBean openSamlConfigBean;

    @Override
    public List<Credential> getSigningCredentials() throws Exception {
        try (val is = metadataResource.getInputStream()) {
            val resolver = new InMemoryResourceMetadataResolver(is, openSamlConfigBean);
            resolver.setId(UUID.randomUUID().toString());
            resolver.initialize();
            val criteria = new CriteriaSet(new EntityIdCriterion(configuration.getIdentityProviderIdentifier()),
                new EvaluableEntityRoleEntityDescriptorCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
            LOGGER.debug("Locating entity descriptor in the metadata for [{}]", configuration.getIdentityProviderIdentifier());
            val entityDescriptor = resolver.resolveSingle(criteria);
            val roleDescriptors = entityDescriptor.getRoleDescriptors(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
            val signingDescriptors = roleDescriptors.getFirst().getKeyDescriptors()
                .stream()
                .filter(key -> key.getUse() == UsageType.SIGNING)
                .collect(Collectors.toList());
            Collections.reverse(signingDescriptors);

            return signingDescriptors
                .stream()
                .map(KeyDescriptor::getKeyInfo)
                .map(KeyInfo::getX509Datas)
                .flatMap(List::stream)
                .map(X509Data::getX509Certificates)
                .flatMap(List::stream)
                .map(Unchecked.function(cert -> {
                    LOGGER.debug("Parsing signing certificate [{}]", cert.getValue());
                    val decode = EncodingUtils.decodeBase64(cert.getValue());
                    try (val value = new ByteArrayInputStream(decode)) {
                        return WsFederationCertificateProvider.readCredential(value);
                    }
                }))
                .collect(Collectors.toList());
        }
    }
}
