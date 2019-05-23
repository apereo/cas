package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import com.couchbase.client.java.document.StringDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.View;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;

import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.couchbase.client.java.query.Select.*;
import static com.couchbase.client.java.query.dsl.Expression.*;

/**
 * This is {@link CouchbaseAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Setter
@RequiredArgsConstructor
public class CouchbaseAuditTrailManager extends AbstractAuditTrailManager {
    /**
     * The utils document.
     */
    public static final String UTIL_DOCUMENT = "utils";

    /**
     * All records view.
     */
    public static final View ALL_RECORDS_VIEW = DefaultView.create(
        "all_records",
        "function(d,m) {if (!isNaN(m.id)) {emit(m.id);}}");

    /**
     * All views.
     */
    public static final Collection<View> ALL_VIEWS = CollectionUtils.wrap(ALL_RECORDS_VIEW);

    private final CouchbaseClientFactory couchbase;
    private final StringSerializer<AuditActionContext> serializer;

    public CouchbaseAuditTrailManager(final CouchbaseClientFactory couchbase, final StringSerializer<AuditActionContext> serializer,
                                      final boolean asynchronous) {
        this(couchbase, serializer);
        this.asynchronous = asynchronous;
    }

    @Override
    public void removeAll() {
        
    }

    @SneakyThrows
    @Override
    protected void saveAuditRecord(final AuditActionContext audit) {
        try (val stringWriter = new StringWriter()) {
            this.serializer.to(stringWriter, audit);
            val id = UUID.randomUUID().toString();
            val document = StringDocument.create(id, 0, stringWriter.toString());
            this.couchbase.getBucket().upsert(document);
        }
    }

    @Override
    public Set<? extends AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        val couchbaseBucket = this.couchbase.getBucket();
        val name = couchbaseBucket.name();
        val statement = select("*").from(i(name)).where(x("whenActionWasPerformed").gte(x("$whenActionWasPerformed")));
        val placeholderValues = JsonObject.create().put("whenActionWasPerformed", DateTimeUtils.dateOf(localDate).getTime());
        val q = N1qlQuery.parameterized(statement, placeholderValues);
        val result = couchbaseBucket.query(q);
        return result.allRows()
            .stream()
            .map(row -> {
                val json = row.value().toString();
                val bucket = JsonObject.fromJson(json).getObject(name);
                return new AuditActionContext(bucket.getString("principal"),
                    bucket.getString("resourceOperatedUpon"),
                    bucket.getString("actionPerformed"),
                    bucket.getString("applicationCode"),
                    new Date(bucket.getArray("whenActionWasPerformed").getLong(1)),
                    bucket.getString("clientIpAddress"),
                    bucket.getString("serverIpAddress"));
            })
            .collect(Collectors.toSet());
    }
}
