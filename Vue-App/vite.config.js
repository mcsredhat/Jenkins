import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  
  // Base public path when served in development or production
  base: '/',
  
  // Define global constants
  define: {
    __APP_VERSION__: JSON.stringify(process.env.npm_package_version || '1.0.0'),
    __BUILD_TIME__: JSON.stringify(new Date().toISOString())
  },
  
  // Development server options
  server: {
    port: 3000,
    host: true, // Listen on all addresses
    strictPort: false,
    open: false,
    cors: true,
    // Proxy API requests if needed
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  },
  
  // Preview server options (for production preview)
  preview: {
    port: 4173,
    host: true,
    strictPort: false,
    open: false,
    cors: true
  },
  
  // Build options
  build: {
    // Build output directory
    outDir: 'dist',
    
    // Generate source maps for debugging
    sourcemap: process.env.NODE_ENV !== 'production',
    
    // Minify options
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: process.env.NODE_ENV === 'production',
        drop_debugger: process.env.NODE_ENV === 'production'
      }
    },
    
    // Rollup options
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html')
      },
      output: {
        // Chunk splitting strategy
        manualChunks: {
          vendor: ['vue'],
          utils: ['./src/utils/index.js']
        },
        // Asset file naming
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.')
          let extType = info[info.length - 1]
          if (/png|jpe?g|svg|gif|tiff|bmp|ico/i.test(extType)) {
            extType = 'img'
          } else if (/woff|woff2|eot|ttf|otf/i.test(extType)) {
            extType = 'fonts'
          }
          return `assets/${extType}/[name]-[hash][extname]`
        },
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js'
      }
    },
    
    // Chunk size warning limit
    chunkSizeWarningLimit: 1000,
    
    // Assets inline threshold
    assetsInlineLimit: 4096,
    
    // CSS code splitting
    cssCodeSplit: true,
    
    // Empty outDir before build
    emptyOutDir: true,
    
    // Copy public directory
    copyPublicDir: true
  },
  
  // Path resolution
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@components': resolve(__dirname, 'src/components'),
      '@assets': resolve(__dirname, 'src/assets'),
      '@utils': resolve(__dirname, 'src/utils'),
      '@types': resolve(__dirname, 'src/types')
    },
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
  },
  
  // CSS options
  css: {
    devSourcemap: true,
    preprocessorOptions: {
      scss: {
        additionalData: `@import "@/styles/variables.scss";`
      }
    },
    modules: {
      localsConvention: 'camelCase'
    }
  },
  
  // Optimization options
  optimizeDeps: {
    include: ['vue'],
    exclude: ['vue-demi']
  },
  
  // Environment variables
  envPrefix: ['VITE_', 'VUE_APP_'],
  
  // Worker options
  worker: {
    format: 'es'
  },
  
  // JSON loading options
  json: {
    namedExports: true,
    stringify: false
  },
  
  // Legacy browser support (uncomment if needed)
  // legacy: {
  //   targets: ['defaults', 'not IE 11']
  // },
  
  // PWA options (if using @vite/plugin-pwa)
  // pwa: {
  //   registerType: 'autoUpdate',
  //   workbox: {
  //     globPatterns: ['**/*.{js,css,html,ico,png,svg}']
  //   }
  // }
})