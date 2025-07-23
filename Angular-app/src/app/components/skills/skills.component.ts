import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Skill {
  name: string;
  level: number;
  category: string;
}

@Component({
  selector: 'app-skills',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="skills" class="skills-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Skills & Expertise</h2>
          <p class="section-subtitle">Technologies and tools I work with</p>
        </div>
        
        <div class="skills-container">
          <div class="skills-category" *ngFor="let category of skillCategories">
            <h3 class="category-title">{{ category }}</h3>
            <div class="skills-grid">
              <div class="skill-item" *ngFor="let skill of getSkillsByCategory(category)">
                <div class="skill-info">
                  <span class="skill-name">{{ skill.name }}</span>
                  <span class="skill-percentage">{{ skill.level }}%</span>
                </div>
                <div class="skill-bar">
                  <div class="skill-progress" [style.width.%]="skill.level"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="tools-section">
          <h3 class="tools-title">Tools & Technologies</h3>
          <div class="tools-grid">
            <div class="tool-item" *ngFor="let tool of tools">
              <div class="tool-icon">
                <img [src]="'assets/icons/' + tool.toLowerCase() + '.svg'" [alt]="tool" />
              </div>
              <span class="tool-name">{{ tool }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  `,
  styleUrls: ['./skills.component.scss']
})
export class SkillsComponent {
  skills: Skill[] = [
    { name: 'JavaScript', level: 95, category: 'Frontend' },
    { name: 'TypeScript', level: 90, category: 'Frontend' },
    { name: 'Angular', level: 92, category: 'Frontend' },
    { name: 'React', level: 88, category: 'Frontend' },
    { name: 'Vue.js', level: 85, category: 'Frontend' },
    { name: 'HTML/CSS', level: 95, category: 'Frontend' },
    { name: 'SCSS/Sass', level: 90, category: 'Frontend' },
    
    { name: 'Node.js', level: 88, category: 'Backend' },
    { name: 'Express.js', level: 85, category: 'Backend' },
    { name: 'Python', level: 80, category: 'Backend' },
    { name: 'MongoDB', level: 82, category: 'Backend' },
    { name: 'PostgreSQL', level: 78, category: 'Backend' },
    { name: 'REST APIs', level: 90, category: 'Backend' },
    
    { name: 'Git', level: 92, category: 'Tools' },
    { name: 'Docker', level: 75, category: 'Tools' },
    { name: 'AWS', level: 70, category: 'Tools' },
    { name: 'CI/CD', level: 72, category: 'Tools' }
  ];

  tools: string[] = [
    'VSCode', 'Git', 'Docker', 'AWS', 'Firebase', 'Figma', 
    'Postman', 'Jest', 'Webpack', 'Nginx', 'Linux', 'Jira'
  ];

  skillCategories: string[] = ['Frontend', 'Backend', 'Tools'];

  getSkillsByCategory(category: string): Skill[] {
    return this.skills.filter(skill => skill.category === category);
  }
}