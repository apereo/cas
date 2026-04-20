const cas = require("../../cas.js");

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

(async () => {
    let failed = false;
    const browser = await cas.newBrowser(cas.browserOptions({ options: ["--disable-local-storage"] }));
    const context = await browser.createBrowserContext();
    const duoUser = "duocode1";
    
    try {
        const page = await cas.newPage(context);
        await makeBrowserStorageUnavailable(page);
        await cas.updateDuoSecurityUserStatus(duoUser);
        const service = "https://localhost:9859/anything/attributes";
        await cas.gotoLoginWithAuthnMethod(page, service, "mfa-duo");
        await cas.sleep(3000);
        await cas.logPage(page);
        await cas.loginWith(page, duoUser, "Mellon");
        await cas.sleep(2000);
        await cas.assertTextContent(page, "#errorPanel h3", "Unable to proceed to the next step.");
        await cas.assertTextContentStartsWith(page, "#errorPanel p", "Storage API is likely not supported");
    } catch (e) {
        failed = true;
        throw e;
    } finally {
        await context.close();
        await cas.closeBrowser(browser);
        if (!failed) {
            await process.exit(0);
        }
    }
})();
