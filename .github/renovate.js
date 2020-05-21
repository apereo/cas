module.exports = {
  branchPrefix: 'renovate/',
  gitAuthor: 'Renovate Bot <bot@renovateapp.com>',
  logLevel: 'warn',
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
