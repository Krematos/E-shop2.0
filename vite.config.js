import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    target: 'esnext', // Zajišťuje využití moderních vlastností JS a lepší kompresi
    minify: 'esbuild', // Esbuild je extrémně rychlý a efektivní pro minifikaci a tree-shaking
    cssMinify: true,
    rollupOptions: {
      output: {
        // Rozdělení velkého JS na menší části (chunks)
        manualChunks: {
          vendor: ['react', 'react-dom', 'react-router-dom'],
          ui: ['lucide-react'],
        }
      }
    }
  }
})
