package org.apereo.cas.support.events.dao;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.influxdb.InfluxDbConnectionFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.QueryResult;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PreDestroy;
import java.lang.reflect.Modifier;
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
@RequiredArgsConstructor
public class InfluxDbCasEventRepository extends AbstractCasEventRepository {
    private static final String MEASUREMENT = "InfluxDbCasEventRepositoryCasEvents";

    private final InfluxDbConnectionFactory influxDbConnectionFactory;

    @Override
    public void save(final CasEvent event) {
        final Point.Builder builder = Point.measurement(MEASUREMENT);
        ReflectionUtils.doWithFields(CasEvent.class, field -> {
            if (!Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                if (field.getType().equals(Map.class)) {
                    builder.fields((Map) field.get(event));
                } else {
                    builder.field(field.getName(), field.get(event));
                }
            }
        });


        final Point point = builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).build();
        influxDbConnectionFactory.writeBatch(point);
    }

    @Override
    public Collection<? extends CasEvent> load() {
        final List<CasEvent> events = new ArrayList<>();
        final QueryResult results = influxDbConnectionFactory.query(MEASUREMENT);
        results.getResults()
            .stream()
            .filter(r -> r.getSeries() != null)
            .map(QueryResult.Result::getSeries)
            .forEach(r -> r.forEach(s -> {
                try {
                    final Iterator<List<Object>> it = s.getValues().iterator();
                    while (it.hasNext()) {
                        final CasEvent event = new CasEvent();
                        final List<Object> row = it.next();
                        for (int i = 0; i < s.getColumns().size(); i++) {
                            final String colName = s.getColumns().get(i);
                            final String value = row.get(i) != null ? row.get(i).toString() : StringUtils.EMPTY;

                            LOGGER.debug("Handling event column name [{}] with value [{}]", colName, value);

                            if (StringUtils.isNotBlank(value)) {
                                switch (colName) {
                                    case "time":
                                        break;
                                    case "id":
                                        event.putId(value);
                                        break;
                                    case "type":
                                        event.setType(value);
                                        break;
                                    case "principalId":
                                        event.setPrincipalId(value);
                                        break;
                                    case "creationTime":
                                        event.setCreationTime(value);
                                        break;
                                    default:
                                        event.put(colName, value);
                                }
                            }
                        }

                        if (StringUtils.isNotBlank(event.getType()) && StringUtils.isNotBlank(event.getPrincipalId()) && StringUtils.isNotBlank(event.getId())) {
                            events.add(event);
                        }
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
        this.influxDbConnectionFactory.close();
    }
}
