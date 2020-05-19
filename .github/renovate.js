module.exports = {
  branchPrefix: 'renovate/',
  gitAuthor: 'Renovate Bot <bot@renovateapp.com>',
  logLevel: 'debug',
  onboarding: false,
  platform: 'github',
  includeForks: true,
  repositories: [
    'apereo/cas'
  ],
  enabledManagers: ["gradle"],
  "gradle": {
    "enabled": true
  }
};
