import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Project {
  id: number;
  title: string;
  description: string;
  technologies: string[];
  image: string;
  liveUrl?: string;
  githubUrl?: string;
}

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="projects" class="projects-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">My Projects</h2>
          <p class="section-subtitle">Here are some of my recent works</p>
        </div>
        
        <div class="projects-grid">
          <div class="project-card" *ngFor="let project of projects">
            <div class="project-image">
              <img [src]="project.image" [alt]="project.title" />
              <div class="project-overlay">
                <div class="project-links">
                  <a [href]="project.liveUrl" *ngIf="project.liveUrl" target="_blank" class="project-link">
                    <span>Live Demo</span>
                  </a>
                  <a [href]="project.githubUrl" *ngIf="project.githubUrl" target="_blank" class="project-link">
                    <span>GitHub</span>
                  </a>
                </div>
              </div>
            </div>
            <div class="project-content">
              <h3 class="project-title">{{ project.title }}</h3>
              <p class="project-description">{{ project.description }}</p>
              <div class="project-technologies">
                <span class="tech-tag" *ngFor="let tech of project.technologies">{{ tech }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  `,
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent {
  projects: Project[] = [
    {
      id: 1,
      title: 'E-Commerce Platform',
      description: 'A full-stack e-commerce solution with payment integration, user authentication, and admin dashboard.',
      technologies: ['Angular', 'Node.js', 'MongoDB', 'Stripe'],
      image: 'assets/images/project1.jpg',
      liveUrl: 'https://example.com',
      githubUrl: 'https://github.com/username/project1'
    },
    {
      id: 2,
      title: 'Task Management App',
      description: 'A collaborative task management application with real-time updates and team collaboration features.',
      technologies: ['React', 'Firebase', 'Material-UI', 'WebSocket'],
      image: 'assets/images/project2.jpg',
      liveUrl: 'https://example.com',
      githubUrl: 'https://github.com/username/project2'
    },
    {
      id: 3,
      title: 'Weather Dashboard',
      description: 'A responsive weather application with location-based forecasts and interactive charts.',
      technologies: ['Vue.js', 'Chart.js', 'OpenWeather API', 'PWA'],
      image: 'assets/images/project3.jpg',
      liveUrl: 'https://example.com',
      githubUrl: 'https://github.com/username/project3'
    },
    {
      id: 4,
      title: 'Portfolio Website',
      description: 'A modern, responsive portfolio website built with Angular and featuring smooth animations.',
      technologies: ['Angular', 'SCSS', 'TypeScript', 'Nginx'],
      image: 'assets/images/project4.jpg',
      liveUrl: 'https://example.com',
      githubUrl: 'https://github.com/username/project4'
    }
  ];
}