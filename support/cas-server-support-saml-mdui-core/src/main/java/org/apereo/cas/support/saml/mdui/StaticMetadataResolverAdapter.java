package org.apereo.cas.support.saml.mdui;

import lombok.NoArgsConstructor;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;

/**
 * A {@link StaticMetadataResolverAdapter} that loads metadata from static xml files
 * served by urls or locally.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@NoArgsConstructor
public class StaticMetadataResolverAdapter extends AbstractMetadataResolverAdapter {
    /**
     * Instantiates a new static metadata resolver adapter.
     *
     * @param metadataResources the metadata resources
     */
    public StaticMetadataResolverAdapter(final Map<Resource, MetadataFilterChain> metadataResources) {
        super(metadataResources);
    }

    @Scheduled(initialDelayString = "${cas.saml.mdui.schedule.start-delay:PT30S}",
        fixedDelayString = "${cas.saml.mdui.schedule.repeat-interval:PT90S}")
    @Override
    public void buildMetadataResolverAggregate() {
        super.buildMetadataResolverAggregate();
    }
}
