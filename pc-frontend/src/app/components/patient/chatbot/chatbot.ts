import { Component, ElementRef, ViewChild, ViewChildren, QueryList, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import {
  marked } from 'marked';
import { AuthService } from '../../../services/auth.service';
import { ChatbotService } from '../../../services/chatbot.service';

interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
}

type ScrollMode = 'none' | 'bottom' | 'lastTop';

marked.setOptions({ gfm: true, breaks: true, async: false });

@Component({
  selector: 'app-chatbot',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './chatbot.html',
  styleUrl: './chatbot.css'
})
export class Chatbot implements AfterViewChecked {
  @ViewChild('scrollAnchor') scrollAnchor?: ElementRef<HTMLDivElement>;
  @ViewChildren('messageEl') messageEls?: QueryList<ElementRef<HTMLDivElement>>;

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

  private scrollMode: ScrollMode = 'none';

  constructor(
    private authService: AuthService,
    private chatbotService: ChatbotService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  renderMarkdown(text: string): string {
    return marked.parse(text) as string;
  }

  ngAfterViewChecked() {
    if (this.scrollMode === 'bottom' && this.scrollAnchor) {
      this.scrollAnchor.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'end' });
      this.scrollMode = 'none';
    } else if (this.scrollMode === 'lastTop' && this.messageEls && this.messageEls.length > 0) {
      const last = this.messageEls.last;
      last.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
      this.scrollMode = 'none';
    }
  }

  send() {
    const msg = this.input.trim();
    if (!msg || this.loading) return;

    this.messages.push({ role: 'user', text: msg });
    this.input = '';
    this.loading = true;
    this.scrollMode = 'bottom';

    this.chatbotService.ask(msg).subscribe({
      next: (res) => {
        this.messages.push({ role: 'assistant', text: res.reply });
        this.loading = false;
        this.scrollMode = 'lastTop';
      },
      error: () => {
        this.messages.push({
          role: 'assistant',
          text: 'Sorry, I couldn’t reach the assistant. Please try again in a moment.'
        });
        this.loading = false;
        this.scrollMode = 'lastTop';
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
