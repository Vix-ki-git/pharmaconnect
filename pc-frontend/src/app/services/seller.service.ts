import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SellerService {
  private base = 'http://localhost:8082/api/seller-portal';

  constructor(private http: HttpClient) {}

  // SellerPortalDashboardResponseDto: pharmacyId, pharmacyName, isPharmacyVerified, isPharmacyActive,
  //   portalAccessMessage, totalStockItems, lowStockItems, outOfStockItems, activeReservationsCount
  getDashboard(sellerEmail: string): Observable<any> {
    return this.http.get<any>(`${this.base}/my-dashboard/${encodeURIComponent(sellerEmail)}`);
  }

  // DemandAnalyticsResponseDto[]: medicineId, medicineName, genericName, searchCount, reservationCount, periodDate
  getAnalytics(pharmacyId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/${pharmacyId}/analytics`);
  }

  uploadLicense(pharmacyId: string, formData: FormData): Observable<any> {
    return this.http.post<any>(`http://localhost:8082/api/pharmacies/${pharmacyId}/upload-license`, formData);
  }
}
