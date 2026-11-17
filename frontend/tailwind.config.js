export default { content: ['./index.html','./src/**/*.{ts,tsx}'], theme: { extend: { fontFamily: { sans: ['Inter','system-ui','sans-serif'] },
  colors: { navy: { DEFAULT: '#14233B', 800: '#0E1A2D', 900: '#0A1322' }, brand: { DEFAULT: '#2563EB', 400: '#60A5FA', 600: '#1D4ED8' }, surface: '#F7F9FC', ok: '#16A34A', warn: '#D97706', danger: '#DC2626' },
  boxShadow: { soft: '0 1px 2px rgba(20,35,59,.04), 0 8px 24px -8px rgba(20,35,59,.10)', glow: '0 0 0 1px rgba(96,165,250,.25), 0 20px 60px -15px rgba(37,99,235,.55)' },
  keyframes: { rise: { from: { opacity: 0, transform: 'translateY(14px)' }, to: { opacity: 1, transform: 'none' } } }, animation: { rise: 'rise .6s ease-out both' } } }, plugins: [] }
