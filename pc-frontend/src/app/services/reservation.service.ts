import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private base = `${environment.apiBaseUrl}/api/reservations`;

  constructor(private http: HttpClient) {}

  // POST body matches ReservationRequestDto: userId, pharmacyId, medicineId, quantity
  // Response: ReservationResponseDto — id, status, quantity, holdAt, expiresAt, medicineName, pharmacyName, pharmacyId, medicineId, userId
  create(userId: string, pharmacyId: string, medicineId: string, quantity: number): Observable<any> {
    return this.http.post<any>(this.base, { userId, pharmacyId, medicineId, quantity });
  }

  // Returns ReservationResponseDto[]
  getUserReservations(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/user/${userId}`);
  }

  // Returns updated ReservationResponseDto with status CANCELLED
  cancel(reservationId: string): Observable<any> {
    return this.http.patch<any>(`${this.base}/${reservationId}/cancel`, {});
  }

  // Returns ReservationResponseDto[]
  getPharmacyReservations(pharmacyId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/pharmacy/${pharmacyId}`);
  }

  // Returns updated ReservationResponseDto with status CLAIMED
  claim(reservationId: string): Observable<any> {
    return this.http.patch<any>(`${this.base}/${reservationId}/claim`, {});
  }
}
