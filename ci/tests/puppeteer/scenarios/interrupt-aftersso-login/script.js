const cas = require("../../cas.js");

async function verifyInterruption(browser) {
    const service = "https://localhost:9859/anything/cas";

    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertTextContent(page, "#interruptMessage", "We interrupted your login");
    await cas.assertCookie(page);
    await cas.assertVisibility(page, "#interruptLinks");
    await cas.assertVisibility(page, "#attributesTable");
    await cas.assertVisibility(page, "#field1");
    await cas.assertVisibility(page, "#field1-value");
    await cas.assertVisibility(page, "#field2");
    await cas.assertVisibility(page, "#field2-value");
    await cas.assertInvisibility(page, "#cancel");
    await cas.submitForm(page, "#fm1");
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.log("Attempt to log into the application again using existing SSO session");
    await cas.gotoLogin(page, service);
    await cas.assertTicketParameter(page);

    for (let i = 0; i < 2; i++) {
        await cas.gotoLogin(page, service + i);
        await cas.sleep(500);
        await cas.assertTicketParameter(page);
    }

    await cas.gotoLogin(page);
    await cas.assertCookie(page, true, "CASINTERRUPT");
    
    await cas.gotoLogout(page);
    await cas.assertCookie(page, false, "CASINTERRUPT");
}

async function verifyInterruptionBlocked(context) {
    const service = "https://localhost:9859/anything/cas";
    
    const page = await cas.newPage(context);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "casblock", "Mellon");
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.assertMissingParameter(page, "ticket");

    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertTextContent(page, "#interruptMessage", "You are blocked");
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertCookie(page);
    await cas.assertInvisibility(page, "#fm1");

    await page.evaluate(() => {
        const execution = document.querySelector("#formlinks input[name=execution]").value;
        const content = document.querySelector("#content");
        content.innerHTML += `
        <form method="post" id="fmblocked">
            <input type="hidden" name="execution" value="${execution}"/>
            <input type="hidden" name="_eventId" value="proceed"/>
            <button name="proceed" id="proceed" type="submit" />
        </form>
        `;
    });
    await cas.submitForm(page, "#fmblocked", undefined, 200);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await cas.logPage(page);
}

(async () => {

    const browser = await cas.newBrowser(cas.browserOptions());

    let context = await browser.createBrowserContext();
    await verifyInterruption(context);
    await context.close();

    context = await browser.createBrowserContext();
    await verifyInterruptionBlocked(context);
    await context.close();

    await cas.closeBrowser(browser);
    await cas.closeBrowser(browser);
})();
