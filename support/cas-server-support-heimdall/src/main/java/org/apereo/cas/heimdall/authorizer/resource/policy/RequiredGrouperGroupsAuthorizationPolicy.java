package org.apereo.cas.heimdall.authorizer.resource.policy;

import org.apereo.cas.grouper.DefaultGrouperFacade;
import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.grouper.GrouperGroupField;
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
import java.io.Serial;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link RequiredGrouperGroupsAuthorizationPolicy}.
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
public class RequiredGrouperGroupsAuthorizationPolicy implements ResourceAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -1244481042826672523L;

    private GrouperGroupField groupField = GrouperGroupField.NAME;

    private Set<String> groups;

    @Override
    public AuthorizationResult evaluate(final AuthorizableResource resource, final AuthorizationRequest request) {
        val facade = new DefaultGrouperFacade();
        val subjectGroups = facade.getGroupsForSubjectId(request.getPrincipal().getId());
        val subjectGroupNames = subjectGroups.stream()
            .flatMap(wsGetGroupsResult -> Stream.of(wsGetGroupsResult.getWsGroups()))
            .map(g -> GrouperFacade.getGrouperGroupAttribute(groupField, g))
            .collect(Collectors.toSet());
        return subjectGroupNames.containsAll(groups) ? AuthorizationResult.granted("OK") : AuthorizationResult.denied("Denied");
    }
}
