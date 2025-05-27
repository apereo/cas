package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.configuration.model.core.authentication.StubPrincipalAttributesProperties;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.util.StringUtils;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link PersonAttributeUtils}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@UtilityClass
public class PersonAttributeUtils {
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
