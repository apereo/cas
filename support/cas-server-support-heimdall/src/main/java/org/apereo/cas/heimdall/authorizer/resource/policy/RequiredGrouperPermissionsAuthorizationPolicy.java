package org.apereo.cas.heimdall.authorizer.resource.policy;

import module java.base;
import org.apereo.cas.grouper.DefaultGrouperFacade;
import org.apereo.cas.grouper.GrouperPermissionAssignmentsQuery;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.AuthorizationResult;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link RequiredGrouperPermissionsAuthorizationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class RequiredGrouperPermissionsAuthorizationPolicy implements ResourceAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -7263181042826672523L;

    private String attributeDefinition;
    private String roleName;

    @Override
    public AuthorizationResult evaluate(final AuthorizableResource resource, final AuthorizationRequest request) {
        val facade = new DefaultGrouperFacade();
        val assignments = facade.getPermissionAssignments(GrouperPermissionAssignmentsQuery.builder()
            .attributeDefinitionName(attributeDefinition)
            .roleName(roleName)
            .subjectId(request.getPrincipal().getId())
            .build());
        return assignments != null && assignments.getWsPermissionAssigns() != null
            && assignments.getWsPermissionAssigns().length > 0
            ? AuthorizationResult.granted("OK") : AuthorizationResult.denied("Denied");
    }
}
