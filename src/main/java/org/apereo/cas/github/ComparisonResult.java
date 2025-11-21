package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ComparisonResult {
    private final String status;
    private final int aheadBy;
    private final int behindBy;
    private final int totalCommits;

    @JsonCreator
    public ComparisonResult(@JsonProperty("status") final String status,
                            @JsonProperty("ahead_by") final int aheadBy,
                            @JsonProperty("behind_by") final int behindBy,
                            @JsonProperty("total_commits") final int totalCommits) {
        this.status = status;
        this.aheadBy = aheadBy;
        this.behindBy = behindBy;
        this.totalCommits = totalCommits;
    }

    public boolean shouldUpdate() {
        return "diverged".equalsIgnoreCase(this.status) && this.behindBy > 0;
    }
}
