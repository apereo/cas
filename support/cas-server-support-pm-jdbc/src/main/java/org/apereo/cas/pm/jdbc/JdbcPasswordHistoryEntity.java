package org.apereo.cas.pm.jdbc;

import org.apereo.cas.pm.impl.history.PasswordHistoryEntity;

import lombok.Getter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;

/**
 * This is {@link JdbcPasswordHistoryEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity
@Table(name = "PasswordHistoryTable")
@Getter
public class JdbcPasswordHistoryEntity extends PasswordHistoryEntity {
    @Serial
    private static final long serialVersionUID = -7485700281426107428L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Override
    public void setId(final long id) {
        super.setId(id);
        this.id = id;
    }
}
