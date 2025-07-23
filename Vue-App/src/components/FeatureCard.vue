<template>
  <div :class="['feature-card', `feature-card--${color}`]" @click="handleClick">
    <div class="feature-icon">
      {{ icon }}
    </div>
    <div class="feature-content">
      <h3 class="feature-title">{{ title }}</h3>
      <p class="feature-description">{{ description }}</p>
    </div>
    <div class="feature-overlay"></div>
  </div>
</template>

<script>
export default {
  name: 'FeatureCard',
  props: {
    title: {
      type: String,
      required: true
    },
    description: {
      type: String,
      required: true
    },
    icon: {
      type: String,
      default: '📦'
    },
    color: {
      type: String,
      default: 'blue',
      validator: (value) => ['blue', 'green', 'red', 'yellow', 'purple', 'orange'].includes(value)
    }
  },
  emits: ['click'],
  methods: {
    handleClick() {
      this.$emit('click', {
        title: this.title,
        description: this.description,
        icon: this.icon,
        color: this.color
      })
      console.log(`Feature card clicked: ${this.title}`)
    }
  }
}
</script>

<style scoped>
.feature-card {
  position: relative;
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  overflow: hidden;
  border: 2px solid transparent;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.feature-card:hover .feature-overlay {
  opacity: 1;
}

.feature-card:hover .feature-icon {
  transform: scale(1.1) rotate(5deg);
}

.feature-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  display: block;
  text-align: center;
  transition: transform 0.3s ease;
  filter: drop-shadow(0 2px 10px rgba(0, 0, 0, 0.1));
}

.feature-content {
  position: relative;
  z-index: 2;
}

.feature-title {
  font-size: 1.3rem;
  font-weight: 700;
  margin: 0 0 1rem 0;
  color: #2c3e50;
  text-align: center;
}

.feature-description {
  color: #666;
  line-height: 1.6;
  margin: 0;
  text-align: center;
}

/* Color variants */
.feature-card--blue {
  border-left: 4px solid #3498db;
}

.feature-card--blue:hover {
  border-color: #3498db;
}

.feature-card--blue .feature-overlay {
  background: linear-gradient(135deg, rgba(52, 152, 219, 0.1) 0%, rgba(52, 152, 219, 0.05) 100%);
}

.feature-card--green {
  border-left: 4px solid #2ecc71;
}

.feature-card--green:hover {
  border-color: #2ecc71;
}

.feature-card--green .feature-overlay {
  background: linear-gradient(135deg, rgba(46, 204, 113, 0.1) 0%, rgba(46, 204, 113, 0.05) 100%);
}

.feature-card--red {
  border-left: 4px solid #e74c3c;
}

.feature-card--red:hover {
  border-color: #e74c3c;
}

.feature-card--red .feature-overlay {
  background: linear-gradient(135deg, rgba(231, 76, 60, 0.1) 0%, rgba(231, 76, 60, 0.05) 100%);
}

.feature-card--yellow {
  border-left: 4px solid #f39c12;
}

.feature-card--yellow:hover {
  border-color: #f39c12;
}

.feature-card--yellow .feature-overlay {
  background: linear-gradient(135deg, rgba(243, 156, 18, 0.1) 0%, rgba(243, 156, 18, 0.05) 100%);
}

.feature-card--purple {
  border-left: 4px solid #9b59b6;
}

.feature-card--purple:hover {
  border-color: #9b59b6;
}

.feature-card--purple .feature-overlay {
  background: linear-gradient(135deg, rgba(155, 89, 182, 0.1) 0%, rgba(155, 89, 182, 0.05) 100%);
}

.feature-card--orange {
  border-left: 4px solid #e67e22;
}

.feature-card--orange:hover {
  border-color: #e67e22;
}

.feature-card--orange .feature-overlay {
  background: linear-gradient(135deg, rgba(230, 126, 34, 0.1) 0%, rgba(230, 126, 34, 0.05) 100%);
}

/* Dark theme support */
[data-theme="dark"] .feature-card {
  background: #3d3d3d;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .feature-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4);
}

[data-theme="dark"] .feature-title {
  color: #e0e0e0;
}

[data-theme="dark"] .feature-description {
  color: #b0b0b0;
}

/* Responsive design */
@media (max-width: 768px) {
  .feature-card {
    padding: 1.5rem;
  }
  
  .feature-icon {
    font-size: 2.5rem;
    margin-bottom: 0.8rem;
  }
  
  .feature-title {
    font-size: 1.2rem;
    margin-bottom: 0.8rem;
  }
  
  .feature-description {
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .feature-card {
    padding: 1rem;
  }
  
  .feature-icon {
    font-size: 2rem;
  }
  
  .feature-title {
    font-size: 1.1rem;
  }
}</style>