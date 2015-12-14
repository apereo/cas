package org.jasig.cas.util.jpa;

import java.time.ZonedDateTime;
import java.util.Date;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.jasig.cas.util.DateTimeUtils;

/**
 * Converts ZonedDateTime &lt;-&gt; Date.
 * @author tduehr
 * @since 4.3.0
 */
@Converter(autoApply = true)
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Date> {

    @Override
    public final Date convertToDatabaseColumn(final ZonedDateTime zonedDateTime){
        return DateTimeUtils.dateOf(zonedDateTime);
    }


    @Override
    public ZonedDateTime convertToEntityAttribute(final Date date) {
        return DateTimeUtils.zonedDateTimeOf(date);
    }
}
