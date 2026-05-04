import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private base = `${environment.apiBaseUrl}/api/inventory`;

  constructor(private http: HttpClient) {}

  // InventoryResponseDTO: stockId, medicineName, genericName, manufacturer, quantity, price,
  //   lastUpdated, manufacturingDate, expiryDate
  getByPharmacy(pharmacyId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/pharmacy/${pharmacyId}`);
  }

  updateQuantity(stockId: string, quantity: number): Observable<any> {
    return this.http.patch<any>(`${this.base}/${stockId}/quantity`, null, {
      params: new HttpParams().set('quantity', quantity)
    });
  }

  updatePrice(stockId: string, price: number): Observable<any> {
    return this.http.patch<any>(`${this.base}/${stockId}/price`, null, {
      params: new HttpParams().set('price', price)
    });
  }

  adjustQuantity(stockId: string, delta: number): Observable<any> {
    return this.http.patch<any>(`${this.base}/${stockId}/adjust-quantity`, null, {
      params: new HttpParams().set('delta', delta)
    });
  }

  deleteItem(stockId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${stockId}`);
  }

  // addItem body: { pharmacy: { id }, medicine: { id }, quantity, price, manufacturingDate?, expiryDate? }
  // Dates are ISO yyyy-MM-dd strings; backend deserializes into LocalDate.
  addItem(stock: {
    pharmacy: { id: string };
    medicine: { id: string };
    quantity: number;
    price: number;
    manufacturingDate?: string | null;
    expiryDate?: string | null;
  }): Observable<any> {
    return this.http.post<any>(`${this.base}/add`, stock);
  }

  bulkUpload(pharmacyId: string, file: File): Observable<string> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post(`${environment.apiBaseUrl}/api/auth/seller/inventory/bulk-upload-csv/${pharmacyId}`, form, {
      responseType: 'text'
    });
  }
}
