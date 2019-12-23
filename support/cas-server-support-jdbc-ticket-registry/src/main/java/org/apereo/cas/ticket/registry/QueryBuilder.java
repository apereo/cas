package org.apereo.cas.ticket.registry;

import java.util.Optional;

interface QueryBuilder {
  String[] createTableAndIndices(String tableName);

  String countRecords(String tableName);

  default Optional<String> recordSize(String tableName) {
    return Optional.empty();
  }

  String deleteExpiredTicketsQuery(String tableName);

  default String selectQuery(String tableName) {
    return String.format("select tkt.id, tkt.data as data, tgt.data as tgtdata from %s tkt left join ticketgrantingticket tgt on tkt.tgt_fk = tgt.id where tkt.id=?", tableName);
  }

  default String selectByPrincipalQuery() {
    return "select count(*) from ticketgrantingticket where principalid=?";
  }

  default String deleteQuery(String tableName) {
    return String.format("delete from %s where id=?", tableName);
  }

  default String deleteByPrincipalIdQuery() {
    return "delete from ticketgrantingticket where principalid=?";
  }

  default String insertQuery(String tableName) {
    return String.format("insert into %s (id, data, tgt_fk, expiration, principalid) values (?, ?, ?, ?, ?)", tableName);
  }

  default String updateQuery(String tableName) {
    return String.format("update %s set data=?, expiration=?, principalid=? where id=?", tableName);
  }

}
