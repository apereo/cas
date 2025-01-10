const { readLocalStorage, newBrowser, newPage, logg, logr, sleep, browserOptions } = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await newBrowser(browserOptions());
    let failed = false;

    try {
        const page = await newPage(browser);

        await logg("Starting localStorage fallback test...");

        // Debugging for network activity
        page.on("request", request => logg(`Request made: ${request.url()}`));
        page.on("requestfailed", request => logr(`Request failed: ${request.url()}`));
        page.on("response", response => logg(`Response received: ${response.url()} -> ${response.status()}`));

        // Error handling for the page
        page.on("error", err => logr("Page error:", err));
        page.on("pageerror", err => logr("Page error:", err));
        page.on("console", msg => {
            if (msg.type() === "error") {
                logr("Console error:", msg.text());
            }
        });

        await logg("Attempting to navigate to CAS login page...");

        // Retry navigation logic
        let response = null;
        let attempts = 0;
        const maxAttempts = 3;

        while (attempts < maxAttempts) {
            try {
                await logg(`Connection attempt ${attempts + 1} of ${maxAttempts}...`);
                response = await page.goto("https://localhost:8443/cas/login", {
                    waitUntil: "networkidle2",
                    timeout: 30000,
                });

                const loginFormExists = await page.$("#username, #fm1");
                if (loginFormExists) {
                    await logg("Successfully connected to CAS login page");
                    break;
                }

                throw new Error("Page load incomplete or login form not found");
            } catch (e) {
                attempts++;
                logr(`Attempt ${attempts} failed:`, e.message);

                if (attempts === maxAttempts) {
                    throw new Error(`Failed to load page after ${maxAttempts} attempts: ${e.message}`);
                }

                await sleep(5000); // Wait before retrying
            }
        }

        if (!response || !response.ok()) {
            throw new Error("Failed to load the CAS login page successfully");
        }

        // Storage tests
        await logg("Testing direct storage access...");
        const canAccessStorage = await page.evaluate(() => {
            try {
                const testKey = "test_" + Date.now();
                window.localStorage.setItem(testKey, "test");
                window.localStorage.removeItem(testKey);
                return true;
            } catch (e) {
                console.error("Storage access error:", e);
                return false;
            }
        }).catch(e => {
            logr("Storage access evaluation failed:", e);
            return false;
        });

        await logg(`Storage accessibility: ${canAccessStorage}`);

        if (!canAccessStorage) {
            await logr("Cannot access localStorage directly - checking fallback behavior");

            const fallbackResult = await page.evaluate(() => {
                try {
                    window.sessionStorage.clear();
                    window.writeToLocalStorage({
                        context: "FallbackTest",
                        payload: "FallbackPayload",
                    });

                    const data = window.sessionStorage.getItem("CAS");
                    return data ? JSON.parse(data) : null;
                } catch (e) {
                    console.error("Fallback storage error:", e);
                    return { error: e.message };
                }
            }).catch(e => {
                logr("Fallback evaluation failed:", e);
                return { error: e.message };
            });

            await logg("Fallback test result:", fallbackResult);
            assert(fallbackResult && fallbackResult.FallbackTest === "FallbackPayload",
                "Fallback to sessionStorage should work when localStorage is unavailable");
        } else {
            await page.evaluate(() => {
                window.writeToLocalStorage({
                    context: "TestContext",
                    payload: "TestPayload",
                });
            });

            const storageData = await readLocalStorage(page);
            assert(storageData && storageData["CAS"], "CAS data should exist in storage");

            const payload = JSON.parse(storageData["CAS"]);
            assert(payload.TestContext === "TestPayload", "Storage payload should match expected value");
        }

        await logg("All storage tests completed successfully");
    } catch (e) {
        failed = true;
        logr("Test failed with error:");
        logr(e);
        throw e;
    } finally {
        await browser.close();

        if (!failed) {
            logg("Test completed successfully");
            process.exit(0);
        } else {
            logr("Test failed");
            process.exit(1);
        }
    }
})();