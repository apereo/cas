package org.apereo.cas;

import org.apereo.cas.persondir.ActiveDirectoryLdapEntryHandler;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ActiveDirectoryLdapEntryHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Ldap")
class ActiveDirectoryLdapEntryHandlerTests {
    @Test
    void verifyAccountLocked() throws Exception {
        val handler = new ActiveDirectoryLdapEntryHandler();
        val entry = LdapEntry.builder()
            .attributes(LdapAttribute.builder().name("userAccountControl")
                .values(String.valueOf(ActiveDirectoryLdapEntryHandler.LOCKOUT))
                .build())
            .build();
        assertNull(handler.apply(entry));
    }

    @Test
    void verifyAccountExpired() throws Exception {
        val handler = new ActiveDirectoryLdapEntryHandler();
        val entry = LdapEntry.builder()
            .attributes(LdapAttribute.builder().name("userAccountControl")
                .values(String.valueOf(ActiveDirectoryLdapEntryHandler.PASSWORD_EXPIRED))
                .build())
            .build();
        assertNull(handler.apply(entry));
    }

    @Test
    void verifyAccountExpiredByDate() throws Exception {
        val handler = new ActiveDirectoryLdapEntryHandler();
        val expiration = new Date().getTime();
        val entry = LdapEntry.builder()
            .attributes(LdapAttribute.builder().name("accountExpires")
                .values(String.valueOf(expiration))
                .build())
            .build();
        assertNull(handler.apply(entry));
    }


    @Test
    void verifyAccountLogonHours() throws Exception {
        val handler = new ActiveDirectoryLdapEntryHandler();
        val logonHours = new byte[21];
        for (var day = 1; day <= 5; day++) {
            for (var hour = 9; hour < 17; hour++) {
                val bitIndex = day * 24 + hour;
                val byteIndex = bitIndex / 8;
                val bitPosition = 7 - (bitIndex % 8);
                logonHours[byteIndex] |= (byte) (1 << bitPosition);
            }
        }
        
        val entry = LdapEntry.builder()
            .attributes(LdapAttribute.builder().name("logonHours")
                .values(logonHours)
                .build())
            .build();
        assertNotNull(handler.apply(entry));
    }
    
    @Test
    void verifyAccountDisabled() throws Exception {
        val handler = new ActiveDirectoryLdapEntryHandler();
        val entry = LdapEntry.builder()
            .attributes(LdapAttribute.builder().name("userAccountControl")
                .values(String.valueOf(ActiveDirectoryLdapEntryHandler.ACCOUNT_DISABLED))
                .build())
            .build();
        assertNull(handler.apply(entry));
    }
}
