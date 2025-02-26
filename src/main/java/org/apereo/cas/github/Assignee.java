package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Assignee {

    private final String login;

    @JsonCreator
    public Assignee(@JsonProperty("login") final String login) {
        this.login = login;
    }
}
