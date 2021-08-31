const startPuppeteerLoadTest = require('puppeteer-loadtest');

const file = "./ci/tests/puppeteer/scenarios/db-postgres-load/test.js";
const samplesRequested = 20;
const concurrencyRequested = 5;

const loattest = async () => {
    const results = await startPuppeteerLoadTest({
        file,
        samplesRequested,
        concurrencyRequested,
    });
    console.log(JSON.stringify(results, null, 2));
}

loattest();