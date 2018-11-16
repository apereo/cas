/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apereo.cas.triage;

import org.apereo.cas.IssueListener;
import org.apereo.cas.github.Issue;

import java.util.List;

/**
 * {@link IssueListener} that identifies open issues that require triage.
 *
 * @author Andy Wilkinson
 */
final class TriageIssueListener implements IssueListener {

    private final List<TriageFilter> triageFilters;

    private final TriageListener triageListener;

    /**
     * Creates a new {@code TriageIssueListener} that will use the given
     * {@code triageFilters} to identify issues that require triage and notify the given
     * {@code triageListener} of those that do.
     *
     * @param triageFilters  the triage filters
     * @param triageListener the triage listener
     */
    TriageIssueListener(List<TriageFilter> triageFilters, TriageListener triageListener) {
        this.triageFilters = triageFilters;
        this.triageListener = triageListener;
    }

    @Override
    public void onOpenIssue(Issue issue) {
        if (requiresTriage(issue)) {
            this.triageListener.requiresTriage(issue);
        }
    }

    private boolean requiresTriage(Issue issue) {
        for (TriageFilter filter : this.triageFilters) {
            if (filter.triaged(issue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onIssueClosure(Issue issue) {
        this.triageListener.doesNotRequireTriage(issue);
    }

}
