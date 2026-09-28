const cas = require("../../cas.js");
const assert = require("assert");

async function makeBrowserStorageUnavailable(page) {
    await page.evaluateOnNewDocument(() => {
        const makeBrokenStorage = () => ({
            getItem() {
                throw new TypeError("Storage API unavailable");
            },
            setItem() {
                throw new TypeError("Storage API unavailable");
            },
            removeItem() {
                throw new TypeError("Storage API unavailable");
            },
            clear() {
                throw new TypeError("Storage API unavailable");
            },
            key() {
                throw new TypeError("Storage API unavailable");
            },
            get length() {
                throw new TypeError("Storage API unavailable");
            }
        });

        Object.defineProperty(window, "localStorage", {
            configurable: true,
            get() {
                return makeBrokenStorage();
            }
        });

        Object.defineProperty(window, "sessionStorage", {
            configurable: true,
            get() {
                return makeBrokenStorage();
            }
        });
    });
}

async function makeScriptCookiesUnavailable(page) {
    await page.evaluateOnNewDocument(() => {
        Object.defineProperty(Document.prototype, "cookie", {
            configurable: true,
            get() {
                return "";
            },
            set() {
            }
        });
    });
}

async function browserStorageCookies(page) {
    const cookies = await page.cookies("https://localhost:8443/cas/login");
    return cookies.filter((cookie) => cookie.name.startsWith("CasBrowserStorage_"));
}

(async () => {
    let failed = false;
    const browser = await cas.newBrowser(cas.browserOptions({ options: ["--disable-local-storage"] }));
    const duoUser = "duocode1";
    const service = "https://localhost:9859/anything/attributes";

    try {
        await cas.updateDuoSecurityUserStatus(duoUser);

        await cas.log("Browser storage is unavailable; Duo Security state must fall back onto cookies");
        let context = await browser.createBrowserContext();
        let page = await cas.newPage(context);
        await makeBrowserStorageUnavailable(page);
        await cas.gotoLoginWithAuthnMethod(page, service, "mfa-duo");
        await cas.sleep(3000);
        await cas.logPage(page);
        await cas.loginWith(page, duoUser, "Mellon");
        await cas.sleep(4000);
        const fallbackCookies = await browserStorageCookies(page);
        await cas.log(`Found ${fallbackCookies.length} browser storage cookie(s) while at ${await page.url()}`);
        assert(fallbackCookies.some((c) => c.name === "CasBrowserStorage_DuoSecuritySessionContext_n"));
        assert(fallbackCookies.every((c) => c.path === "/cas" && c.secure && c.sameSite === "Lax"));
        const bypassCodes = await cas.fetchDuoSecurityBypassCodes(duoUser);
        await cas.loginDuoSecurityBypassCode(page, duoUser, bypassCodes);
        await cas.sleep(4000);
        await cas.screenshot(page);

        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.user === duoUser);
        assert(authenticationSuccess.attributes.authnContextClass[0] === "mfa-duo");
        assert(authenticationSuccess.attributes.duoAuthResult[0] === "allow");

        await cas.gotoLogin(page);
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.assertCookie(page);
        const leftover = await browserStorageCookies(page);
        assert(leftover.length === 0, `Browser storage cookies must be removed: ${leftover.map((c) => c.name)}`);
        await context.close();

        await cas.log("Browser storage and script cookies are both unavailable; login must fail");
        context = await browser.createBrowserContext();
        page = await cas.newPage(context);
        await makeBrowserStorageUnavailable(page);
        await makeScriptCookiesUnavailable(page);
        await cas.gotoLoginWithAuthnMethod(page, service, "mfa-duo");
        await cas.sleep(3000);
        await cas.loginWith(page, duoUser, "Mellon");
        await cas.sleep(2000);
        await cas.assertTextContent(page, "#errorPanel h3", "Unable to proceed to the next step.");
        await cas.assertTextContentStartsWith(page, "#errorPanel p", "Neither the Storage API nor cookies are supported");
        await context.close();
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
