package org.apereo.cas.jdbc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link PartialWhereClause}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ToString
@Getter
@Setter
public class PartialWhereClause {
    private StringBuilder sql = new StringBuilder();
    private List<String> arguments = new ArrayList<>();
}
