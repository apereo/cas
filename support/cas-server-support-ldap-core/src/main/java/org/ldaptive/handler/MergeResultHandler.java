package org.ldaptive.handler;

import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResponse;

/**
 * Merges the values of the attributes in all entries into a single entry.
 *
 * TODO: REMOVE THIS CLASS
 * THIS CLASS WAS ADDED IN ORDER TO TEST LDAPTATIVE CHANGES. SHOULD BE REMOVED
 * AS SOON AS THE NEW LDAPTIVE LIBRARY IS RELEASED AND INCORPORATED TO CAS
 *
 * @author  Middleware Services
 */
public final class MergeResultHandler extends AbstractEntryHandler<SearchResponse> implements SearchResultHandler {

    public MergeResultHandler() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return LdapUtils.computeHashCode(829);
    }

    public String toString() {
        return "[" + this.getClass().getName() + "@" + this.hashCode() + "]";
    }

    @Override
    public SearchResponse apply(SearchResponse searchResponse) {
        return SearchResponse.merge(searchResponse);
    }
}
