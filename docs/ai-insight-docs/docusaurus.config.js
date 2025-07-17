const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

// With JSDoc @type annotations, IDEs can provide config autocompletion
/** @type {import('@docusaurus/types').DocusaurusConfig} */
(module.exports = {
  title: 'AI Insight Dashboard',
  tagline: 'Documentation for AI Insight Dashboard',
  url: 'https://your-deployment-url.com',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'UNBunny', // Your GitHub org/user name.
  projectName: 'ai-insight-dashboard', // Your repo name.

  presets: [
    [
      '@docusaurus/preset-classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Comment out or update if you have a public repository
          // editUrl: 'https://github.com/UNBunny/ai-insight-dashboard/edit/main/docs/',
        },
        blog: {
          showReadingTime: true,
          // Comment out or update if you have a public repository
          // editUrl: 'https://github.com/UNBunny/ai-insight-dashboard/edit/main/docs/blog/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'AI Insight Dashboard',
        logo: {
          alt: 'AI Insight Dashboard Logo',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'doc',
            docId: 'intro',
            position: 'left',
            label: 'Documentation',
          },
          {
            type: 'doc',
            docId: 'api/overview',
            position: 'left',
            label: 'API',
          },
          {
            type: 'doc',
            docId: 'guides/getting-started',
            position: 'left',
            label: 'Guides',
          },
          {to: '/blog', label: 'Updates', position: 'left'},
          // Update this with your actual repository URL if available
          // {
          //   href: 'https://github.com/UNBunny/ai-insight-dashboard',
          //   label: 'GitHub',
          //   position: 'right',
          // },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Documentation',
            items: [
              {
                label: 'Introduction',
                to: '/docs/intro',
              },
              {
                label: 'Getting Started',
                to: '/docs/guides/getting-started',
              },
              {
                label: 'API Reference',
                to: '/docs/api/overview',
              },
            ],
          },
          {
            title: 'Features',
            items: [
              {
                label: 'Dashboard',
                to: '/docs/features/dashboard',
              },
              {
                label: 'Metrics',
                to: '/docs/features/metrics',
              },
              {
                label: 'Analytics',
                to: '/docs/features/analytics',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'Updates',
                to: '/blog',
              },
              {
                label: 'Support',
                to: '/docs/support',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} AI Insight Dashboard. Built with Docusaurus.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
});
