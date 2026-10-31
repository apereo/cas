const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    let failed = false;
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogout(page);
        await cas.gotoLogin(page, service);
        await cas.sleep(2000);
        await cas.assertTextContent(page, "#accountSignUpLink", "Sign Up");
        await cas.submitForm(page, "#accountMgmtSignupForm");
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#content h2", "Account Registration");
        await cas.type(page, "#username", "casuser");
        await cas.type(page, "#firstName", "CAS");
        await cas.type(page, "#lastName", "Person");
        await cas.type(page, "#email", "cas@example.org");
        await cas.type(page, "#phone", "+1 347 745 1234");
        await cas.click(page, "#submit");
        await cas.waitForNavigation(page);
        await cas.assertInnerTextStartsWith(page, "#content p", "Account activation instructions are successfully sent");

        const link = await cas.extractFromEmail(browser);
        assert(link !== undefined);
        const activationToken = new URL(link).searchParams.get("acctregtoken");
        await cas.log(`Account registration ticket is ${activationToken.length} characters`);
        assert(activationToken.startsWith("TST-"), activationToken);

        await cas.goto(page, link);
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#content h2", "Account Registration");
        await cas.assertInnerTextStartsWith(page, "#content p", "Welcome back!");
        await page.focus("#password");
        await cas.type(page, "#password", "EaP8R&iX$eK4nb8eAI");
        await page.$eval("#password", (e) => e.blur());
        await page.focus("#confirmedPassword");
        await cas.type(page, "#confirmedPassword", "EaP8R&iX$eK4nb8eAI");
        await page.$eval("#confirmedPassword", (e) => e.blur());
        await cas.sleep(1000);
        for (let i = 1; i <= 2; i++) {
            await cas.type(page, `#securityquestion${i}`, `Security question ${i}`);
            await cas.type(page, `#securityanswer${i}`, `Security answer ${i}`);
        }
        await cas.click(page, "#submit");
        await cas.sleep(500);
        await cas.assertInnerTextStartsWith(page, "#content p", "Thank you! Your account is now activated");
        await cas.sleep(5000);
        await cas.assertPageUrlStartsWith(page, service);
        await cas.assertTicketParameter(page);

        await cas.gotoLogout(page);
        await cas.log("A tampered activation link is rejected");
        const index = Math.floor(activationToken.length * 2 / 3);
        const tampered = `${activationToken.substring(0, index)}${activationToken[index] === "A" ? "B" : "A"}${activationToken.substring(index + 1)}`;
        await cas.goto(page, `https://localhost:8443/cas/login?acctregtoken=${encodeURIComponent(tampered)}`);
        await cas.sleep(1000);
        await cas.assertInnerTextDoesNotContain(page, "#content", "Welcome back!");
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
