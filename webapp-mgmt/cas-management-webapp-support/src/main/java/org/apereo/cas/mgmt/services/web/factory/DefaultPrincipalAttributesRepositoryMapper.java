package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.services.persondir.support.merger.IAttributeMerger;
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.apereo.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.apereo.services.persondir.support.merger.ReplacingAttributeAdder;

/**
 * Default mapper for converting {@link PrincipalAttributesRepository} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class DefaultPrincipalAttributesRepositoryMapper implements PrincipalAttributesRepositoryMapper {
    @Override
    public void mapPrincipalRepository(final PrincipalAttributesRepository pr, final RegisteredServiceEditBean.ServiceData bean) {
        final RegisteredServiceAttributeReleasePolicyEditBean attrPolicyBean = bean.getAttrRelease();
        if (pr instanceof DefaultPrincipalAttributesRepository) {
            attrPolicyBean.setAttrOption(RegisteredServiceAttributeReleasePolicyEditBean.Types.DEFAULT);
        } else if (pr instanceof AbstractPrincipalAttributesRepository) {
            attrPolicyBean.setAttrOption(RegisteredServiceAttributeReleasePolicyEditBean.Types.CACHED);

            final AbstractPrincipalAttributesRepository cc = (AbstractPrincipalAttributesRepository) pr;

            attrPolicyBean.setCachedExpiration(cc.getExpiration());
            attrPolicyBean.setCachedTimeUnit(cc.getTimeUnit());

            final IAttributeMerger merger = cc.getMergingStrategy() != null ? cc.getMergingStrategy().getAttributeMerger() : null;

            if (merger != null) {
                if (merger instanceof NoncollidingAttributeAdder) {
                    attrPolicyBean.setMergingStrategy(RegisteredServiceAttributeReleasePolicyEditBean
                            .AttributeMergerTypes.ADD);
                } else if (merger instanceof MultivaluedAttributeMerger) {
                    attrPolicyBean.setMergingStrategy(RegisteredServiceAttributeReleasePolicyEditBean
                            .AttributeMergerTypes.MULTIVALUED);
                } else if (merger instanceof ReplacingAttributeAdder) {
                    attrPolicyBean.setMergingStrategy(RegisteredServiceAttributeReleasePolicyEditBean
                            .AttributeMergerTypes.REPLACE);
                }
            }
        }
    }

    @Override
    public PrincipalAttributesRepository toPrincipalRepository(final RegisteredServiceEditBean.ServiceData data) {
        final RegisteredServiceAttributeReleasePolicyEditBean attrRelease = data.getAttrRelease();
        final RegisteredServiceAttributeReleasePolicyEditBean.Types attrType = attrRelease.getAttrOption();

        if (attrType == RegisteredServiceAttributeReleasePolicyEditBean.Types.CACHED) {
            final CachingPrincipalAttributesRepository r = new CachingPrincipalAttributesRepository(
                    attrRelease.getCachedTimeUnit().toUpperCase(), 
                    attrRelease.getCachedExpiration());
            
            switch (attrRelease.getMergingStrategy()) {
                case ADD:
                    r.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.ADD);
                    break;
                case MULTIVALUED:
                    r.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.MULTIVALUED);
                    break;
                case REPLACE:
                    r.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.REPLACE);
                    break;
                default:
                    r.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.NONE);
                    break;
            }
            return r;
        } 
        
        if (attrType == RegisteredServiceAttributeReleasePolicyEditBean.Types.DEFAULT) {
            return new DefaultPrincipalAttributesRepository();
        }

        return null;
    }
}
