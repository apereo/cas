const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    let token = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0IiwiYWxnIjoiZGlyIn0..zTe4qLwuyvVi6gHmSL4VcQ.NAmUxtxfEc-xJ0BO1DIUtxRN05-XTiB4bFVqN4YFvBPtVUrppTR5oXVKszWYQD_jWuhHnUBCVcvOBri9n-q6rKXQBkMnW9TLXQ4d9waJUOKpxORvgX3T56qoVfYTbUkVxe-5VchX000JWy8GdhpyWawldG0au03GhU7jhVnQeMlb7WWaNFOGXQwx6wvF0B30UL-6wgZO1nWD7InaQXJiFZazE0HK0DX61DUbP6PFYJKOBkbSWg9vSSzCeTxVFx9uJXMSAg9_vacpAYmq02ixV8e73CU9_hHhiCqEOYGunzwO4mEm6mn3fhPz6Q5azzPhZTc-lROKc66bmo_Y7jtMRmwgwBTq4diY9bIhw0x_qMsLLEdk1qk-dH9_FhBcunW2PkM8rCyGyDQ2slL-Axs_zg.HgP2g-UNsljcDhO74OMFWKxkYiSo4mbK";

    await page.goto(`https://localhost:8443/cas/login?service=https://apereo.github.io&token=${token}`);
    await page.waitForTimeout(1000);
    await cas.assertTicketParameter(page);

    await page.goto("https://localhost:8443/cas/login");
    await cas.assertTicketGrantingCookie(page);
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    
    await browser.close();
    
})();
