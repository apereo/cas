module.exports = {
  extractCookies: (requestParams, response, context, ee, next) => {
    context.vars.currentPageUrl = response.url;
    const setCookieHeaders = response.headers["set-cookie"];

    if (setCookieHeaders) {
      setCookieHeaders.forEach((cookie) => {
        const match = cookie.match(/^([^=]+)=([^;]+);/);
        if (match) {
          const [_, cookieName, cookieValue] = match;
          context.vars[cookieName] = cookieValue;
        }
      });
    }
    return next();
  }
};
