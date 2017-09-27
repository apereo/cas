package org.apereo.cas.support.events.dao;

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
public class InfluxDbCasEventRepository extends AbstractCasEventRepository {
    private static final String MEASUREMENT = "InfluxDbCasEventRepositoryCasEvents";

    private final InfluxDbConnectionFactory influxDbConnectionFactory;

    public InfluxDbCasEventRepository(final InfluxDbConnectionFactory influxDbConnectionFactory) {
        this.influxDbConnectionFactory = influxDbConnectionFactory;
    }

    @Override
    public void save(final CasEvent event) {
        final Point.Builder builder = Point.measurement(MEASUREMENT);
        ReflectionUtils.doWithFields(CasEvent.class, field -> {
            field.setAccessible(true);
            if (field.getType().equals(Map.class)) {
                builder.fields((Map) field.get(event));
            } else {
                builder.field(field.getName(), field.get(event));
            }
        });
        final Point point = builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).build();
        influxDbConnectionFactory.write(point);
    }

    @Override
    public Collection<? extends CasEvent> load() {
        final List<CasEvent> events = new ArrayList<>();
        final QueryResult results = influxDbConnectionFactory.query(MEASUREMENT);
        results.getResults().stream().forEach(r -> r.getSeries().forEach(s -> {
            try {
                final Iterator<List<Object>> it = s.getValues().iterator();
                while (it.hasNext()) {
                    final CasEvent event = new CasEvent();
                    final List<Object> row = it.next();
                    for (int i = 0; i < s.getColumns().size(); i++) {
                        final String colName = s.getColumns().get(i);
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
    public void destroy() {
        try {
            LOGGER.debug("Shutting down Couchbase");
            this.influxDbConnectionFactory.close();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
