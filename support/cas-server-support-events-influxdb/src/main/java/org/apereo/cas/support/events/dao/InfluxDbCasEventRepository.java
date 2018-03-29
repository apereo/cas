package org.apereo.cas.support.events.dao;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.influxdb.InfluxDbConnectionFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.QueryResult;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link InfluxDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class InfluxDbCasEventRepository extends AbstractCasEventRepository {
    private static final String MEASUREMENT = "InfluxDbCasEventRepositoryCasEvents";

    private final InfluxDbConnectionFactory influxDbConnectionFactory;

    @Override
    public void save(final CasEvent event) {
        final var builder = Point.measurement(MEASUREMENT);
        ReflectionUtils.doWithFields(CasEvent.class, field -> {
            field.setAccessible(true);
            if (field.getType().equals(Map.class)) {
                builder.fields((Map) field.get(event));
            } else {
                builder.field(field.getName(), field.get(event));
            }
        });
        final var point = builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).build();
        influxDbConnectionFactory.write(point);
    }

    @Override
    public Collection<? extends CasEvent> load() {
        final List<CasEvent> events = new ArrayList<>();
        final var results = influxDbConnectionFactory.query(MEASUREMENT);
        results.getResults().forEach(r -> r.getSeries().forEach(s -> {
            try {
                final var it = s.getValues().iterator();
                while (it.hasNext()) {
                    final var event = new CasEvent();
                    final var row = it.next();
                    for (var i = 0; i < s.getColumns().size(); i++) {
                        final var colName = s.getColumns().get(i);
                        switch (colName) {
                            case "time":
                                break;
                            case "id":
                                event.putId(row.get(i).toString());
                                break;
                            case "type":
                                event.setType(row.get(i).toString());
                                break;
                            case "principalId":
                                event.setPrincipalId(row.get(i).toString());
                                break;
                            case "creationTime":
                                event.setCreationTime(row.get(i).toString());
                                break;
                            default:
                                event.put(colName, row.get(i).toString());
                        }
                    }
                    events.add(event);
                }

            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }));
        return events;
    }

    /**
     * Stops the database client.
     */
    @PreDestroy
    @SneakyThrows
    public void destroy() {

        LOGGER.debug("Shutting down Couchbase");
        this.influxDbConnectionFactory.close();

    }
}
