package org.apereo.cas.ticket.registry;

class HsqlQueryBuilder implements QueryBuilder {

  @Override
  public String countRecords(String tableName) {
    return String.format("select count(*) from %s", tableName);
  }
  @Override
  public String[] createTableAndIndices(String tableName) {
    return new String[]{
        "CREATE TABLE IF NOT EXISTS ticketgrantingticket (id varchar(300), data BLOB, tgt_fk varchar(300), expiration bigint, principalid varchar(300), PRIMARY KEY(id));",
        String.format("CREATE TABLE IF NOT EXISTS %s (id varchar(300), data BLOB, tgt_fk varchar(300) REFERENCES ticketgrantingticket(id) ON DELETE CASCADE, expiration bigint, principalid varchar(300), PRIMARY KEY(id));", tableName)};
  }

  public String deleteExpiredTicketsQuery(String tableName) {
    return String.format("delete from %s where expiration < unix_millis()", tableName);
  }
}
