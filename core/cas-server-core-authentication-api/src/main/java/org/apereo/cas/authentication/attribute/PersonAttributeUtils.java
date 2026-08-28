package org.apereo.cas.authentication.attribute;

import module java.base;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.configuration.model.core.authentication.MappedPrincipalAttributesProperties;
import org.apereo.cas.configuration.model.core.authentication.StubPrincipalAttributesProperties;
import org.apereo.cas.util.CollectionUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.util.StringUtils;

/**
 * This is {@link PersonAttributeUtils}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@UtilityClass
public class PersonAttributeUtils {

    /**
     * New mapped attribute repository.
     *
     * @param properties the properties
     * @return the person attribute dao
     */
    public static PersonAttributeDao newMappedAttributeRepository(
        final MappedPrincipalAttributesProperties properties) {
        val dao = new ComplexPersonAttributeDao();
        val people = new LinkedHashMap<String, Map<String, List<Object>>>();
        properties.getPeople().forEach((key, value) ->
            people.put(key, CollectionUtils.convertDirectedListToMultiValueMap(value)));
        dao.setBackingMap(people);
        dao.setOrder(properties.getOrder());
        dao.setEnabled(properties.getState() != AttributeRepositoryStates.DISABLED);
        dao.putTag("state", properties.getState());
        if (StringUtils.hasText(properties.getId())) {
            dao.setId(properties.getId());
        }
        return dao;
    }
    
    /**
     * New stub attribute repository.
     *
     * @param stub the stub
     * @return the person attribute dao
     */
    public static PersonAttributeDao newStubAttributeRepository(final StubPrincipalAttributesProperties stub) {
        val dao = new StubPersonAttributeDao();
        val backingMap = new LinkedHashMap<String, List<Object>>();
        stub.getAttributes().forEach((key, value) -> {
            val vals = StringUtils.commaDelimitedListToStringArray(value);
            backingMap.put(key, Arrays.stream(vals)
                .map(v -> {
                    val result = BooleanUtils.toBooleanObject(v);
                    if (result != null) {
                        return result;
                    }
                    return v;
                })
                .collect(Collectors.toList()));
        });
        dao.setBackingMap(backingMap);
        dao.setOrder(stub.getOrder());
        dao.setEnabled(stub.getState() != AttributeRepositoryStates.DISABLED);
        dao.putTag("state", stub.getState());
        if (StringUtils.hasText(stub.getId())) {
            dao.setId(stub.getId());
        }
        return dao;
    }
}
