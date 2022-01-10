package org.apereo.cas.support.wsfederation.authentication.crypto;

import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.util.EncodingUtils;

import lombok.RequiredArgsConstructor;
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
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class WsFederationMetadataCertificateProvider implements WsFederationCertificateProvider {
    private final Resource metadataResource;

    private final WsFederationConfiguration configuration;

    private final OpenSamlConfigBean openSamlConfigBean;

    @Override
    public List<Credential> getSigningCredentials() throws Exception {
        val resolver = new InMemoryResourceMetadataResolver(metadataResource, openSamlConfigBean);
        resolver.setId(UUID.randomUUID().toString());
        resolver.initialize();
        val criteria = new CriteriaSet(new EntityIdCriterion(configuration.getIdentityProviderIdentifier()),
            new EvaluableEntityRoleEntityDescriptorCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
        val entityDescriptor = resolver.resolveSingle(criteria);
        val roleDescriptors = entityDescriptor.getRoleDescriptors(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        val keyDescriptor = roleDescriptors.get(0)
            .getKeyDescriptors()
            .stream().filter(key -> key.getUse() == UsageType.SIGNING).findFirst().orElseThrow();
        return keyDescriptor.getKeyInfo()
            .getX509Datas()
            .stream()
            .map(X509Data::getX509Certificates)
            .flatMap(List::stream)
            .map(Unchecked.function(cert -> {
                val decode = EncodingUtils.decodeBase64(cert.getValue());
                return WsFederationCertificateProvider.readCredential(new ByteArrayInputStream(decode));
            }))
            .collect(Collectors.toList());
    }

}
