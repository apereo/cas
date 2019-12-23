package org.apereo.cas.ticket.registry;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.memcached.kryo.CasKryoPool;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.AbstractTicketRegistry;
import org.pac4j.core.exception.http.HttpAction;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.cloud.sleuth.annotation.TagValueResolver;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j

public class JdbcTicketRegistry extends AbstractTicketRegistry {

  // 10 years in seconds
  private static final long MAX_TTL = 10 * 365 * 24 * 60 * 60L;
  private static final String ADD_ACTION = "add";
  private static final String GET_ACTION = "get";
  private static final String DELETE_ACTION = "delete";
  public static final String TICKET_GRANTING_TICKET = "ticketGrantingTicket";
  private static Field causeField = causeField();
  private final TicketCatalog ticketCatalog;
  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final QueryBuilder queryBuilder;
  private final JpaTicketRegistryProperties properties;
  private final MeterRegistry meterRegistry;
  private Map<String, Map<String, Timer>> timers;
  private CasKryoPool kryoPool;

  JdbcTicketRegistry(final TicketCatalog ticketCatalog,
      final JdbcTemplate jdbcTemplate,
      final TransactionTemplate transactionTemplate,
      final JpaTicketRegistryProperties properties,
      final MeterRegistry meterRegistry,
      final CasKryoPool kryoPool) {
    this.ticketCatalog = ticketCatalog;
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
    this.transactionTemplate = transactionTemplate;
    val url = properties.getUrl();
    if (url.contains("hsqldb")) {
      this.queryBuilder = new HsqlQueryBuilder();
    } else if (url.contains("postgresql")) {
      this.queryBuilder = new PostgresQueryBuilder();
    } else {
      throw new IllegalArgumentException(
          String.format("%s is not supported (yet), implement QueryBuilder interface for this URL if needed", url));
    }
    this.meterRegistry = meterRegistry;
    this.timers =
        mapAllTicketTypes(definition -> new ImmutablePair<>(tableName(definition),
            Stream.of(ADD_ACTION, GET_ACTION, DELETE_ACTION)
                .map(action -> {
                  val timer = createTimer(meterRegistry,
                      "action", action,
                      "table", tableName(definition));
                  return new ImmutablePair<>(action, timer);
                }).collect(Collectors.toMap(p -> p.left, p -> p.right))
        )).collect(Collectors.toMap(p -> p.left, p -> p.right));

    this.kryoPool = kryoPool;

    ticketCatalog.findAll().forEach(definition -> {
      val tableName = tableName(definition);
      Supplier<Number> supplier = () -> JdbcTicketRegistry.this.tableSize(tableName);
      Gauge
          .builder("cas.ticket.registry.table.record.count", supplier)
          .description("Record count per table")
          .tags("table", tableName)
          .register(JdbcTicketRegistry.this.meterRegistry);
    });
  }

  @SneakyThrows
  private static Field causeField() {
    val f = Throwable.class.getDeclaredField("cause");
    f.setAccessible(true);
    return f;
  }

  private static long expirationMoment(final Ticket ticket) {
    val expirationPolicy = ticket.getExpirationPolicy();
    val ttl = expirationPolicy.getTimeToLive();
    if (ttl >= MAX_TTL) {
      return System.currentTimeMillis() + MAX_TTL * 1000L;
    }
    return System.currentTimeMillis() + ttl * 1000L;
  }

  private <T> Stream<T> mapAllTicketTypes(Function<TicketDefinition, T> f) {
    return ticketCatalog
        .findAll()
        .stream()
        .map(f);
  }

  private JdbcTicketHolder toJdbcTicketHolder(Ticket ticket) {
    val encodedTicketId = encodeTicketId(ticket.getId());

    final byte[] encrypted = (byte[]) cipherExecutor.encode(serializeTicket(ticket));

    TicketGrantingTicket tgt;
    if (TicketGrantingTicket.class.isAssignableFrom(ticket.getClass())) {
      tgt = (TicketGrantingTicket) ticket;
    } else {
      tgt = ticket.getTicketGrantingTicket();
    }

    String tgtForeignKey = null;
    String principalId = null;
    if (tgt != null) {
      tgtForeignKey = digestValue(tgt.getId());
      val auth = tgt.getAuthentication();
      if (auth != null && auth.getPrincipal() != null) {
        principalId = digestValue(auth.getPrincipal().getId());
      }
    }
    return new JdbcTicketHolder(encodedTicketId, encrypted, tgtForeignKey, principalId);

  }

