package org.apereo.cas;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * This is {@link StringBean}. Allows one to declare strings as Spring beans.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StringBean implements Serializable {

    private static final long serialVersionUID = -2216572507148074902L;

    private String id = UUID.randomUUID().toString();

}
