import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="about" class="about-section">
      <div class="container">
        <div class="hero-content">
          <div class="text-content">
            <h1 class="hero-title">Hello, I'm <span class="highlight">John Doe</span></h1>
            <h2 class="hero-subtitle">Full Stack Developer</h2>
            <p class="hero-description">
              I'm a passionate developer with 5+ years of experience creating 
              beautiful and functional web applications. I specialize in modern 
              JavaScript frameworks and love turning ideas into reality.
            </p>
            <div class="cta-buttons">
              <a href="#projects" class="btn btn-primary">View My Work</a>
              <a href="#contact" class="btn btn-secondary">Get In Touch</a>
            </div>
          </div>
          <div class="image-content">
            <div class="profile-image">
              <img src="assets/images/profile.jpg" alt="Profile" />
            </div>
          </div>
        </div>
        
        <div class="stats">
          <div class="stat-item">
            <h3>50+</h3>
            <p>Projects Completed</p>
          </div>
          <div class="stat-item">
            <h3>5+</h3>
            <p>Years Experience</p>
          </div>
          <div class="stat-item">
            <h3>100+</h3>
            <p>Happy Clients</p>
          </div>
        </div>
      </div>
    </section>
  `,
  styleUrls: ['./about.component.scss']
})
export class AboutComponent {}