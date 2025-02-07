package org.apereo.cas.scim.v2.access;

import org.apereo.cas.scim.v2.ScimService;
import org.apereo.cas.services.BaseRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAccessStrategyRequest;
import org.apereo.cas.services.util.RegisteredServiceAccessStrategyEvaluator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link ScimRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class ScimRegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {

    @Serial
    private static final long serialVersionUID = -3157247044344135788L;

    /**
     * Collection of required attributes
     * for this service to proceed.
     */
    private Map<String, Set<String>> requiredAttributes = new HashMap<>();

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) throws Throwable {
        val service = request.getApplicationContext().getBean(ScimService.BEAN_NAME, ScimService.class);
        val scimService = service.getScimRequestBuilder(Optional.of(request.getRegisteredService()));
        val response = service.findUser(scimService, request.getPrincipalId());
        val scimGroups = new ArrayList<>();
        if (response.isSuccess() && response.getResource().getTotalResults() > 0) {
            val user = response.getResource().getListedResources().getFirst();
            user.getGroups().forEach(group -> {
                group.getDisplay().ifPresent(scimGroups::add);
                group.getValue().ifPresent(scimGroups::add);
                group.getRef().ifPresent(scimGroups::add);
            });
        }
        val allAttributes = new HashMap<>(request.getAttributes());
        if (!scimGroups.isEmpty()) {
            allAttributes.put("scimGroups", scimGroups);
        }
        return RegisteredServiceAccessStrategyEvaluator.builder()
            .requiredAttributes(this.requiredAttributes)
            .build()
            .apply(request.withAttributes(allAttributes));
    }
}
