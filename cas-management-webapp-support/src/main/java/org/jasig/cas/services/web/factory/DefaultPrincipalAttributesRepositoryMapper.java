package org.jasig.cas.services.web.factory;

import org.jasig.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.PrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.jasig.cas.services.web.beans.RegisteredServiceAttributeReleasePolicyEditBean;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.jasig.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.jasig.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.jasig.services.persondir.support.merger.ReplacingAttributeAdder;

/**
 * Default mapper for converting {@link PrincipalAttributesRepository} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class DefaultPrincipalAttributesRepositoryMapper implements PrincipalAttributesRepositoryMapper {
    @Override
    public void mapPrincipalRepository(final PrincipalAttributesRepository pr, final ServiceData bean) {
        final RegisteredServiceAttributeReleasePolicyEditBean attrPolicyBean = bean.getAttrRelease();
        if (pr instanceof DefaultPrincipalAttributesRepository) {
            attrPolicyBean.setAttrOption(RegisteredServiceAttributeReleasePolicyEditBean.Types.DEFAULT.toString());
        } else if (pr instanceof AbstractPrincipalAttributesRepository) {
            attrPolicyBean.setAttrOption(RegisteredServiceAttributeReleasePolicyEditBean.Types.CACHED.toString());

            final AbstractPrincipalAttributesRepository cc = (AbstractPrincipalAttributesRepository) pr;

            attrPolicyBean.setCachedExpiration(cc.getExpiration());
            attrPolicyBean.setCachedTimeUnit(cc.getTimeUnit().name());

            final IAttributeMerger merger = cc.getMergingStrategy().getAttributeMerger();

            if (merger != null) {
                if (merger instanceof NoncollidingAttributeAdder) {
                    attrPolicyBean.setMergingStrategy(RegisteredServiceAttributeReleasePolicyEditBean
                            .AttributeMergerTypes.ADD.toString());
                } else if (merger instanceof MultivaluedAttributeMerger) {
                    attrPolicyBean.setMergingStrategy(RegisteredServiceAttributeReleasePolicyEditBean
                            .AttributeMergerTypes.MULTIVALUED.toString());
                } else if (merger instanceof ReplacingAttributeAdder) {
                    attrPolicyBean.setMergingStrategy(RegisteredServiceAttributeReleasePolicyEditBean
                            .AttributeMergerTypes.REPLACE.toString());
                }
            }
        }
    }
}
