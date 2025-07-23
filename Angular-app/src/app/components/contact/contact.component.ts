import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface ContactForm {
  name: string;
  email: string;
  subject: string;
  message: string;
}

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section id="contact" class="contact-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Get In Touch</h2>
          <p class="section-subtitle">Let's work together on your next project</p>
        </div>

        <div class="contact-content">
          <div class="contact-info">
            <h3>Let's Connect</h3>
            <p>I'm always interested in new opportunities and exciting projects. Feel free to reach out!</p>
            
            <div class="contact-items">
              <div class="contact-item">
                <div class="contact-icon">📧</div>
                <div class="contact-details">
                  <h4>Email</h4>
                  <p>john.doe&#64;gmail.com</p>
                </div>
              </div>
              
              <div class="contact-item">
                <div class="contact-icon">📱</div>
                <div class="contact-details">
                  <h4>Phone</h4>
                  <p>+1 (555) 123-4567</p>
                </div>
              </div>
              
              <div class="contact-item">
                <div class="contact-icon">📍</div>
                <div class="contact-details">
                  <h4>Location</h4>
                  <p>New York, USA</p>
                </div>
              </div>
            </div>

            <div class="social-links">
              <a href="https://github.com" target="_blank" class="social-link">GitHub</a>
              <a href="https://linkedin.com" target="_blank" class="social-link">LinkedIn</a>
              <a href="https://twitter.com" target="_blank" class="social-link">Twitter</a>
            </div>
          </div>

          <form class="contact-form" (ngSubmit)="onSubmit()" #contactForm="ngForm">
            <div class="form-group">
              <label for="name">Name *</label>
              <input 
                type="text" 
                id="name" 
                name="name"
                [(ngModel)]="formData.name"
                required
                #name="ngModel"
                class="form-control"
                [class.error]="name.invalid && name.touched"
              />
              <div class="error-message" *ngIf="name.invalid && name.touched">
                Name is required
              </div>
            </div>

            <div class="form-group">
              <label for="email">Email *</label>
              <input 
                type="email" 
                id="email" 
                name="email"
                [(ngModel)]="formData.email"
                required
                email
                #email="ngModel"
                class="form-control"
                [class.error]="email.invalid && email.touched"
              />
              <div class="error-message" *ngIf="email.invalid && email.touched">
                <span *ngIf="email.errors?.['required']">Email is required</span>
                <span *ngIf="email.errors?.['email']">Please enter a valid email</span>
              </div>
            </div>

            <div class="form-group">
              <label for="subject">Subject *</label>
              <input 
                type="text" 
                id="subject" 
                name="subject"
                [(ngModel)]="formData.subject"
                required
                #subject="ngModel"
                class="form-control"
                [class.error]="subject.invalid && subject.touched"
              />
              <div class="error-message" *ngIf="subject.invalid && subject.touched">
                Subject is required
              </div>
            </div>

            <div class="form-group">
              <label for="message">Message *</label>
              <textarea 
                id="message" 
                name="message"
                [(ngModel)]="formData.message"
                required
                #message="ngModel"
                rows="5"
                class="form-control"
                [class.error]="message.invalid && message.touched"
              ></textarea>
              <div class="error-message" *ngIf="message.invalid && message.touched">
                Message is required
              </div>
            </div>

            <button 
              type="submit" 
              class="submit-btn"
              [disabled]="contactForm.invalid || isSubmitting"
            >
              <span *ngIf="!isSubmitting">Send Message</span>
              <span *ngIf="isSubmitting">Sending...</span>
            </button>

            <div class="success-message" *ngIf="showSuccessMessage">
              Thank you! Your message has been sent successfully.
            </div>
          </form>
        </div>
      </div>
    </section>
  `,
  styleUrls: ['./contact.component.scss']
})
export class ContactComponent {
  formData: ContactForm = {
    name: '',
    email: '',
    subject: '',
    message: ''
  };

  isSubmitting = false;
  showSuccessMessage = false;

  onSubmit() {
    if (this.isSubmitting) return;

    this.isSubmitting = true;

    // Simulate form submission
    setTimeout(() => {
      this.isSubmitting = false;
      this.showSuccessMessage = true;
      this.resetForm();

      // Hide success message after 5 seconds
      setTimeout(() => {
        this.showSuccessMessage = false;
      }, 5000);
    }, 2000);
  }

  resetForm() {
    this.formData = {
      name: '',
      email: '',
      subject: '',
      message: ''
    };
  }
}