package org.apereo.cas.support.saml.web.idp.profile.builders.conditions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.Conditions;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlProfileSamlConditionsBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlProfileSamlConditionsBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Conditions> {

    private final CasConfigurationProperties casProperties;

    public SamlProfileSamlConditionsBuilder(final OpenSamlConfigBean configBean,
                                            final CasConfigurationProperties casProperties) {
        super(configBean);
        this.casProperties = casProperties;
    }

    @Override
    public Conditions build(final SamlProfileBuilderContext context) throws SamlException {
        return buildConditions(context);
    }

    /**
     * Build conditions conditions.
     *
     * @param context the context
     * @return the conditions
     * @throws SamlException the saml exception
     */
    protected Conditions buildConditions(final SamlProfileBuilderContext context) throws SamlException {
        val currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        var skewAllowance = context.getRegisteredService().getSkewAllowance() != 0
            ? context.getRegisteredService().getSkewAllowance()
            : Beans.newDuration(casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance()).toSeconds();
        if (skewAllowance != 0) {
            skewAllowance = Beans.newDuration(casProperties.getSamlCore().getSkewAllowance()).toSeconds();
        }

        val audienceUrls = buildConditionsAudiences(context);
        return newConditions(currentDateTime.minusSeconds(skewAllowance),
            currentDateTime.plusSeconds(skewAllowance),
            audienceUrls.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
    }

    protected List<String> buildConditionsAudiences(final SamlProfileBuilderContext context) {
        val audienceUrls = new ArrayList<String>(2);
        if (StringUtils.isNotBlank(context.getRegisteredService().getAssertionAudiences())) {
            val audiences = org.springframework.util.StringUtils.commaDelimitedListToSet(context.getRegisteredService().getAssertionAudiences());
            audienceUrls.addAll(audiences);
        } else {
            audienceUrls.add(context.getAdaptor().getEntityId());
        }
        return audienceUrls;
    }
}
