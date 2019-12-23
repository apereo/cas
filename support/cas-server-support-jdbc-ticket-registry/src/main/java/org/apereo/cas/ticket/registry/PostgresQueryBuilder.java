package org.apereo.cas.ticket.registry;

class PostgresQueryBuilder implements QueryBuilder {

  @Override
  public String countRecords(String tableName) {
    return "SELECT reltuples AS approximate_row_count FROM pg_class WHERE relname = '" + tableName.toLowerCase() + "'";
  }

  @Override
  public String[] createTableAndIndices(String tableName) {
    return new String[]{
        "CREATE TABLE IF NOT EXISTS ticketgrantingticket (id varchar(300), data bytea, tgt_fk varchar(300), expiration bigint, principalid varchar(300), PRIMARY KEY(id));",
            String.format("CREATE TABLE IF NOT EXISTS %s (id varchar(300), data bytea, tgt_fk varchar(300) REFERENCES ticketgrantingticket(id) ON DELETE CASCADE, expiration bigint, principalid varchar(300), PRIMARY KEY(id));", tableName),
            String.format("CREATE INDEX IF NOT EXISTS  %s_expiration ON %s USING btree (expiration);", tableName, tableName),
            String.format("CREATE INDEX IF NOT EXISTS  %s_principalid ON %s USING btree (principalid);", tableName, tableName)};
  }

  public String deleteExpiredTicketsQuery(String tableName) {
    return String.format("delete from %s where expiration < (extract(epoch from now()) * 1000)", tableName);
  }
}
