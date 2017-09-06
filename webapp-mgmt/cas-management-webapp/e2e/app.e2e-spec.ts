import { CasManagementAngularWebappPage } from './app.po';

describe('cas-management-angular-webapp App', function() {
  let page: CasManagementAngularWebappPage;

  beforeEach(() => {
    page = new CasManagementAngularWebappPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
