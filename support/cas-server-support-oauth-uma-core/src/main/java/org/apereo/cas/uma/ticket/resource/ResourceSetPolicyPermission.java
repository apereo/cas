package org.apereo.cas.uma.ticket.resource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * This is {@link ResourceSetPolicyPermission}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Embeddable
@Table(name = "UMA_ResourceSetPolicyPermission")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class ResourceSetPolicyPermission implements Serializable {
    private static final int MAP_SIZE = 8;

    private static final long serialVersionUID = 1664113523427391736L;

    @org.springframework.data.annotation.Id
    @Id
    private long id;

    @Column
    private String subject;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private HashSet<String> scopes = new HashSet<>(MAP_SIZE);

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private LinkedHashMap<String, Object> claims = new LinkedHashMap<>(MAP_SIZE);
}
