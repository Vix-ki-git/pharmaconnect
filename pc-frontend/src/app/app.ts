import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LandingPage } from './components/landing-page/landing-page'; // Match your file/class name

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, LandingPage], // Use LandingPage here
  templateUrl: './app.html', // Make sure this matches your filename too!
  styleUrl: './app.css'
})
export class App {
  // Your main app class
}
