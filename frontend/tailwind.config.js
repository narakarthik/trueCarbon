export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        bg: '#0a0a0f', surface: '#111118', border: '#1e1e2e',
        muted: '#3f3f52', dim: '#71717a', text: '#e4e4e7',
        green: { DEFAULT: '#22c55e', dim: '#16a34a', bg: '#052e16' },
        red:   { DEFAULT: '#ef4444', dim: '#b91c1c', bg: '#2d0a0a' },
      },
      fontFamily: {
        sans: ['-apple-system','BlinkMacSystemFont','Segoe UI','sans-serif'],
        mono: ['ui-monospace','SFMono-Regular','monospace'],
      },
    },
  },
  plugins: [],
}
