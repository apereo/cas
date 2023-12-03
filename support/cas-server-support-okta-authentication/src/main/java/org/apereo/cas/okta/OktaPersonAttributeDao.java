package org.apereo.cas.okta;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;

import com.okta.sdk.client.Client;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.BasePersonAttributeDao;
import org.apereo.services.persondir.support.CaseInsensitiveNamedPersonImpl;
import org.apereo.services.persondir.support.IUsernameAttributeProvider;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link OktaPersonAttributeDao}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
public class OktaPersonAttributeDao extends BasePersonAttributeDao {
    private IUsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    private final Client oktaClient;
    
    private static Map<String, List<Object>> stuffAttributesIntoList(final Map<String, ?> personAttributesMap) {
        val entries = (Set<? extends Map.Entry<String, ?>>) personAttributesMap.entrySet();
        return entries.stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> CollectionUtils.toCollection(entry.getValue(), ArrayList.class)));
    }

    @Override
    @SuppressWarnings("JavaUtilDate")
    public IPersonAttributes getPerson(final String uid, final Set<IPersonAttributes> resolvedPeople,
                                       final IPersonAttributeDaoFilter filter) {
        val attributes = new HashMap<String, Object>();
        val user = oktaClient.getUser(uid);

        FunctionUtils.doIfNotNull(user.getActivated(), attribute -> attributes.put("oktaUserActivatedDate", user.getActivated().getTime()), LOGGER);
        FunctionUtils.doIfNotNull(user.getCreated(), attribute -> attributes.put("oktaUserCreatedDate", user.getCreated().getTime()), LOGGER);
        FunctionUtils.doIfNotNull(user.getLastLogin(), attribute -> attributes.put("oktaUserLastLoginDate", user.getLastLogin().getTime()), LOGGER);
        FunctionUtils.doIfNotNull(user.getLastUpdated(), attribute -> attributes.put("oktaUserLastUpdatedDate", user.getLastUpdated().getTime()), LOGGER);
        FunctionUtils.doIfNotNull(user.getPasswordChanged(), attribute -> attributes.put("oktaUserPasswordChangedDate", user.getPasswordChanged().getTime()), LOGGER);
        FunctionUtils.doIfNotNull(user.getId(), attribute -> attributes.put("oktaUserId", attribute), LOGGER);
        FunctionUtils.doIfNotNull(user.getStatus(), attribute -> attributes.put("oktaUserStatus", attribute.toString()), LOGGER);
        FunctionUtils.doIfNotNull(user.getType(), attribute -> attributes.put("oktaUserType", attribute), LOGGER);

        val profile = user.getProfile();
        FunctionUtils.doIfNotNull(profile.getCity(), attribute -> attributes.put("oktaCity", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getCostCenter(), attribute -> attributes.put("oktaCostCenter", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getCountryCode(), attribute -> attributes.put("oktaCountryCode", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getDepartment(), attribute -> attributes.put("oktaDepartment", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getDisplayName(), attribute -> attributes.put("oktaDisplayName", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getDivision(), attribute -> attributes.put("oktaDivision", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getEmail(), attribute -> attributes.put("oktaEmail", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getEmployeeNumber(), attribute -> attributes.put("oktaEmployeeNumber", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getFirstName(), attribute -> attributes.put("oktaFirstName", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getHonorificPrefix(), attribute -> attributes.put("oktaPrefix", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getHonorificSuffix(), attribute -> attributes.put("oktaSuffix", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getLastName(), attribute -> attributes.put("oktaLastName", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getLocale(), attribute -> attributes.put("oktaLocale", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getManager(), attribute -> attributes.put("oktaManager", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getManagerId(), attribute -> attributes.put("oktaManagerId", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getMiddleName(), attribute -> attributes.put("oktaMiddleName", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getMobilePhone(), attribute -> attributes.put("oktaMobilePhone", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getNickName(), attribute -> attributes.put("oktaNickName", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getOrganization(), attribute -> attributes.put("oktaOrganization", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getPostalAddress(), attribute -> attributes.put("oktaPostalAddress", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getPreferredLanguage(), attribute -> attributes.put("oktaPreferredLanguage", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getPrimaryPhone(), attribute -> attributes.put("oktaPrimaryPhone", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getSecondEmail(), attribute -> attributes.put("oktaSecondEmail", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getState(), attribute -> attributes.put("oktaState", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getStreetAddress(), attribute -> attributes.put("oktaStreetAddress", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getTimezone(), attribute -> attributes.put("oktaTimezone", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getTitle(), attribute -> attributes.put("oktaTitle", attribute), LOGGER);
        FunctionUtils.doIfNotNull(profile.getLogin(), attribute -> attributes.put("oktaLogin", attribute), LOGGER);

        return new CaseInsensitiveNamedPersonImpl(uid, stuffAttributesIntoList(attributes));
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> map, final IPersonAttributeDaoFilter filter,
                                            final Set<IPersonAttributes> resolvedPeople) {
        return getPeopleWithMultivaluedAttributes(stuffAttributesIntoList(map), filter);
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> map,
                                                                     final IPersonAttributeDaoFilter filter,
                                                                     final Set<IPersonAttributes> resolvedPeople) {
        val people = new LinkedHashSet<IPersonAttributes>();
        val username = this.usernameAttributeProvider.getUsernameFromQuery(map);
        val person = this.getPerson(username, resolvedPeople, filter);
        if (person != null) {
            people.add(person);
        }
        return people;
    }

    @Override
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return new LinkedHashSet<>(0);
    }

    @Override
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        return new LinkedHashSet<>(0);
    }
}
