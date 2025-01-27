package org.apereo.cas.okta;

import org.apereo.cas.authentication.attribute.BasePersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.attribute.UsernameAttributeProvider;
import org.apereo.cas.util.function.FunctionUtils;
import com.okta.sdk.client.Client;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link OktaPersonAttributeDao}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Getter
@Setter
public class OktaPersonAttributeDao extends BasePersonAttributeDao {
    private UsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    private final Client oktaClient;

    @Override
    public PersonAttributes getPerson(final String uid, final Set<PersonAttributes> resolvedPeople,
                                       final PersonAttributeDaoFilter filter) {
        val attributes = new HashMap<String, Object>();
        val user = oktaClient.getUser(uid);

        FunctionUtils.doIfNotNull(user.getActivated(), attribute -> attributes.put("oktaUserActivatedDate", user.getActivated().getTime()));
        FunctionUtils.doIfNotNull(user.getCreated(), attribute -> attributes.put("oktaUserCreatedDate", user.getCreated().getTime()));
        FunctionUtils.doIfNotNull(user.getLastLogin(), attribute -> attributes.put("oktaUserLastLoginDate", user.getLastLogin().getTime()));
        FunctionUtils.doIfNotNull(user.getLastUpdated(), attribute -> attributes.put("oktaUserLastUpdatedDate", user.getLastUpdated().getTime()));
        FunctionUtils.doIfNotNull(user.getPasswordChanged(), attribute -> attributes.put("oktaUserPasswordChangedDate", user.getPasswordChanged().getTime()));
        FunctionUtils.doIfNotNull(user.getId(), attribute -> attributes.put("oktaUserId", attribute));
        FunctionUtils.doIfNotNull(user.getStatus(), attribute -> attributes.put("oktaUserStatus", attribute.toString()));
        FunctionUtils.doIfNotNull(user.getType(), attribute -> attributes.put("oktaUserType", attribute));

        val profile = user.getProfile();
        FunctionUtils.doIfNotNull(profile.getCity(), attribute -> attributes.put("oktaCity", attribute));
        FunctionUtils.doIfNotNull(profile.getCostCenter(), attribute -> attributes.put("oktaCostCenter", attribute));
        FunctionUtils.doIfNotNull(profile.getCountryCode(), attribute -> attributes.put("oktaCountryCode", attribute));
        FunctionUtils.doIfNotNull(profile.getDepartment(), attribute -> attributes.put("oktaDepartment", attribute));
        FunctionUtils.doIfNotNull(profile.getDisplayName(), attribute -> attributes.put("oktaDisplayName", attribute));
        FunctionUtils.doIfNotNull(profile.getDivision(), attribute -> attributes.put("oktaDivision", attribute));
        FunctionUtils.doIfNotNull(profile.getEmail(), attribute -> attributes.put("oktaEmail", attribute));
        FunctionUtils.doIfNotNull(profile.getEmployeeNumber(), attribute -> attributes.put("oktaEmployeeNumber", attribute));
        FunctionUtils.doIfNotNull(profile.getFirstName(), attribute -> attributes.put("oktaFirstName", attribute));
        FunctionUtils.doIfNotNull(profile.getHonorificPrefix(), attribute -> attributes.put("oktaPrefix", attribute));
        FunctionUtils.doIfNotNull(profile.getHonorificSuffix(), attribute -> attributes.put("oktaSuffix", attribute));
        FunctionUtils.doIfNotNull(profile.getLastName(), attribute -> attributes.put("oktaLastName", attribute));
        FunctionUtils.doIfNotNull(profile.getLocale(), attribute -> attributes.put("oktaLocale", attribute));
        FunctionUtils.doIfNotNull(profile.getManager(), attribute -> attributes.put("oktaManager", attribute));
        FunctionUtils.doIfNotNull(profile.getManagerId(), attribute -> attributes.put("oktaManagerId", attribute));
        FunctionUtils.doIfNotNull(profile.getMiddleName(), attribute -> attributes.put("oktaMiddleName", attribute));
        FunctionUtils.doIfNotNull(profile.getMobilePhone(), attribute -> attributes.put("oktaMobilePhone", attribute));
        FunctionUtils.doIfNotNull(profile.getNickName(), attribute -> attributes.put("oktaNickName", attribute));
        FunctionUtils.doIfNotNull(profile.getOrganization(), attribute -> attributes.put("oktaOrganization", attribute));
        FunctionUtils.doIfNotNull(profile.getPostalAddress(), attribute -> attributes.put("oktaPostalAddress", attribute));
        FunctionUtils.doIfNotNull(profile.getPreferredLanguage(), attribute -> attributes.put("oktaPreferredLanguage", attribute));
        FunctionUtils.doIfNotNull(profile.getPrimaryPhone(), attribute -> attributes.put("oktaPrimaryPhone", attribute));
        FunctionUtils.doIfNotNull(profile.getSecondEmail(), attribute -> attributes.put("oktaSecondEmail", attribute));
        FunctionUtils.doIfNotNull(profile.getState(), attribute -> attributes.put("oktaState", attribute));
        FunctionUtils.doIfNotNull(profile.getStreetAddress(), attribute -> attributes.put("oktaStreetAddress", attribute));
        FunctionUtils.doIfNotNull(profile.getTimezone(), attribute -> attributes.put("oktaTimezone", attribute));
        FunctionUtils.doIfNotNull(profile.getTitle(), attribute -> attributes.put("oktaTitle", attribute));
        FunctionUtils.doIfNotNull(profile.getLogin(), attribute -> attributes.put("oktaLogin", attribute));

        return new SimplePersonAttributes(uid, PersonAttributeDao.stuffAttributesIntoList(attributes));
    }

    @Override
    public Set<PersonAttributes> getPeople(final Map<String, Object> map, final PersonAttributeDaoFilter filter,
                                            final Set<PersonAttributes> resolvedPeople) {
        return getPeopleWithMultivaluedAttributes(PersonAttributeDao.stuffAttributesIntoList(map), filter);
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> map,
                                                                     final PersonAttributeDaoFilter filter,
                                                                     final Set<PersonAttributes> resolvedPeople) {
        val people = new LinkedHashSet<PersonAttributes>();
        val username = usernameAttributeProvider.getUsernameFromQuery(map);
        val person = getPerson(username, resolvedPeople, filter);
        if (person != null) {
            people.add(person);
        }
        return people;
    }
}
