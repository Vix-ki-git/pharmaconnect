import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; // 1. Add this import
@Component({
  selector: 'app-landing-page',
  imports: [CommonModule],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.css',
})

export class LandingPage {
  isDarkMode = false;

    toggleTheme() {
        this.isDarkMode = !this.isDarkMode;
        document.body.classList.toggle('dark-theme');
      }
}
