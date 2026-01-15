package org.apereo.cas.persondir;

import module java.base;
import org.apereo.cas.authentication.attribute.BasePersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.attribute.UsernameAttributeProvider;
import org.apereo.cas.util.LoggingUtils;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Class implementing a minimal {@link PersonAttributeDao} API only used by CAS which simply reads all
 * the groups from Grouper repository
 * for a given principal and adopts them to {@link PersonAttributes} instance.
 * All other unimplemented methods throw {@link UnsupportedOperationException}
 * <br>
 * This implementation uses Grouper's <i>grouperClient</i> library to query Grouper's back-end repository.
 * <br>
 * <p>
 * Note: All the Grouper server connection configuration for grouperClient is defined in
 * <i>grouper.client.properties</i> file and must be available
 * in client application's (CAS web application) classpath.
 *
 * @author Dmitriy Kopylenko
 * @since 7.1.0
 */
@Getter
@Setter
@Slf4j
public class GrouperPersonAttributeDao extends BasePersonAttributeDao {
    /**
     * Default group attribute.
     */
    public static final String DEFAULT_GROUPER_ATTRIBUTES_KEY = "grouperGroups";

    private UsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    private Map<String, String> parameters = new LinkedHashMap<>();

    private GrouperSubjectType subjectType = GrouperSubjectType.SUBJECT_ID;

    private GroupAttributeValueType groupAttributeValueType = GroupAttributeValueType.NAME;
    
    @Override
    public PersonAttributes getPerson(final String subjectId, final Set<PersonAttributes> resultPeople,
                                      final PersonAttributeDaoFilter filter) {
        if (!this.isEnabled()) {
            return null;
        }
        Objects.requireNonNull(subjectId, "username cannot be null");

        val groupsClient = getGroupsClient();
        switch (this.subjectType) {
            case SUBJECT_IDENTIFIER -> groupsClient.addSubjectIdentifier(subjectId);
            case SUBJECT_ATTRIBUTE_NAME -> groupsClient.addSubjectAttributeName(subjectId);
            case SUBJECT_ID -> groupsClient.addSubjectId(subjectId);
        }

        parameters.forEach(groupsClient::addParam);
        val grouperGroupsAsAttributesMap = new HashMap<String, List<Object>>(1);
        val groupsList = retrieveAttributesFromGrouper(groupsClient);
        grouperGroupsAsAttributesMap.put("grouperGroups", groupsList);
        return new SimplePersonAttributes(subjectId, grouperGroupsAsAttributesMap);
    }

    @Override
    public Set<PersonAttributes> getPeople(final Map<String, Object> query,
                                            final PersonAttributeDaoFilter filter,
                                            final Set<PersonAttributes> resultPeople) {
        val queryAttributes = PersonAttributeDao.stuffAttributesIntoList(query);
        return getPeopleWithMultivaluedAttributes(queryAttributes, filter, resultPeople);
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final PersonAttributeDaoFilter filter,
                                                                     final Set<PersonAttributes> resultPeople) {
        val people = new LinkedHashSet<PersonAttributes>();
        val username = usernameAttributeProvider.getUsernameFromQuery(query);
        val person = getPerson(username, resultPeople, filter);
        if (person != null) {
            people.add(person);
        }
        return people;
    }

    protected List<Object> retrieveAttributesFromGrouper(final GcGetGroups groupsClient) {
        val groupsList = new ArrayList<>();
        try {
            for (val groupsResult : groupsClient.execute().getResults()) {
                val wsGroups = groupsResult.getWsGroups();
                if (wsGroups != null) {
                    for (val group : wsGroups) {
                        switch (groupAttributeValueType) {
                            case NAME -> groupsList.add(group.getName());
                            case EXTENSION -> groupsList.add(group.getExtension());
                            case DISPLAY_EXTENSION -> groupsList.add(group.getDisplayExtension());
                            case DISPLAY_NAME -> groupsList.add(group.getDisplayName());
                            case UUID -> groupsList.add(group.getUuid());
                            case ALTERNATE_NAME -> groupsList.add(group.getAlternateName());
                        }
                    }
                }
            }
            return groupsList;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return groupsList;
    }

    protected GcGetGroups getGroupsClient() {
        return new GcGetGroups();
    }

    enum GroupAttributeValueType {
        /**
         * Display extension attribute type.
         */
        DISPLAY_EXTENSION,
        /**
         * Display name attribute type.
         */
        DISPLAY_NAME,
        /**
         * UUID attribute type.
         */
        UUID,
        /**
         * Alternate name attribute type.
         */
        ALTERNATE_NAME,
        /**
         * Name attribute type.
         */
        NAME,
        /**
         * Extension attribute type.
         */
        EXTENSION
    }
    public enum GrouperSubjectType {
        /**
         * Subject id type.
         */
        SUBJECT_ID,
        /**
         * Subject identifier type.
         */
        SUBJECT_IDENTIFIER,
        /**
         * Subject attribute name type.
         */
        SUBJECT_ATTRIBUTE_NAME
    }
}
