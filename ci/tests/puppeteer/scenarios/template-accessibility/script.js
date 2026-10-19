const cas = require("../../cas.js");
const assert = require("assert");
const {AxePuppeteer} = require("@axe-core/puppeteer");

const BLOCKING_IMPACTS = ["serious", "critical"];
const WCAG_TAGS = ["wcag2a", "wcag2aa", "wcag21a", "wcag21aa"];
const TAGS = [...WCAG_TAGS, "best-practice"];

const isBlocking = (violation) => BLOCKING_IMPACTS.includes(violation.impact)
    && violation.tags.some((tag) => WCAG_TAGS.includes(tag));

/*
 * Waits for the animations that end, such as the fade-in of the page content, so that axe measures colors at full
 * opacity. Infinite ones, such as the notification bell, are left running.
 */
async function waitForAnimations(page) {
    await page.evaluate(() => Promise.all(document.getAnimations()
        .filter((animation) => animation.effect?.getComputedTiming().endTime !== Infinity)
        .map((animation) => animation.finished.catch(() => undefined))));
}

/*
 * Runs axe-core over the page as it stands and logs every violation with the elements it found.
 * Serious and critical violations of WCAG 2.1 A/AA are returned so the scenario can fail on them
 * once every page has been checked; best-practice findings and lesser impacts are only reported.
 */
async function audit(page, name) {
    await waitForAnimations(page);
    await cas.screenshot(page);
    const results = await new AxePuppeteer(page).withTags(TAGS).analyze();
    await cas.log(`${name}: ${results.violations.length} violation(s), ${results.passes.length} rule(s) passed`);
    for (const violation of results.violations) {
        const targets = violation.nodes.map((node) => node.target.join(" ")).join(", ");
        const message = `[${violation.impact}] ${violation.id}: ${violation.help} -> ${targets}`;
        if (isBlocking(violation)) {
            await cas.logr(`${name} ${message}`);
        } else {
            await cas.logy(`${name} ${message}`);
        }
    }
    return results.violations
        .filter(isBlocking)
        .map((violation) => `${name}: ${violation.id}`);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const blocking = [];

    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.assertVisibility(page, "#username");
    blocking.push(...await audit(page, "Login"));

    await cas.loginWith(page, "casuser", "BadPassword");
    await cas.assertVisibility(page, "#loginErrorsPanel");
    blocking.push(...await audit(page, "Login error"));

    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.gotoLogin(page, "https://example.org");
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#content h2", "Attribute Consent");
    blocking.push(...await audit(page, "Attribute consent"));

    await cas.gotoLogout(page);
    await cas.assertCookie(page, false);
    blocking.push(...await audit(page, "Logout"));

    await cas.gotoLoginWithAuthnMethod(page, undefined, "GoogleAuth");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#token");
    blocking.push(...await audit(page, "Google Authenticator"));

    await cas.closeBrowser(browser);
    assert(blocking.length === 0, `Serious or critical accessibility violations found: ${blocking.join(", ")}`);
})();
