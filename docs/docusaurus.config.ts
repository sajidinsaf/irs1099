import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'IRS 1099 Filing Platform',
  tagline: 'File your IRS 1099s electronically with ease',
  favicon: 'img/favicon.svg',

  future: {
    v4: true,
  },

  url: 'https://docs.irs1099.visibleai.com',
  baseUrl: '/',

  organizationName: 'sajidinsaf',
  projectName: 'irs1099',

  onBrokenLinks: 'throw',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          routeBasePath: '/',
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    navbar: {
      title: 'IRS 1099 Filing',
      logo: {
        alt: 'IRS 1099 Filing Logo',
        src: 'img/favicon.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docs',
          position: 'left',
          label: 'Documentation',
        },
        {
          href: 'https://irs1099.visibleai.com',
          label: 'Go to App',
          position: 'right',
        },
        {
          href: 'https://github.com/sajidinsaf/irs1099',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Documentation',
          items: [
            { label: 'Getting Started', to: '/' },
            { label: 'Filing 1099-NEC', to: '/filing-1099-nec' },
            { label: 'Payments', to: '/payments' },
          ],
        },
        {
          title: 'Product',
          items: [
            { label: 'Go to App', href: 'https://irs1099.visibleai.com' },
            { label: 'Register', href: 'https://irs1099.visibleai.com/register' },
          ],
        },
        {
          title: 'Support',
          items: [
            { label: 'FAQ', to: '/faq' },
            { label: 'Contact', to: '/support' },
          ],
        },
      ],
      copyright: `Copyright ${new Date().getFullYear()} IRS 1099 Filing Platform. Not affiliated with the IRS.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
    },
    colorMode: {
      defaultMode: 'light',
      disableSwitch: true,
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
