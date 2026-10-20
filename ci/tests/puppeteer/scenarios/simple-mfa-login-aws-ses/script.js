const cas = require("../../cas.js");
const assert = require("assert");

/*
 * Reads the messages localstack accepted through SES, keeping those sent to the given address, oldest first.
 */
async function sentMessages(recipient) {
    const response = JSON.parse(await cas.doRequest("http://localhost:4566/_aws/ses", "GET", {"Accept": "application/json"}));
    return response.messages
        .filter((message) => (message.Destination?.ToAddresses ?? []).includes(recipient))
        .sort((first, second) => new Date(first.Timestamp) - new Date(second.Timestamp));
}

async function waitForToken(recipient, previousCount) {
    for (let attempt = 1; attempt <= 10; attempt++) {
        const messages = await sentMessages(recipient);
        if (messages.length > previousCount) {
            const message = messages[messages.length - 1];
            await cas.log(`Email ${message.Id} from ${message.Source} with subject ${message.Subject}`);
            assert(message.Source === "hello@example.com");
            assert(message.Subject === "Token");
            const token = message.Body.text_part.trim();
            assert(token.length > 0);
            return {token: token, count: messages.length};
        }
        await cas.sleep(1000);
    }
    throw new Error(`No email was sent through SES to ${recipient}`);
}

(async () => {
    const recipient = "casuser@example.org";
    const existing = (await sentMessages(recipient)).length;

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#token");

    const first = await waitForToken(recipient, existing);
    await cas.type(page, "#token", "unknownCode");
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);
    await cas.assertTextContentStartsWith(page, "div .banner-danger p", "Multifactor authentication attempt has failed");

    await cas.assertVisibility(page, "#resendButton");
    await cas.click(page, "#resendButton");
    await cas.sleep(1000);
    const second = await waitForToken(recipient, first.count);
    await cas.log("A token that has not expired yet is sent again rather than replaced");
    assert(second.token === first.token);

    await cas.type(page, "#token", second.token);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);
    await cas.closeBrowser(browser);
})();
