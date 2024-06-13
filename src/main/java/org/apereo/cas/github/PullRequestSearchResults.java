package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PullRequestSearchResults {
    @JsonProperty("total_count")
    private int totalCount;

    private List<PullRequest> items;
}
