/**
 * @type {import("puppeteer").Configuration}
 */
module.exports = {
    chrome: {
        skipDownload: false
    },
    firefox: {
        skipDownload: true
    }
};
