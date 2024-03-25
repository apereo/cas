package org.apereo.cas.persondir;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.ldaptive.LdapEntry;
import org.ldaptive.handler.AbstractEntryHandler;
import org.ldaptive.handler.LdapEntryHandler;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is {@link ActiveDirectoryLdapEntryHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EqualsAndHashCode(callSuper = false)
@ToString
public class ActiveDirectoryLdapEntryHandler extends AbstractEntryHandler<LdapEntry> implements LdapEntryHandler {
    /**
     * The user account is disabled.
     */
    public static final int ACCOUNT_DISABLED = 0x00000002;

    /**
     * The account is currently locked out.
     */
    public static final int LOCKOUT = 0x00000010;

    /**
     * Password expired flag.
     */
    public static final int PASSWORD_EXPIRED = 0x00800000;

    private static final int AD_STARTING_YEAR = 1601;
    private static final int DATE_TO_MILISECONDS = 10000;

    //CHECKSTYLE:OFF
    private static String decodeLogonBits(final byte b) {
        val sb = new StringBuilder();
        sb.append((b & 0x01) > 0 ? "1" : "0");
        sb.append((b & 0x02) > 0 ? "1" : "0");
        sb.append((b & 0x04) > 0 ? "1" : "0");
        sb.append((b & 0x08) > 0 ? "1" : "0");
        sb.append((b & 0x10) > 0 ? "1" : "0");
        sb.append((b & 0x20) > 0 ? "1" : "0");
        sb.append((b & 0x40) > 0 ? "1" : "0");
        sb.append((b & 0x80) > 0 ? "1" : "0");
        return sb.toString();
    }
    //CHECKSTYLE:ON

    @Override
    public LdapEntry apply(final LdapEntry ldapEntry) {
        val attr = ldapEntry.getAttribute("userAccountControl");
        if (attr != null) {
            val uac = Integer.parseInt(attr.getStringValue());
            if ((uac & LOCKOUT) == LOCKOUT) {
                logger.warn("Account is disabled with UAC [{}] for entry [{}]", uac, ldapEntry);
                return null;
            }
            if ((uac & ACCOUNT_DISABLED) == ACCOUNT_DISABLED) {
                logger.warn("Account is disabled with UAC [{}] for entry [{}]", uac, ldapEntry);
                return null;
            }
            if ((uac & PASSWORD_EXPIRED) == PASSWORD_EXPIRED) {
                logger.warn("Account has expired");
                return null;
            }
        }

        val accountExpires = ldapEntry.getAttribute("accountExpires");
        if (accountExpires != null) {
            val adDate = Long.parseLong(accountExpires.getStringValue());
            logger.debug("Current active directory account expiration date [{}]", adDate);
            if (adDate > 0) {
                val cal = new GregorianCalendar(TimeZone.getDefault());
                cal.set(AD_STARTING_YEAR, Calendar.JANUARY, 1, 0, 0);

                val converted = adDate / DATE_TO_MILISECONDS;
                val timeStamp = Long.valueOf(converted + cal.getTime().getTime());
                val date = new Date(timeStamp);
                val accountExpiresDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                val now = LocalDateTime.now(ZoneId.systemDefault());
                logger.debug("Now: [{}], account expires at [{}]", now, accountExpiresDate);
                if (accountExpiresDate.isBefore(now)) {
                    logger.warn("Account has expired with date [{}]", accountExpiresDate);
                    return null;
                }
            }
        }

        if (!isValidLogonHour(ldapEntry)) {
            logger.warn("Logon Hours are invalid and no attributes will be used");
            return null;
        }
        return ldapEntry;
    }

    protected boolean isValidLogonHour(final LdapEntry attr) {
        if (attr.getAttribute("logonHours") != null) {
            val raw = attr.getAttribute("logonHours").getBinaryValue();
            val days = DayOfWeek.values();
            //CHECKSTYLE:OFF
            val ret = IntStream.range(0, days.length)
                .mapToObj(day -> day == 6 ? new byte[]{raw[19], raw[20], raw[0]} : new byte[]{raw[day * 3], raw[day * 3 + 1], raw[day * 3 + 2]})
                .map(vBits -> IntStream.range(0, 3).mapToObj(b -> decodeLogonBits(vBits[b])).collect(Collectors.joining()))
                .collect(Collectors.toCollection(ArrayList::new));
            //CHECKSTYLE:ON

            val result = new String[ret.size()];
            ret.toArray(result);

            val currentDay = LocalDate.now(ZoneId.systemDefault()).getDayOfWeek();
            var currentHour = LocalDateTime.now(ZoneId.systemDefault()).getHour() - 1;
            if (currentHour < 0) {
                currentHour = 0;
            }
            logger.debug("Current day [{}], current hour [{}]", currentDay, currentHour);
            for (var day = 0; day < days.length; day++) {
                if (days[day] == currentDay) {
                    val validHours = result[day];
                    logger.debug("Valid hours are [{}]", validHours);
                    val hourEnabled = String.valueOf(validHours.charAt(currentHour));
                    logger.debug("Hour enabled at [{}] is [{}]", currentHour, hourEnabled);
                    if (!"1".equalsIgnoreCase(hourEnabled)) {
                        logger.warn("Invalid login hour");
                        return false;
                    }
                }
            }

            return true;
        }
        return true;
    }
}
