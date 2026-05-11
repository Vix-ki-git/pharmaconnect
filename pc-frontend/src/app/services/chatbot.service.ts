import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ChatReply {
  reply: string;
}

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private base = `${environment.apiBaseUrl}/api/chatbot`;

  constructor(private http: HttpClient) {}

  ask(message: string): Observable<ChatReply> {
    return this.http.post<ChatReply>(`${this.base}/ask`, { message });
  }
}