  @SneakyThrows
  private byte[] serializeTicket(Ticket ticket) {
    try (val kryo = this.kryoPool.borrow();
        val byteStream = new ByteArrayOutputStream();
        val output = new Output(byteStream)) {

      // this hack is needed to avoid recursive self-dependency
      // that causes stack overflow on serialisation
      // (as FoundAction is a subclass of Throwable (!!!))
      if (ticket.getClass().isAssignableFrom(TransientSessionTicketImpl.class)) {
        val pac4jRequestedUrl = ((TransientSessionTicketImpl) ticket).getProperties().get("pac4jRequestedUrl");
        if (pac4jRequestedUrl != null && HttpAction.class.isAssignableFrom(pac4jRequestedUrl.getClass())) {
          causeField.set(pac4jRequestedUrl, null);
        }
      }

      // we are breaking the class contract to pass a ticket with a null TGT field to Kryo.
      final boolean doHack = !(ticket instanceof TransientSessionTicket);
      if (doHack) {
        Field tgtField;
        if (ticket instanceof OAuth20AccessToken || ticket instanceof OAuth20RefreshToken) {
          tgtField = ticket.getClass().getSuperclass().getDeclaredField(TICKET_GRANTING_TICKET);
        } else {
          tgtField = ticket.getClass().getDeclaredField(TICKET_GRANTING_TICKET);
        }
        tgtField.setAccessible(true);
        val ref = ticket.getTicketGrantingTicket();
        tgtField.set(ticket, null);

        kryo.writeClassAndObject(output, ticket);
        output.flush();
        // The same ticket might be concurrently used somewhere else before the value is restored causing unexpected NPEs.
        tgtField.set(ticket, ref);
      } else {
        kryo.writeClassAndObject(output, ticket);
        output.flush();
      }

      return byteStream.toByteArray();
    }
  }

  private Timer createTimer(MeterRegistry meterRegistry, String... tags) {
    return Timer.builder("cas.ticket.registry")
        .tags(tags)
        .publishPercentiles(0.5, 0.95, 0.99, 0.999)
        .publishPercentileHistogram()
        .sla(Duration.ofMillis(10), Duration.ofSeconds(10))
        .minimumExpectedValue(Duration.ofMillis(10))
        .maximumExpectedValue(Duration.ofSeconds(10))
        .register(meterRegistry);
  }

  private String tableName(TicketDefinition definition) {
    if (StringUtils.isNotBlank(definition.getProperties().getStorageName())) {
      return definition.getProperties().getStorageName();
    } else {
      return definition
          .getImplementationClass()
          .getSimpleName()
          .toLowerCase()
          .replace("impl", "");
    }
  }

  @SneakyThrows
  void initDatastore() {
    LOGGER.info("Initialising JDBC datastore " + properties.getUrl());
    ticketCatalog.findAll().forEach(
        definition -> Arrays.stream(queryBuilder
            .createTableAndIndices(tableName(definition)))
            .forEach(sql -> {
              LOGGER.debug(definition.getPrefix() + ": " + sql);
              transactionTemplate.execute(status -> {
                jdbcTemplate.execute(sql);
                return null;
              });
            })
    );
  }

  @Override
  @NewSpan
  public Ticket getTicket(
      @SpanTag(key = "ticketType", resolver = TagValueResolver.class) final String ticketId,
      final Predicate<Ticket> predicate) {
    if (ticketId == null) {
      return null;
    }

    val definition = ticketCatalog.find(ticketId);
    if (definition == null) {
      LOGGER.info("Ticket definition [{}] could not be found in the ticket catalog", ticketId);
      return null;
    }

    return timers.get(tableName(definition)).get(GET_ACTION).record(() -> {
      val encodedTicketId = encodeTicketId(ticketId);
      LOGGER.debug("GET ticket {}:{}", definition.getPrefix(), ticketId);
      if (StringUtils.isBlank(encodedTicketId)) {
        LOGGER.debug("Ticket id [{}] could not be found", ticketId);
        return null;
      }

      val holder = findTicketBy(definition, encodedTicketId);
      if (holder.isEmpty()) {
        LOGGER.debug("Ticket [{}] could not be found in JDBC datastore", encodedTicketId);
        return null;
      }

      return holder.filter(predicate).orElse(null);
    });
  }

  @Override
  @NewSpan
  public void addTicket(@SpanTag(key = "ticketType", resolver = TagValueResolver.class) final Ticket ticket) {
    try {
      addTicket(ticket, true);
    } catch (final Exception e) {
      LOGGER.error("Failed adding {}: {}", ticket, e);
    }
  }

  @Override
  @NewSpan
  public Ticket updateTicket(@SpanTag(key = "ticketType", resolver = TagValueResolver.class) final Ticket ticket) {
    addTicket(ticket, false);
    return ticket;
  }

