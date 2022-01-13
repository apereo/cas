package org.apereo.cas.support.wsfederation.authentication.crypto;

import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.util.EncodingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.jooq.lambda.Unchecked;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.X509Data;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
            val keyDescriptors = roleDescriptors.get(0).getKeyDescriptors();
            val keyDescriptor = keyDescriptors
                .stream()
                .filter(key -> key.getUse() == UsageType.SIGNING)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unable to find key descriptor marked for signing usage"));
            return keyDescriptor
                .getKeyInfo()
                .getX509Datas()
                .stream()
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
