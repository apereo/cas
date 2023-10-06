import { defineConfig } from 'vite';
import { resolve } from 'path';
import react from '@vitejs/plugin-react';

const base = '/cas/palantir';

// https://vitejs.dev/config/
export default defineConfig(({mode}) => {
    return ({
        define: {
            'process.env.NODE_ENV': JSON.stringify(mode),
        },
        target: 'esnext',
        base,
        server: {
            port: 3000,
            proxy: {
                [`${base}/api`]: {
                    target: 'https://localhost:8443',
                    changeOrigin: true,
                    secure: false,
                    rewrite: (path) => {
                        return path.replace('/api', '');
                    },
                }
            }
        },
        plugins: [
            react(),
        ],
        build: {
            minify: mode === 'production' ? true : false,
            lib: {
                entry: resolve(__dirname, 'src/main.jsx'),
                name: 'Palantir',
                fileName: (format) => `palantir.${format}.js`,
                formats: [
                    'umd'
                ]
            }
        }
    });
});