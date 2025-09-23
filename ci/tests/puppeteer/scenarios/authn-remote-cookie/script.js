
const cas = require("../../cas.js");

const COOKIE_VALUE = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCIsImtpZCI6ImRmNGQ2NTgzLTFhOTItNDA1Mi05ZjNlLWJkM2IyYjY3ZGY3ZiJ9.ZXlKNmFYQWlPaUpFUlVZaUxDSmhiR2NpT2lKa2FYSWlMQ0psYm1NaU9pSkJNVEk0UTBKRExVaFRNalUySWl3aVkzUjVJam9pU2xkVUlpd2lkSGx3SWpvaVNsZFVJaXdpYTJsa0lqb2lPRE5pTW1Ga05EZ3RZV0U0WkMwMFlqa3pMV0ZpWW1JdFlUTm1ORGs0Tm1SaU5Ua3hJbjAuLno5ZnRWMnAxcG5qVEUwTWZUYUQ4Q1EudVA1em5YVmVRaldhVVFmOGxDaGxrdy5QU2E1NWJDaHVyUHRhcTV5eFdVb3NB.G8uX6Ecc2KMe7wP1615Z81mODpmy4HbGZb2SSWFs_nf8iTjke3HpK0pxegdMNg-scmmZ7vUt7CQzzEffgfNqZA";

async function verifyWithoutService() {
    const browser2 = await cas.newBrowser(cas.browserOptions());
    const page2 = await cas.newPage(browser2);
    await page2.setCookie(await buildCookie(page2, COOKIE_VALUE, "/", "MyCookie"));
    await cas.gotoLogin(page2, "https://localhost:8443/cas/login");
    await cas.sleep(2000);
    await cas.assertCookie(page2);
    await cas.screenshot(page2);
    await cas.closeBrowser(browser2);
}

async function verifyWithService() {
    const browser2 = await cas.newBrowser(cas.browserOptions());
    const page2 = await cas.newPage(browser2);
    await page2.setCookie(await buildCookie(page2, COOKIE_VALUE, "/", "MyCookie"));
    await cas.gotoLogin(page2, "https://localhost:9859/anything/1");
    await cas.sleep(2000);
    await cas.assertTicketParameter(page2);
    await cas.closeBrowser(browser2);
}

async function buildCookie(page, value, path, name) {
    await cas.log(`Adding cookie ${name}:${value}:${path}`);
    return {
        name: name,
        value: value,
        domain: "localhost",
        path: path,
        httpOnly: true,
        secure: true
    };
}

(async () => {
    await verifyWithoutService();
    await verifyWithService();
})();
