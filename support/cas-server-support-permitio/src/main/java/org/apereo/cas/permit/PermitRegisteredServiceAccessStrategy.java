package org.apereo.cas.permit;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.services.BaseRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAccessStrategyRequest;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.permit.sdk.Permit;
import io.permit.sdk.PermitConfig;
import io.permit.sdk.enforcement.Resource;
import io.permit.sdk.enforcement.User;
import io.permit.sdk.openapi.models.UserCreate;
import io.permit.sdk.openapi.models.UserRead;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link PermitRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Slf4j
public class PermitRegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {
    @Serial
    private static final long serialVersionUID = -8645706342973605991L;

    private String action;

    private String tenant;

    private String resource;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, Object> context = new HashMap<>();

    @ExpressionLanguageCapable
    private String apiKey;

    private String emailAttributeName = "email";

    private String firstNameAttributeName = "firstname";

    private String lastNameAttributeName = "lastname";

    @ExpressionLanguageCapable
    private String pdpAddress = "https://cloudpdp.api.permit.io";

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) throws Throwable {
        val permit = buildPermitInstance();
        val user = syncUserWithPermit(permit, request);
        return checkPermitForUserAccess(permit, user);
    }

    protected boolean checkPermitForUserAccess(final Permit permit, final UserRead user) throws Throwable {
        val resourceCheck = new Resource.Builder(this.resource)
            .withTenant(this.tenant)
            .withAttributes(new HashMap<>(this.context))
            .build();
        return permit.check(User.fromString(user.key), this.action, resourceCheck);
    }

    protected Permit buildPermitInstance() {
        val permitConfig = new PermitConfig.Builder(SpringExpressionLanguageValueResolver.getInstance().resolve(this.apiKey))
            .withPdpAddress(SpringExpressionLanguageValueResolver.getInstance().resolve(this.pdpAddress))
            .withDebugMode(LOGGER.isDebugEnabled())
            .build();
        return new Permit(permitConfig);
    }

    protected UserRead syncUserWithPermit(final Permit permit, final RegisteredServiceAccessStrategyRequest request) throws Throwable {
        val email = CollectionUtils.firstElement(request.getAttributes().get(emailAttributeName)).orElseThrow().toString();
        val firstname = CollectionUtils.firstElement(request.getAttributes().get(firstNameAttributeName)).orElseThrow().toString();
        val lastname = CollectionUtils.firstElement(request.getAttributes().get(lastNameAttributeName)).orElseThrow().toString();
        val userCreate = new UserCreate(request.getPrincipalId())
            .withEmail(email)
            .withFirstName(firstname)
            .withLastName(lastname)
            .withAttributes(new HashMap<>(request.getAttributes()));
        return permit.api.users.sync(userCreate).getResult();
    }
}
