package org.apereo.cas.pm.jdbc;

import org.apereo.cas.pm.impl.history.PasswordHistoryEntity;

import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
    private static final long serialVersionUID = -7485700281426107428L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;
}
