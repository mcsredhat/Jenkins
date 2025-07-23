<template>
  <div id="app">
    <header class="app-header">
      <div class="container">
        <div class="logo-section">
          <img src="/vite.svg" alt="Vite Logo" class="logo vite-logo" />
          <img src="/vue.svg" alt="Vue Logo" class="logo vue-logo" />
        </div>
        <h1 class="app-title">{{ appName }}</h1>
        <p class="app-subtitle">{{ appSubtitle }}</p>
      </div>
    </header>

    <main class="app-main">
      <div class="container">
        <div class="feature-grid">
          <FeatureCard
            v-for="feature in features"
            :key="feature.id"
            :title="feature.title"
            :description="feature.description"
            :icon="feature.icon"
            :color="feature.color"
          />
        </div>

        <div class="stats-section">
          <div class="stats-grid">
            <div class="stat-item">
              <h3>{{ stats.uptime }}</h3>
              <p>Uptime</p>
            </div>
            <div class="stat-item">
              <h3>{{ stats.version }}</h3>
              <p>Version</p>
            </div>
            <div class="stat-item">
              <h3>{{ stats.environment }}</h3>
              <p>Environment</p>
            </div>
            <div class="stat-item">
              <h3>{{ stats.status }}</h3>
              <p>Status</p>
            </div>
          </div>
        </div>

        <div class="actions-section">
          <button @click="refreshData" class="btn btn-primary" :disabled="loading">
            {{ loading ? 'Loading...' : 'Refresh Data' }}
          </button>
          <button @click="toggleTheme" class="btn btn-secondary">
            Switch to {{ isDark ? 'Light' : 'Dark' }} Theme
          </button>
        </div>
      </div>
    </main>

    <footer class="app-footer">
      <div class="container">
        <p>&copy; 2025 Vue.js Application. Built with Docker & Nginx.</p>
        <p class="health-status">
          Health Status: 
          <span :class="['status-indicator', healthStatus.toLowerCase()]">
            {{ healthStatus }}
          </span>
        </p>
      </div>
    </footer>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import FeatureCard from './components/FeatureCard.vue'

export default {
  name: 'App',
  components: {
    FeatureCard
  },
  setup() {
    // Reactive data
    const appName = ref('Vue.js Docker App')
    const appSubtitle = ref('A production-ready Vue.js application running in Docker with Nginx')
    const loading = ref(false)
    const isDark = ref(false)
    const healthStatus = ref('Healthy')
    
    const stats = reactive({
      uptime: '0 days',
      version: '1.0.0',
      environment: 'Production',
      status: 'Running'
    })

    const features = ref([
      {
        id: 1,
        title: 'Multi-Stage Build',
        description: 'Optimized Docker build with 4 distinct stages for maximum efficiency',
        icon: '🏗️',
        color: 'blue'
      },
      {
        id: 2,
        title: 'Production Ready',
        description: 'Served with Nginx, includes health checks and security headers',
        icon: '🚀',
        color: 'green'
      },
      {
        id: 3,
        title: 'Security First',
        description: 'Non-root user, minimal attack surface, and security best practices',
        icon: '🔒',
        color: 'red'
      },
      {
        id: 4,
        title: 'Performance',
        description: 'Gzip compression, caching, and optimized static asset delivery',
        icon: '⚡',
        color: 'yellow'
      }
    ])

    // Computed properties
    const currentTheme = computed(() => isDark.value ? 'dark' : 'light')

    // Methods
    const refreshData = async () => {
      loading.value = true
      try {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // Update stats
        const now = new Date()
        stats.uptime = Math.floor((now - new Date('2025-01-01')) / (1000 * 60 * 60 * 24)) + ' days'
        stats.status = 'Running'
        healthStatus.value = 'Healthy'
        
        console.log('Data refreshed successfully')
      } catch (error) {
        console.error('Failed to refresh data:', error)
        healthStatus.value = 'Error'
      } finally {
        loading.value = false
      }
    }

    const toggleTheme = () => {
      isDark.value = !isDark.value
      document.documentElement.setAttribute('data-theme', currentTheme.value)
    }

    const checkHealth = async () => {
      try {
        const response = await fetch('/health')
        if (response.ok) {
          healthStatus.value = 'Healthy'
        } else {
          healthStatus.value = 'Warning'
        }
      } catch (error) {
        console.error('Health check failed:', error)
        healthStatus.value = 'Error'
      }
    }

    // Lifecycle hooks
    onMounted(() => {
      console.log('Vue App mounted successfully')
      refreshData()
      checkHealth()
      
      // Set up periodic health checks
      setInterval(checkHealth, 30000) // Check every 30 seconds
    })

    return {
      appName,
      appSubtitle,
      loading,
      isDark,
      healthStatus,
      stats,
      features,
      currentTheme,
      refreshData,
      toggleTheme
    }
  }
}
</script>

<style scoped>
.app-header {
  background: linear-gradient(135deg, #41b883 0%, #35495e 100%);
  color: white;
  padding: 3rem 1rem;
  text-align: center;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.logo-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.logo {
  height: 3rem;
  width: 3rem;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.1) rotate(5deg);
}

.vue-logo {
  filter: drop-shadow(0 0 10px rgba(65, 184, 131, 0.3));
}

.vite-logo {
  filter: drop-shadow(0 0 10px rgba(100, 108, 255, 0.3));
}

.app-title {
  font-size: 2.5rem;
  margin: 0 0 0.5rem 0;
  font-weight: 700;
}

.app-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
  max-width: 600px;
  margin: 0 auto;
}

.app-main {
  padding: 4rem 1rem;
  min-height: calc(100vh - 300px);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
  margin-bottom: 4rem;
}

.stats-section {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 2rem;
  margin-bottom: 3rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 2rem;
}

.stat-item {
  text-align: center;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.stat-item h3 {
  font-size: 1.8rem;
  color: #41b883;
  margin: 0 0 0.5rem 0;
}

.stat-item p {
  color: #666;
  margin: 0;
  font-weight: 500;
}

.actions-section {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 5px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
}

.btn-primary {
  background: #41b883;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #369870;
  transform: translateY(-2px);
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background: #35495e;
  color: white;
}

.btn-secondary:hover {
  background: #2c3e50;
  transform: translateY(-2px);
}

.app-footer {
  background: #2c3e50;
  color: white;
  padding: 2rem 1rem;
  text-align: center;
}

.app-footer p {
  margin: 0.5rem 0;
}

.health-status {
  font-size: 0.9rem;
}

.status-indicator {
  font-weight: bold;
  padding: 0.2rem 0.5rem;
  border-radius: 3px;
  font-size: 0.8rem;
}

.status-indicator.healthy {
  background: #d4edda;
  color: #155724;
}

.status-indicator.warning {
  background: #fff3cd;
  color: #856404;
}

.status-indicator.error {
  background: #f8d7da;
  color: #721c24;
}

/* Dark theme styles */
[data-theme="dark"] {
  background: #1a1a1a;
  color: #e0e0e0;
}

[data-theme="dark"] .stats-section {
  background: #2d2d2d;
}

[data-theme="dark"] .stat-item {
  background: #3d3d3d;
  color: #e0e0e0;
}

[data-theme="dark"] .stat-item p {
  color: #b0b0b0;
}

/* Responsive design */
@media (max-width: 768px) {
  .app-title {
    font-size: 2rem;
  }
  
  .app-subtitle {
    font-size: 1rem;
  }
  
  .feature-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .actions-section {
    flex-direction: column;
    align-items: center;
  }
  
  .btn {
    width: 100%;
    max-width: 300px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>