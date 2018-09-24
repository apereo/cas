package org.apereo.cas.pm;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link AbstractPasswordManagementTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class AbstractPasswordManagementTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    public static final String CASUSER = "casuser";

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("passwordChangeService")
    protected PasswordManagementService passwordChangeService;

    @Autowired
    @Qualifier("passwordValidationService")
    protected PasswordValidationService passwordValidationService;

    @Autowired
    @Qualifier("passwordManagementCipherExecutor")
    protected CipherExecutor passwordManagementCipherExecutor;

    @Test
    public void verifyTokenCreationAndParsing() {
        val token = passwordChangeService.createToken(CASUSER);
        assertNotNull(token);
        val result = passwordChangeService.parseToken(token);
        assertEquals(CASUSER, result);
    }

    @Test
    public void verifyUserEmailCanBeFound() {
        assertEquals("casuser@example.org", passwordChangeService.findEmail(CASUSER));
    }

    @Test
    public void verifyUserEmailCanNotBeFound() {
        assertNull("Email should be null.", passwordChangeService.findEmail("casusernotfound"));
    }

    @Test
    public void verifyUserQuestionsCanBeFound() {
        val questions = passwordChangeService.getSecurityQuestions(CASUSER);
        assertEquals(2, questions.size());
        assertTrue("Question 1 not found.", questions.containsKey("question1"));
        assertTrue("Question 2 not found.", questions.containsKey("question2"));
    }

    @Test
    public void verifyUserPasswordChange() {
        val bean = new PasswordChangeBean();
        bean.setConfirmedPassword("newPassword");
        bean.setPassword("newPassword");
        assertTrue("Password did not change", passwordChangeService.change(new UsernamePasswordCredential(CASUSER, "password"), bean));
    }

    @Test
    public void verifyPasswordValidationService() {
        val bean = new PasswordChangeBean();
        bean.setConfirmedPassword("Test@1234");
        bean.setPassword("Test@1234");
        assertTrue("Password should be valid", passwordValidationService.isValid(new UsernamePasswordCredential(CASUSER, "password"), bean));
        assertFalse("Password should not be valid", passwordValidationService.isValid(new UsernamePasswordCredential(CASUSER, "Test@1234"), bean));
    }
}
