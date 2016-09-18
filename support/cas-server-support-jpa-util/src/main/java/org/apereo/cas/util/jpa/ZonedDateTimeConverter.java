package org.apereo.cas.util.jpa;

import java.time.ZonedDateTime;
import java.util.Date;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apereo.cas.util.DateTimeUtils;

/**
 * Converts ZonedDateTime &lt;-&gt; Date.
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
@Converter(autoApply = true)
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Date> {

    @Override
    public Date convertToDatabaseColumn(final ZonedDateTime zonedDateTime){
        return DateTimeUtils.dateOf(zonedDateTime);
    }


    @Override
    public ZonedDateTime convertToEntityAttribute(final Date date) {
        return DateTimeUtils.zonedDateTimeOf(date);
    }
}
