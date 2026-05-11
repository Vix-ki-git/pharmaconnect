import { Component, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ChatbotService } from '../../../services/chatbot.service';

interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
}

@Component({
  selector: 'app-chatbot',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './chatbot.html',
  styleUrl: './chatbot.css'
})
export class Chatbot implements AfterViewChecked {
  @ViewChild('scrollAnchor') scrollAnchor?: ElementRef<HTMLDivElement>;

  sidebarOpen = false;
  user: any;
  input = '';
  loading = false;
  messages: ChatMessage[] = [
    {
      role: 'assistant',
      text: 'Hi! I can help you understand medicines on PharmaConnect — uses, common dosage forms, side effects, generics, storage. Ask me anything about a medicine. (I won’t diagnose or prescribe.)'
    }
  ];

  private shouldScroll = false;

  constructor(
    private authService: AuthService,
    private chatbotService: ChatbotService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngAfterViewChecked() {
    if (this.shouldScroll && this.scrollAnchor) {
      this.scrollAnchor.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'end' });
      this.shouldScroll = false;
    }
  }

  send() {
    const msg = this.input.trim();
    if (!msg || this.loading) return;

    this.messages.push({ role: 'user', text: msg });
    this.input = '';
    this.loading = true;
    this.shouldScroll = true;

    this.chatbotService.ask(msg).subscribe({
      next: (res) => {
        this.messages.push({ role: 'assistant', text: res.reply });
        this.loading = false;
        this.shouldScroll = true;
      },
      error: () => {
        this.messages.push({
          role: 'assistant',
          text: 'Sorry, I couldn’t reach the assistant. Please try again in a moment.'
        });
        this.loading = false;
        this.shouldScroll = true;
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
