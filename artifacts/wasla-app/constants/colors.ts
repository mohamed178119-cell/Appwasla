/**
 * Semantic design tokens for the mobile app.
 *
 * These tokens mirror the naming conventions used in web artifacts (index.css)
 * so that multi-artifact projects share a cohesive visual identity.
 *
 * Replace the placeholder values below with values that match the project's
 * brand. If a sibling web artifact exists, read its index.css and convert the
 * HSL values to hex so both artifacts use the same palette.
 *
 * To add dark mode, add a `dark` key with the same token names.
 * The useColors() hook will automatically pick it up.
 */

const colors = {
  light: {
    // Legacy aliases (kept for backward compatibility)
    text: '#dce9f2',
    tint: '#20d9d0',

    // Core surfaces
    background: '#0e1727',
    foreground: '#dce9f2',

    // Cards / elevated surfaces
    card: '#142036',
    cardForeground: '#dce9f2',

    // Primary action color (buttons, links, active states)
    primary: '#20d9d0',
    primaryForeground: '#0e1727',

    // Secondary / less-emphasis interactive surfaces
    secondary: '#8581e8',
    secondaryForeground: '#0e1727',

    // Muted / subdued elements (dividers, timestamps, placeholders)
    muted: '#1b2941',
    mutedForeground: '#8fa2b7',

    // Accent highlights (badges, selected items, focus rings)
    accent: '#b795f4',
    accentForeground: '#0e1727',

    // Destructive actions (delete, error states)
    destructive: '#ed7777',
    destructiveForeground: '#0e1727',

    // Borders and input outlines
    border: '#273650',
    input: '#273650',
  },

  // Border radius (in px). Sync from the sibling web artifact's --radius
  // CSS variable. This value applies to cards, buttons, inputs, and modals.
  radius: 8,
};

export default colors;
