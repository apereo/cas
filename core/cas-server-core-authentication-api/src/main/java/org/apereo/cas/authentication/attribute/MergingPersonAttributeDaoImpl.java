package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.merger.MultivaluedAttributeMerger;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@link PersonAttributeDao} implementation which iterates over child
 * {@link PersonAttributeDao} queries each with the same data and merges their
 * reported attributes in a configurable way. The default merger is
 * {@link MultivaluedAttributeMerger}.
 *
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @since 7.1.0
 */
public class MergingPersonAttributeDaoImpl extends AbstractAggregatingDefaultQueryPersonAttributeDao {
    public MergingPersonAttributeDaoImpl() {
        setAttributeMerger(new MultivaluedAttributeMerger());
    }

    @Override
    protected Set<PersonAttributes> getAttributesFromDao(final Map<String, List<Object>> seed,
                                                         final boolean isFirstQuery,
                                                         final PersonAttributeDao currentlyConsidering,
                                                         final Set<PersonAttributes> resultPeople,
                                                         final PersonAttributeDaoFilter filter) {
        return currentlyConsidering.getPeopleWithMultivaluedAttributes(seed, filter, resultPeople);
    }
}
