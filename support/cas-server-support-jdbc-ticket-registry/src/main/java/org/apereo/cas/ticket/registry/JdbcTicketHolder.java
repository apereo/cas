package org.apereo.cas.ticket.registry;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Simple holder class to organize code in {@link JdbcTicketRegistry}
 *
 * Currently realizes Kryo serialized data to a byte array to support {@link org.apereo.cas.util.crypto.CipherExecutor#encode(java.lang.Object) }
 * but we could use {@link com.esotericsoftware.kryo.serializers.BlowfishSerializer} instead, preserve the stream and pass that to JDBC
 *
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
class JdbcTicketHolder {

  private final String id;
  private final byte[] data;
  private final String tgtForeignKey;
  private final String principalId;
}