  @Override
  public Collection<Ticket> getTickets() {
    LOGGER.info("Getting tickets from JDBC is not implemented");
    return new ArrayList<>();
  }

  @Override
  @NewSpan
  public boolean deleteSingleTicket(
      @SpanTag(key = "ticketType", resolver = TagValueResolver.class) final String ticketIdToDelete) {
    val definition = this.ticketCatalog.find(ticketIdToDelete);
    if (definition == null) {
      LOGGER.info("Ticket definition [{}] could not be found in the ticket catalog", ticketIdToDelete);
      return false;
    }

    return timers.get(tableName(definition)).get(DELETE_ACTION).record(() -> {
      val ticketId = encodeTicketId(ticketIdToDelete);
      LOGGER.debug("DELETE ticket {}", ticketId);
      try {
        return transactionTemplate.execute(
            status -> jdbcTemplate.execute(queryBuilder.deleteQuery(tableName(definition)),
                (PreparedStatementCallback<Boolean>) ps -> {
                  ps.setString(1, ticketId);
                  ps.execute();
                  return ps.getUpdateCount() == 1;
                }));
      } catch (final Exception e) {
        LOGGER.error("Failed deleting [{}]: [{}]", ticketId, e);
        return false;
      }
    });
  }

  @Override
  public long deleteAll() {
    LOGGER.info("Deleting all tickets from JDBC is not implemented");
    return 0;
  }

  private Optional<Ticket> findTicketBy(final TicketDefinition definition, final String ticketId) {
    try {
      LOGGER.debug("Looking up ticket by definition={}, id={} ", definition.getPrefix(), ticketId);
      return jdbcTemplate.queryForObject(
          queryBuilder.selectQuery(tableName(definition)), new Object[] {ticketId}, (rs, rowNum) -> {
            val encData = rs.getBytes("data");
            val encTgt = rs.getBytes("tgtdata");
            val decodedData = (byte[]) cipherExecutor.decode(encData);
            try (val kryo = this.kryoPool.borrow(); val ticketInput = new Input(decodedData)) {
              val value = kryo.readClassAndObject(ticketInput);

              if (encTgt != null) {
                val decodedTgt = (byte[]) cipherExecutor.decode(encTgt);
                try (val tgtInput = new Input(decodedTgt)) {
                  val tgt = (TicketGrantingTicket) kryo.readClassAndObject(tgtInput);
                  final Field tgtField;
                  if (value instanceof OAuth20AccessToken || value instanceof OAuth20RefreshToken) {
                    tgtField = value.getClass().getSuperclass().getDeclaredField(TICKET_GRANTING_TICKET);
                  } else {
                    tgtField = value.getClass().getDeclaredField(TICKET_GRANTING_TICKET);
                  }
                  tgtField.setAccessible(true);
                  tgtField.set(value, tgt);
                }
              }

              return Optional.of((Ticket) value);

            } catch (IllegalAccessException | NoSuchFieldException e) {
              LOGGER.error("Unexpected error while patching up ticket instance for Ticket id={}. msg={}", ticketId,
                  e.getMessage());
              return Optional.empty();
            }
          });
    } catch (final IncorrectResultSizeDataAccessException exception) {
      LOGGER.debug("Could not find ticket for id={}. msg={}", ticketId, exception.getMessage());
      return Optional.empty();
    }
  }

