import { defineConfig } from 'vite';
import { resolve } from 'path';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig(({mode}) => ({
    define: {
        'process.env.NODE_ENV': JSON.stringify(mode),
    },
    target: 'esnext',
    base: '/cas/palantir',
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'https://localhost:8443',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, ''),
            }
        }
    },
    plugins: [
        react(),
    ],
    build: {
        lib: {
            entry: resolve(__dirname, 'src/main.jsx'),
            name: 'Palantir',
            fileName: (format) => `palantir.${format}.js`,
            formats: [
                'cjs',
                'umd',
                'iife',
                'es'
            ]
        }
    }
  }));
