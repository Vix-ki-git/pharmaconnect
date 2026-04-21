import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {

  // Simple login check (replace with real API call later)
  login(email: string, password: string): boolean {
    console.log('Logging in:', email);
    return email !== '' && password !== '';
  }

  // Simple register (replace with real API call later)
  register(name: string, email: string, password: string): boolean {
    console.log('Registering:', name, email);
    return name !== '' && email !== '' && password !== '';
  }
}
