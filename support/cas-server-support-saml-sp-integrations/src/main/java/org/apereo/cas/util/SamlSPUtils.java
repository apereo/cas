package org.apereo.cas.util;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.configuration.model.support.saml.sps.AbstractSamlSPProperties;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter;
import org.opensaml.saml.metadata.resolver.impl.AbstractBatchMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * This is {@link SamlSPUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class SamlSPUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlSPUtils.class);

    private SamlSPUtils() {
    }

    /**
     * New saml service provider registration.
     *
     * @param sp       the properties
     * @param resolver the resolver
     * @return the saml registered service
     */
    public static SamlRegisteredService newSamlServiceProviderService(final AbstractSamlSPProperties sp,
                                                                      final SamlRegisteredServiceCachingMetadataResolver resolver) {
        if (StringUtils.isBlank(sp.getMetadata())) {
            LOGGER.debug("Skipped registration of [{}] since no metadata location is found", sp.getName());
            return null;
        }

        try {
            final SamlRegisteredService service = new SamlRegisteredService();
            service.setName(sp.getName());
            service.setDescription(sp.getDescription());
            service.setEvaluationOrder(Integer.MIN_VALUE);
            service.setMetadataLocation(sp.getMetadata());

            final List<String> attributesToRelease = new ArrayList<>(sp.getAttributes());
            if (StringUtils.isNotBlank(sp.getNameIdAttribute())) {
                attributesToRelease.add(sp.getNameIdAttribute());
                service.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider(sp.getNameIdAttribute()));
            }
            if (StringUtils.isNotBlank(sp.getNameIdFormat())) {
                service.setRequiredNameIdFormat(sp.getNameIdFormat());
            }

            final Multimap<String, String> attributes = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(attributesToRelease);
            final ChainingAttributeReleasePolicy policy = new ChainingAttributeReleasePolicy();
            policy.addPolicy(new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(attributes)));
            service.setAttributeReleasePolicy(policy);

            service.setMetadataCriteriaRoles(SPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart());
            service.setMetadataCriteriaRemoveEmptyEntitiesDescriptors(true);
            service.setMetadataCriteriaRemoveRolelessEntityDescriptors(true);

            if (StringUtils.isNotBlank(sp.getSignatureLocation())) {
                service.setMetadataSignatureLocation(sp.getSignatureLocation());
            }

            final List<String> entityIDList = determineEntityIdList(sp, resolver, service);

            if (entityIDList.isEmpty()) {
                LOGGER.warn("Skipped registration of [{}] since no metadata entity ids could be found", sp.getName());
                return null;
            }
            final String entityIds = org.springframework.util.StringUtils.collectionToDelimitedString(entityIDList, "|");
            service.setMetadataCriteriaDirection(PredicateFilter.Direction.INCLUDE.name());
            service.setMetadataCriteriaPattern(entityIds);

            LOGGER.debug("Registering saml service [{}] by entity id [{}]", sp.getName(), entityIds);
            service.setServiceId(entityIds);

            service.setSignAssertions(sp.isSignAssertions());
            service.setSignResponses(sp.isSignResponses());

            return service;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static List<String> determineEntityIdList(final AbstractSamlSPProperties sp,
                                                      final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                      final SamlRegisteredService service) {
        final List<String> entityIDList = sp.getEntityIds();
        if (entityIDList.isEmpty()) {
            final MetadataResolver metadataResolver = resolver.resolve(service);

            final List<MetadataResolver> resolvers = new ArrayList<>();
            if (metadataResolver instanceof ChainingMetadataResolver) {
                resolvers.addAll(((ChainingMetadataResolver) metadataResolver).getResolvers());
            } else {
                resolvers.add(metadataResolver);
            }

            resolvers.forEach(r -> {
                if (r instanceof AbstractBatchMetadataResolver) {
                    final Iterator<EntityDescriptor> it = ((AbstractBatchMetadataResolver) r).iterator();
                    final Optional<EntityDescriptor> descriptor =
                            StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
                                    .filter(e -> e.getSPSSODescriptor(SAMLConstants.SAML20P_NS) != null)
                                    .findFirst();
                    if (descriptor.isPresent()) {
                        entityIDList.add(descriptor.get().getEntityID());
                    } else {
                        LOGGER.warn("Skipped registration of [{}] since no entity id could be found", sp.getName());
                    }
                }
            });
        }
        return entityIDList;
    }

    /**
     * Save service only if it's not already found in the registry.
     *
     * @param service         the service
     * @param servicesManager the services manager
     */
    public static void saveService(final RegisteredService service, final ServicesManager servicesManager) {
        servicesManager.load();

        if (servicesManager.findServiceBy(registeredService -> registeredService instanceof SamlRegisteredService
                && registeredService.getServiceId().equals(service.getServiceId())).isEmpty()) {
            LOGGER.info("Service [{}] does not exist in the registry and will be added.", service.getServiceId());
            servicesManager.save(service);
            servicesManager.load();
        } else {
            LOGGER.info("Service [{}] exists in the registry and will not be added again.", service.getServiceId());
        }
    }
}
