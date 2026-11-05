import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/auth':          { target: 'http://localhost:8081', changeOrigin: true },
      '/api/users':         { target: 'http://localhost:8081', changeOrigin: true },
      '/api/certificates':  { target: 'http://localhost:8081', changeOrigin: true },
      '/api/portfolio':     { target: 'http://localhost:8081', changeOrigin: true },
      '/api/orders':        { target: 'http://localhost:8082', changeOrigin: true },
      '/api/trades':        { target: 'http://localhost:8082', changeOrigin: true },
      '/api/prices':        { target: 'http://localhost:8082', changeOrigin: true },
      '/api/notifications': { target: 'http://localhost:8082', changeOrigin: true },
    }
  }
})
