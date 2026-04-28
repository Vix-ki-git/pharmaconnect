import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private base = 'http://localhost:8082/api/inventory';

  constructor(private http: HttpClient) {}

  // InventoryResponseDTO: stockId, medicineName, genericName, manufacturer, quantity, price, lastUpdated
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

  // addItem body: { pharmacy: { id }, medicine: { id }, quantity, price }
  addItem(stock: { pharmacy: { id: string }; medicine: { id: string }; quantity: number; price: number }): Observable<any> {
    return this.http.post<any>(`${this.base}/add`, stock);
  }

  bulkUpload(pharmacyId: string, file: File): Observable<string> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post(`http://localhost:8082/api/auth/seller/inventory/bulk-upload-csv/${pharmacyId}`, form, {
      responseType: 'text'
    });
  }
}
