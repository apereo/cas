package org.apereo.cas.services.cassandra;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;

/**
 * This is {@link CassandraRegisteredServiceHolder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Setter
@AllArgsConstructor
@Table(CassandraRegisteredServiceHolder.TABLE_NAME)
public class CassandraRegisteredServiceHolder implements Serializable {
    /**
     * Table name.
     */
    public static final String TABLE_NAME = "casservices";

    private static final long serialVersionUID = -8911404192063509340L;

    @PrimaryKey
    private long id;

    /**
     * The Data.
     */
    private String data;
}
