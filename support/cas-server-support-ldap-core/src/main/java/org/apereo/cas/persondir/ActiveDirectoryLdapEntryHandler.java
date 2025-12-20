package org.apereo.cas.persondir;

import module java.base;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.ldaptive.LdapEntry;
import org.ldaptive.handler.LdapEntryHandler;

/**
 * This is {@link ActiveDirectoryLdapEntryHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
@EqualsAndHashCode(callSuper = false)
@ToString
public class ActiveDirectoryLdapEntryHandler implements LdapEntryHandler {
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
    private static final int DATE_TO_MILLISECONDS = 10000;

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
    @SuppressWarnings("NullAway")
    public @Nullable LdapEntry apply(final LdapEntry ldapEntry) {
        val attr = ldapEntry.getAttribute("userAccountControl");
        if (attr != null) {
            val uac = Integer.parseInt(attr.getStringValue());
            if ((uac & LOCKOUT) == LOCKOUT) {
                LOGGER.warn("Account is disabled with UAC [{}] for entry [{}]", uac, ldapEntry);
                return null;
            }
            if ((uac & ACCOUNT_DISABLED) == ACCOUNT_DISABLED) {
                LOGGER.warn("Account is disabled with UAC [{}] for entry [{}]", uac, ldapEntry);
                return null;
            }
            if ((uac & PASSWORD_EXPIRED) == PASSWORD_EXPIRED) {
                LOGGER.warn("Account has expired");
                return null;
            }
        }

        val accountExpires = ldapEntry.getAttribute("accountExpires");
        if (accountExpires != null) {
            val adDate = Long.parseLong(accountExpires.getStringValue());
            LOGGER.debug("Current active directory account expiration date [{}]", adDate);
            if (adDate > 0) {
                val cal = new GregorianCalendar(TimeZone.getDefault());
                cal.set(AD_STARTING_YEAR, Calendar.JANUARY, 1, 0, 0);

                val converted = adDate / DATE_TO_MILLISECONDS;
                val timeStamp = Long.valueOf(converted + cal.getTime().getTime());
                val date = new Date(timeStamp);
                val accountExpiresDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                val now = LocalDateTime.now(ZoneId.systemDefault());
                LOGGER.debug("Now: [{}], account expires at [{}]", now, accountExpiresDate);
                if (accountExpiresDate.isBefore(now)) {
                    LOGGER.warn("Account has expired with date [{}]", accountExpiresDate);
                    return null;
                }
            }
        }

        if (!isValidLogonHour(ldapEntry)) {
            LOGGER.warn("Logon Hours are invalid and no attributes will be used");
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
                .toList();
            //CHECKSTYLE:ON

            val result = new String[ret.size()];
            ret.toArray(result);

            val currentDay = LocalDate.now(ZoneId.systemDefault()).getDayOfWeek();
            var currentHour = LocalDateTime.now(ZoneId.systemDefault()).getHour() - 1;
            if (currentHour < 0) {
                currentHour = 0;
            }
            LOGGER.debug("Current day [{}], current hour [{}]", currentDay, currentHour);
            for (var day = 0; day < days.length; day++) {
                if (days[day] == currentDay) {
                    val validHours = result[day];
                    LOGGER.debug("Valid hours are [{}]", validHours);
                    val hourEnabled = String.valueOf(validHours.charAt(currentHour));
                    LOGGER.debug("Hour enabled at [{}] is [{}]", currentHour, hourEnabled);
                    if (!"1".equalsIgnoreCase(hourEnabled)) {
                        LOGGER.warn("Invalid login hour");
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
