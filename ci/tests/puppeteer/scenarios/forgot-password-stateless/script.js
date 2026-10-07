const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    let failed = false;
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.sleep(2000);
        await cas.click(page, "#forgotPasswordLink");
        await cas.sleep(1000);
        await cas.type(page, "#username", "casuser");
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#content h2", "Password Reset Instructions Sent Successfully.");

        const link = await cas.extractFromEmail(browser);
        const resetToken = new URL(link).searchParams.get("pswdrst");
        await cas.log(`Password reset ticket is ${resetToken.length} characters`);
        assert(resetToken.startsWith("TST-"), resetToken);

        await cas.goto(page, link);
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#content #pwdmain h2", "Hello, casuser. You must change your password.");
        await cas.type(page, "#password", "EaP8R&iX$eK4nb8eAI");
        await cas.type(page, "#confirmedPassword", "EaP8R&iX$eK4nb8eAI");
        await cas.sleep(1000);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.assertInnerText(page, "#content h2", "Password Change Successful");

        await cas.log("The reset link is not single-use and stays valid until it expires, as documented");
        await cas.goto(page, link);
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#content #pwdmain h2", "Hello, casuser. You must change your password.");

        await cas.log("A tampered reset link is rejected");
        const index = Math.floor(resetToken.length * 2 / 3);
        const tampered = `${resetToken.substring(0, index)}${resetToken[index] === "A" ? "B" : "A"}${resetToken.substring(index + 1)}`;
        await cas.goto(page, `https://localhost:8443/cas/login?pswdrst=${encodeURIComponent(tampered)}`);
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#content h2", "Password Reset Failed");
    } catch (e) {
        failed = true;
        throw e;
    } finally {
        await cas.closeBrowser(browser);
        if (!failed) {
            await process.exit(0);
        }
    }
})();
