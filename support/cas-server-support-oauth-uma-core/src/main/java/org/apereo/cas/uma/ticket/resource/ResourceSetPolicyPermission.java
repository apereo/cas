package org.apereo.cas.uma.ticket.resource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.io.Serial;
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
@Accessors(chain = true)
public class ResourceSetPolicyPermission implements Serializable {
    @Serial
    private static final long serialVersionUID = 1664113523427391736L;

    @org.springframework.data.annotation.Id
    @Id
    private long id;

    @Column
    private String subject;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private HashSet<String> scopes = new HashSet<>();

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private LinkedHashMap<String, Object> claims = new LinkedHashMap<>();
}
