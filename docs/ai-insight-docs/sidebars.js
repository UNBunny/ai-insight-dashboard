/**
 * Creating a sidebar enables you to:
 - create an ordered group of docs
 - render a sidebar for each doc of that group
 - provide next/previous navigation

 The sidebars can be generated from the filesystem, or explicitly defined here.

 Create as many sidebars as you want.
 */

module.exports = {
  // Main sidebar configuration for the documentation
  docs: [
    'intro',
    {
      type: 'category',
      label: 'Getting Started',
      items: ['guides/getting-started'],
    },
    {
      type: 'category',
      label: 'Features',
      items: [
        'features/dashboard',
        'features/metrics',
        'features/analytics',
      ],
    },
    {
      type: 'category',
      label: 'API Reference',
      items: ['api/overview'],
    },
    'support',
  ],
};
