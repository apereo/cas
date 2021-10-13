const assert = require('assert');
const fs = require('fs');
const {JSONPath} = require('jsonpath-plus');

const startPuppeteerLoadTest = require('puppeteer-loadtest');
let args = process.argv.slice(2);
const config = JSON.parse(fs.readFileSync(args[0]));
assert(config != null)

const paramOptions = {
    file: config.loadScript,
    samplesRequested: config.samplesRequested,
    concurrencyRequested: config.concurrencyRequested
}
const loattest = async () => {
    return await startPuppeteerLoadTest(paramOptions);
}

loattest().then(results => {
    console.log(JSON.stringify(results, null, 2))
    const samples = JSONPath({path: '$..sample', json: results })
    assert(samples.length === parseInt(config.samplesRequested))
});
