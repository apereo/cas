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

import org.apereo.cas.github.Issue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * A {@link TriageFilter} that considers an issue as having been triaged if it was opened
 * by a collaborator.
 *
 * @author Andy Wilkinson
 */
final class OpenedByCollaboratorTriageFilter implements TriageFilter {

    private static final Logger log = LoggerFactory
        .getLogger(OpenedByCollaboratorTriageFilter.class);

    private final List<String> collaborators;

    OpenedByCollaboratorTriageFilter(List<String> collaborators) {
        this.collaborators = collaborators == null ? Collections.emptyList()
            : collaborators;
    }

    @Override
    public boolean triaged(Issue issue) {
        if (this.collaborators.contains(issue.getUser().getLogin())) {
            log.debug("{} has been triaged. It was opened by {}", issue, issue.getUser());
            return true;
        }
        return false;
    }

}
