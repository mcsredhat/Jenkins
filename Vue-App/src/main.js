import { createApp } from 'vue'
import App from './App.vue'
import './style.css'

const app = createApp(App)

// Global error handler
app.config.errorHandler = (err, vm, info) => {
  console.error('Vue Error:', err)
  console.error('Component:', vm)
  console.error('Info:', info)
}

// Global warning handler
app.config.warnHandler = (msg, vm, trace) => {
  console.warn('Vue Warning:', msg)
  console.warn('Component:', vm)
  console.warn('Trace:', trace)
}

// Mount the app
app.mount('#app')

// Log application information
console.log('Vue.js Application Started')
console.log('Environment:', import.meta.env.MODE)
console.log('Base URL:', import.meta.env.BASE_URL)