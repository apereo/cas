const assert = require("assert");
const cas = require("../../cas.js");

const LOGIN_URL = "https://localhost:8443/cas/login";
const TOKEN_FAILURE = "The provided token could not be verified. It may have been invalidated, removed or expired.";

async function deleteTicketGrantingCookie(page) {
    await page.deleteCookie({name: "TGC", url: "https://localhost:8443/cas/"});
}

async function requestToken(page) {
    await cas.gotoLogin(page);
    await cas.type(page, "#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#login h3", "Provide Token");
}

async function submitToken(page, token) {
    await page.bringToFront();
    await cas.type(page, "#token", token);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);
}

async function assertTokenRefused(page) {
    await cas.assertInnerText(page, "#login h3", "Provide Token");
    await cas.assertInnerText(page, "#login .alert-danger p", TOKEN_FAILURE);
    await cas.assertCookie(page, false);
}

async function replayToken(execution, token) {
    const form = new URLSearchParams({
        execution: execution,
        _eventId_submit: "LOGIN",
        token: token
    });
    let cookies = [];
    const body = await cas.doRequest(LOGIN_URL, "POST", {"Content-Type": "application/x-www-form-urlencoded"}, 200, form.toString(),
        (res) => {
            cookies = res.headers["set-cookie"] ?? [];
        });
    assert(!cookies.some((cookie) => cookie.startsWith("TGC=")), "Replayed token must not issue a ticket-granting cookie");
    assert(body.includes(TOKEN_FAILURE), "Replayed token must be refused");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await deleteTicketGrantingCookie(page);
    await cas.gotoLogin(page);

    await cas.assertElementDoesNotExist(page, "#password");

    await cas.attributeValue(page, "#username", "autocapitalize", "none");
    await cas.attributeValue(page, "#username", "spellcheck", "false");
    await cas.attributeValue(page, "#username", "autocomplete", "username");
    await cas.assertAccessibility(page, "Passwordless user id");

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#login h3", "Provide Token");
    await cas.assertInnerTextStartsWith(page, "#login p", "Please provide the security token sent to you");
    await cas.assertVisibility(page, "#token");
    await cas.attributeValue(page, "#token", "autocomplete", "one-time-code");
    await cas.attributeValue(page, "#token", "type", "text");
    await cas.assertAccessibility(page, "Passwordless token");

    const code = await cas.extractFromEmail(browser);
    const execution = await cas.elementValue(page, "#fm1 input[name=execution]");

    await submitToken(page, code);

    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, casuser, have successfully logged in");

    await cas.click(page, "#auth-tab");
    await cas.sleep(1000);
    await cas.type(page, "#attribute-tab-1 input[type=search]", "surrogate");
    await cas.sleep(1000);
    await cas.screenshot(page);

    const surrogateEnabled = await page.$("#surrogateEnabled");
    assert(surrogateEnabled === null);
    const surrogatePrincipal = await page.$("#surrogatePrincipal");
    assert(surrogatePrincipal === null);
    const surrogateUser = await page.$("#surrogateUser");
    assert(surrogateUser === null);
    await cas.sleep(1000);

    await cas.log("Replaying the used token with its flow execution must fail: tokens are single-use");
    await replayToken(execution, code);
    await deleteTicketGrantingCookie(page);

    await cas.log("A wrong token is refused, and the token sent by email stays valid");
    await requestToken(page);
    const secondCode = await cas.extractFromEmail(browser);
    assert(secondCode !== code, "A new token must be issued for a new login");
    await submitToken(page, "abcdef");
    await assertTokenRefused(page);
    await submitToken(page, secondCode);
    await cas.assertCookie(page);
    await deleteTicketGrantingCookie(page);

    await cas.log("A token used after its expiration is refused");
    await requestToken(page);
    const thirdCode = await cas.extractFromEmail(browser);
    await cas.sleep(32000);
    await submitToken(page, thirdCode);
    await assertTokenRefused(page);

    await cas.closeBrowser(browser);
})();
