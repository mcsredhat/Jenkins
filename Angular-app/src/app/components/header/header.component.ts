import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header class="header" [class.scrolled]="isScrolled">
      <nav class="nav">
        <div class="logo">
          <h1>Portfolio</h1>
        </div>
        <div class="nav-links" [class.mobile-open]="isMobileMenuOpen">
          <a href="#about" (click)="closeMenu()">About</a>
          <a href="#projects" (click)="closeMenu()">Projects</a>
          <a href="#skills" (click)="closeMenu()">Skills</a>
          <a href="#contact" (click)="closeMenu()">Contact</a>
        </div>
        <button class="mobile-toggle" (click)="toggleMobileMenu()">
          <span></span>
          <span></span>
          <span></span>
        </button>
      </nav>
    </header>
  `,
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  isScrolled = false;
  isMobileMenuOpen = false;

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.isScrolled = window.scrollY > 50;
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMenu() {
    this.isMobileMenuOpen = false;
  }
}