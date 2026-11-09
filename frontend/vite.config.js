import {defineConfig} from 'vite';
export default defineConfig({server:{port:5174,strictPort:true,proxy:{'/ws':{target:'ws://localhost:8081',ws:true}}}});
