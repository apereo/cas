
const cas = require("../../cas.js");

const SERVICE = "https://localhost:9859/anything/cas";

/**
 * The hard timeout ends the single sign-on session once its time to kill passes, however often it is used.
 *
 * @param page the page
 */
async function verifyHardTimeout(page) {
    await cas.log("Waiting for hard timeout to complete...");
    await cas.sleep(3000);
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);
}

/**
 * The throttled timeout keeps the single sign-on session while uses are spaced out, and ends it when it is used again
 * within the time allowed in between uses.
 *
 * @param page the page
 */
async function verifyThrottledTimeout(page) {
    await cas.log("Using the session after the time in between uses has passed");
    await cas.sleep(4000);
    await cas.gotoLogin(page, SERVICE);
    await cas.assertTicketParameter(page);

    await cas.log("Using the session again too soon");
    await cas.sleep(1500);
    await cas.gotoLogin(page, SERVICE);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page, false);
    await cas.assertVisibility(page, "#username");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    if (process.env.SCENARIO_VARIATION === "throttled") {
        await verifyThrottledTimeout(page);
    } else {
        await verifyHardTimeout(page);
    }
    await cas.closeBrowser(browser);
})();
