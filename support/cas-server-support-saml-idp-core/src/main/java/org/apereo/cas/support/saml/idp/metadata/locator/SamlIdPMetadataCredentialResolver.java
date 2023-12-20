package org.apereo.cas.support.saml.idp.metadata.locator;

import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.ResolverException;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;

import jakarta.annotation.Nonnull;
import javax.xml.namespace.QName;
import java.util.Objects;

/**
 * This is {@link SamlIdPMetadataCredentialResolver}.
 * This extension passes the entire criteria-set object
 * to the {@link #getRoleDescriptorResolver}, making other criteria available
 * for additional filtering. Specifically, the set might include
 * a reference to {@link SamlIdPSamlRegisteredServiceCriterion}
 * that allows the {@link #getRoleDescriptorResolver} to process metadata (and keys later on)
 * for a specific service, rather than what might have been
 * globally defined in CAS settings.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class SamlIdPMetadataCredentialResolver extends MetadataCredentialResolver {
    @Nonnull
    @Override
    protected Iterable<RoleDescriptor> getRoleDescriptors(
        @Nonnull
        final CriteriaSet criteriaSet, @Nonnull
        final String entityID,
        @Nonnull
        final QName role, final String protocol) throws ResolverException {
        return Objects.requireNonNull(getRoleDescriptorResolver()).resolve(criteriaSet);
    }
}
