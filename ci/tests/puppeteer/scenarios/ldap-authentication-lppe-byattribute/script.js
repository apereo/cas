
const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const path = require("path");

const LDAP_ADMIN = ["-x", "-H", "ldapi:///", "-D", "cn=admin,dc=example,dc=org", "-w", "P@ssw0rd"];

const PPOLICY_ENTRIES = [
    "uid=ppexpiresoon,ou=People,dc=example,dc=org",
    "uid=ppexpiring,ou=People,dc=example,dc=org",
    "uid=ppexpiringquiet,ou=People,dc=example,dc=org",
    "uid=ppgrace,ou=People,dc=example,dc=org",
    "uid=pplocked,ou=People,dc=example,dc=org",
    "cn=expiresoon,ou=pwpolicies,dc=example,dc=org",
    "cn=expiring,ou=pwpolicies,dc=example,dc=org",
    "cn=graceperiod,ou=pwpolicies,dc=example,dc=org",
    "cn=lockout,ou=pwpolicies,dc=example,dc=org"
];

/*
 * Expiration warnings expected per variation, keyed by user. The ppolicy overlay reports the time left before
 * the password expires: about 2 days for ppexpiresoon (shown as 1 day) and 5 days for the other two (shown as 4).
 * With warning-days=3 only ppexpiresoon is warned; warn-all warns every user the overlay reports on, unless the
 * warning attribute (description=pwdwarn) does not match, which is the case for ppexpiringquiet.
 */
const EXPIRATION_WARNINGS = {
    "warning-days": {
        ppexpiresoon: "Your password expires in 1 day(s).",
        ppexpiring: undefined,
        ppexpiringquiet: undefined
    },
    "warn-all-by-attribute": {
        ppexpiresoon: "Your password expires in 1 day(s).",
        ppexpiring: "Your password expires in 4 day(s).",
        ppexpiringquiet: undefined
    }
};

async function resetPasswordPolicyEntries() {
    await cas.dockerExec("openldap-server", ["sh", "-c",
        `ldapdelete -c ${LDAP_ADMIN.map((arg) => `'${arg}'`).join(" ")} ${PPOLICY_ENTRIES.map((dn) => `'${dn}'`).join(" ")} || true`]);
    const ldif = fs.readFileSync(path.join(__dirname, "ppolicy-users.ldif"));
    await cas.dockerExec("openldap-server", ["ldapadd", ...LDAP_ADMIN], ldif);
}

async function loginAs(page, user, password = "Mellon") {
    await cas.gotoLogin(page);
    await cas.loginWith(page, user, password);
    await cas.sleep(2000);
}

async function assertLoggedInWithWarning(page, warning) {
    await cas.assertTextContent(page, "#content h1", "Authentication Succeeded with Warnings");
    const messages = await cas.innerText(page, "#content ul");
    assert(warning instanceof RegExp ? warning.test(messages) : messages.includes(warning),
        `Warnings [${messages}] do not match [${warning}]`);
    await cas.submitForm(page, "#form");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
}

async function assertLoggedInWithoutWarning(page) {
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
}

(async () => {
    const variation = process.env.SCENARIO_VARIATION;
    const warnings = EXPIRATION_WARNINGS[variation];
    assert(warnings !== undefined, `Unknown variation ${variation}`);
    await cas.log(`Running with password policy variation ${variation}`);
    await resetPasswordPolicyEntries();

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#content h2", "This account has been locked.");
    await cas.assertCookie(page, false);
    await cas.gotoLogout(page);

    for (const [user, warning] of Object.entries(warnings)) {
        await cas.log(`Logging in as ${user}, expecting warning: ${warning}`);
        await loginAs(page, user);
        if (warning === undefined) {
            await assertLoggedInWithoutWarning(page);
        } else {
            await assertLoggedInWithWarning(page, warning);
        }
    }

    await cas.log("Logging in as ppgrace, whose password expired after one second and who has grace logins left");
    await loginAs(page, "ppgrace");
    await assertLoggedInWithWarning(page, /You have \d+ login\(s\) remaining before you MUST change your password\./);

    await cas.log("Locking pplocked out after three failed attempts (pwdMaxFailure=3)");
    for (let attempt = 1; attempt <= 3; attempt++) {
        await loginAs(page, "pplocked", "BadPassword");
        await cas.assertInnerTextStartsWith(page, "#loginErrorsPanel p", "Authentication attempt has failed");
        await cas.assertCookie(page, false);
    }
    await loginAs(page, "pplocked");
    await cas.assertInnerText(page, "#content h2", "This account has been locked.");
    await cas.assertCookie(page, false);

    await cas.closeBrowser(browser);
})();
