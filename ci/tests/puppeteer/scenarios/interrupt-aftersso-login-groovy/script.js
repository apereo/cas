
const assert = require("assert");
const cas = require("../../cas.js");

async function verifyInterruptEndpoint() {
    const service = encodeURIComponent("https://localhost:9859/anything/cas1");
    await cas.doGet(`https://localhost:8443/cas/actuator/interrupts/casuser?service=${service}`,
        (res) => {
            assert(res.status === 200);
            assert(res.data.principal === "casuser");
            assert(res.data.service === "https://localhost:9859/anything/cas1");
            assert(res.data.inquirers.some((inquirer) => inquirer.name === "GroovyScriptInterruptInquirer"));
            assert(res.data.interrupt.message === "Interrupted");
            assert(res.data.interrupt.links.Continue.startsWith("https://localhost:8443/cas/login?service="));
        },
        (error) => {
            throw error;
        });
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await verifyInterruptEndpoint();

    await cas.gotoLogin(page, "https://localhost:9859/anything/cas1");
    await cas.loginWith(page);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertTextContentStartsWith(page, "#interruptMessage", "Interrupted");
    await cas.assertCookie(page);
    await cas.assertCookie(page, false, "CASINTERRUPT");

    await cas.click(page, "#continue");
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page, true, "CASINTERRUPT");
    await cas.closeBrowser(browser);
})();
