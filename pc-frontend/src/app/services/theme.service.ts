import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  isDark = false;

  constructor() {
    this.isDark = localStorage.getItem('theme') === 'dark';
    document.body.classList.toggle('dark-theme', this.isDark);
  }

  toggle() {
    this.isDark = !this.isDark;
    document.body.classList.toggle('dark-theme', this.isDark);
    localStorage.setItem('theme', this.isDark ? 'dark' : 'light');
  }
}
