package org.apereo.cas.util;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.configuration.model.support.saml.sps.AbstractSamlSPProperties;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter;
import org.opensaml.saml.metadata.resolver.impl.AbstractBatchMetadataResolver;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * This is {@link SamlSPUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@UtilityClass
public class SamlSPUtils {

    /**
     * New saml service provider registration.
     * Precedence of services is lowest so generated service can be overridden by non-generated version.
     * @param sp       the properties
     * @param resolver the resolver
     * @return the saml registered service
     */
    @SneakyThrows
    public static SamlRegisteredService newSamlServiceProviderService(final AbstractSamlSPProperties sp,
                                                                      final SamlRegisteredServiceCachingMetadataResolver resolver) {
        if (StringUtils.isBlank(sp.getMetadata())) {
            LOGGER.debug("Skipped registration of [{}] since no metadata location is defined", sp.getName());
            return null;
        }

        val service = new SamlRegisteredService();
        service.setName(sp.getName());
        service.setDescription(sp.getDescription());
        service.setEvaluationOrder(Ordered.LOWEST_PRECEDENCE);
        service.setMetadataLocation(sp.getMetadata());
        val attributesToRelease = new ArrayList<String>(sp.getAttributes());
        if (StringUtils.isNotBlank(sp.getNameIdAttribute())) {
            attributesToRelease.add(sp.getNameIdAttribute());
            service.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider(sp.getNameIdAttribute()));
        }
        if (StringUtils.isNotBlank(sp.getNameIdFormat())) {
            service.setRequiredNameIdFormat(sp.getNameIdFormat());
        }

        val attributes = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(attributesToRelease);
        val policy = new ChainingAttributeReleasePolicy();
        policy.addPolicy(new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(attributes)));
        service.setAttributeReleasePolicy(policy);

        service.setMetadataCriteriaRoles(SPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart());
        service.setMetadataCriteriaRemoveEmptyEntitiesDescriptors(true);
        service.setMetadataCriteriaRemoveRolelessEntityDescriptors(true);

        if (StringUtils.isNotBlank(sp.getSignatureLocation())) {
            service.setMetadataSignatureLocation(sp.getSignatureLocation());
        }

        val entityIDList = determineEntityIdList(sp, resolver, service);

        if (entityIDList.isEmpty()) {
            LOGGER.warn("Skipped registration of [{}] since no metadata entity ids could be found", sp.getName());
            return null;
        }
        val entityIds = org.springframework.util.StringUtils.collectionToDelimitedString(entityIDList, "|");
        service.setMetadataCriteriaDirection(PredicateFilter.Direction.INCLUDE.name());
        service.setMetadataCriteriaPattern(entityIds);

        LOGGER.debug("Registering saml service [{}] by entity id [{}]", sp.getName(), entityIds);
        service.setServiceId(entityIds);

        service.setSignAssertions(sp.isSignAssertions());
        service.setSignResponses(sp.isSignResponses());

        return service;
    }

    private static List<String> determineEntityIdList(final AbstractSamlSPProperties sp,
                                                      final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                      final SamlRegisteredService service) {
        val entityIDList = sp.getEntityIds();
        if (entityIDList.isEmpty()) {

            val criteriaSet = new CriteriaSet();
            criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
            val metadataResolver = resolver.resolve(service, criteriaSet);

            val resolvers = new ArrayList<MetadataResolver>();
            if (metadataResolver instanceof ChainingMetadataResolver) {
                resolvers.addAll(((ChainingMetadataResolver) metadataResolver).getResolvers());
            } else {
                resolvers.add(metadataResolver);
            }

            resolvers.forEach(r -> {
                if (r instanceof AbstractBatchMetadataResolver) {
                    val it = ((AbstractBatchMetadataResolver) r).iterator();
                    val descriptor =
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
        LOGGER.debug("Attempting to save service definition [{}]", service);
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
