package org.apereo.cas.uma.ticket.resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;

/**
 * This is {@link ResourceSet}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@Embeddable
@Table(name = "UMA_ResourceSet")
@EqualsAndHashCode
public class ResourceSet implements Serializable {
    private static final long serialVersionUID = -5529923878827427102L;

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = System.currentTimeMillis();

    @Column
    private String name;

    @Column
    private String uri;

    @Column
    private String type;

    @Lob
    @Column
    private HashSet<String> scopes = new HashSet<>();

    @Column
    private String iconUri;

    @Column
    private String owner;

    @Column
    private String clientId;

    @Lob
    @Column
    private HashSet<ResourceSetPolicy> policies = new HashSet<>();
}