  private void addTicket(final Ticket ticket, final boolean inserting) {
    val definition = this.ticketCatalog.find(ticket);
    if (definition == null) {
      LOGGER.info("Could not locate ticket definition in the catalog for ticket [{}]", ticket.getId());
      return;
    }
    timers.get(tableName(definition)).get(ADD_ACTION).record(() -> {

      // PE-873 do not store descendant tickets
      if (ticket instanceof TicketGrantingTicket) {
        ((TicketGrantingTicket) ticket).getDescendantTickets().clear();
      }

      val ticketToStore = toJdbcTicketHolder(ticket);

      LOGGER.debug("ADD ticket {}:{}", definition.getPrefix(), ticket.getId());
      val expiration = expirationMoment(ticket);

      transactionTemplate.execute(status -> {
        if (inserting) {

          final TicketGrantingTicket ticketGrantingTicket = ticket.getTicketGrantingTicket();
          if (ticketGrantingTicket != null) {
            val tgtTableName = tableName(ticketCatalog.find(ticketGrantingTicket));
            String sql = String.format("SELECT count(*) FROM %s WHERE id=?", tgtTableName);
            final String id = digestValue(ticketGrantingTicket.getId());
            int count = jdbcTemplate.queryForObject(sql, new Object[] {id}, Integer.class);
            if (count == 0) {
              JdbcTicketHolder tgtTicket = toJdbcTicketHolder(ticketGrantingTicket);
              jdbcTemplate.execute(queryBuilder.insertQuery(tgtTableName),
                  (PreparedStatementCallback<Void>) ps -> {
                    ps.setString(1, tgtTicket.getId());
                    ps.setBytes(2, tgtTicket.getData());
                    ps.setString(3, null);
                    ps.setLong(4, expiration);
                    ps.setString(5, tgtTicket.getPrincipalId());
                    ps.execute();
                    return null;
                  });
            }
          }

          val tgtEncodedId = ticketGrantingTicket != null ? encodeTicketId(ticketGrantingTicket.getId()) : null;
          jdbcTemplate.execute(queryBuilder.insertQuery(tableName(definition)),
              (PreparedStatementCallback<Void>) ps -> {
                ps.setString(1, ticketToStore.getId());
                ps.setBytes(2, ticketToStore.getData());
                ps.setString(3, tgtEncodedId);
                ps.setLong(4, expiration);
                ps.setString(5, ticketToStore.getPrincipalId());
                ps.execute();
                return null;
              });
        } else {
          jdbcTemplate.execute(queryBuilder.updateQuery(tableName(definition)),
              (PreparedStatementCallback<Boolean>) ps -> {
                ps.setBytes(1, ticketToStore.getData());
                ps.setLong(2, expiration);
                ps.setString(3, ticketToStore.getPrincipalId());
                ps.setString(4, ticketToStore.getId());
                return ps.execute();
              }
          );
        }
        return null;
      });
    });
  }

  public Map<String, Long> deleteByPrincipalId(final String principalId) {
    LOGGER.info("Requested deletion of tickets for principalId={}", principalId);
    val principalIdDigest = digestValue(principalId);

    return transactionTemplate.execute(status -> {
      Map<String, Long> stats = new TreeMap<>();
      stats.put(principalId, (long) jdbcTemplate.update(
          queryBuilder.deleteByPrincipalIdQuery(),
          ps -> {
            ps.setString(1, principalIdDigest);
            ps.execute();
          }));
      return stats;
    });

  }

  public Map<String, Long> selectByPrincipalId(final String principalId) {
    val principalIdDigest = digestValue(principalId);
    LOGGER.debug("Requested selection of tickets for principalId={}", principalId);
    return transactionTemplate.execute(status -> {
      Map<String, Long> stats = new TreeMap<>();
      stats.put(principalId, jdbcTemplate.query(
          queryBuilder.selectByPrincipalQuery(),
          ps -> {
            ps.setString(1, principalIdDigest);
            ps.execute();
          },
          mapper -> mapper.next() ? mapper.getLong(1) : 0
      ));
      return stats;
    });

  }

  public Map<String, Long> cleanExpiredTickets() {
    LOGGER.info("Started expired tickets cleanup");
    Map<String, Long> result = transactionTemplate.execute(status ->
        mapAllTicketTypes(ticketDefinition -> {
          val tableName = tableName(ticketDefinition);
          LOGGER.debug("Cleaning " + tableName);
          return new CleanupResult(tableName,
              (long) jdbcTemplate.update(queryBuilder.deleteExpiredTicketsQuery(tableName)));
        }).collect(Collectors.toMap(CleanupResult::getTableName, CleanupResult::getRecords)));

    LOGGER.info("Completed cleaning of expired tickets. Result: {}", result);
    return result;
  }

  private Long tableSize(String tableName) {
    return transactionTemplate.execute(status ->
        jdbcTemplate.query(queryBuilder.countRecords(tableName),
            mapper -> mapper.next() ? mapper.getLong(1) : 0
        )
    );
  }

  Map<String, JdbcTicketRegistryEndpoint.JdbcTicketRegistryTableStats> getStats() {
    Map<String, JdbcTicketRegistryEndpoint.JdbcTicketRegistryTableStats> stats = new TreeMap<>();
    for (val ticketDefinition : ticketCatalog.findAll()) {
      val tableName = tableName(ticketDefinition);
      val tableSize = tableSize(tableName);
      stats.put(tableName, new JdbcTicketRegistryEndpoint.JdbcTicketRegistryTableStats(tableSize));
    }
    return stats;
  }

  // use a more generic name when encoding fields that are not actually the ticketId
  private String digestValue(final String value) {
    return encodeTicketId(value);
  }

  @AllArgsConstructor
  @Getter
  private static class CleanupResult {

    private String tableName;
    private Long records;
  }
}

