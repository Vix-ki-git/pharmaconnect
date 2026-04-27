import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private base = 'http://localhost:8082/api/search';

  constructor(private http: HttpClient) {}

  // Returns MedicineSearchResult[] — flat: stockId, pharmacyId, medicineId, pharmacyName, pharmacyAddress, medicineName, genericName, quantity, price, distance
  searchClosest(keyword: string, lat: number, lng: number, userId?: string | null): Observable<any[]> {
    let params = new HttpParams().set('keyword', keyword).set('lat', lat).set('lng', lng);
    if (userId) params = params.set('userId', userId);
    return this.http.get<any[]>(`${this.base}/closest`, { params });
  }

  // Returns MedicineSearchResult[] — same as closest but 24/7 pharmacies only
  searchEmergency(keyword: string, lat: number, lng: number, userId?: string | null): Observable<any[]> {
    let params = new HttpParams().set('keyword', keyword).set('lat', lat).set('lng', lng);
    if (userId) params = params.set('userId', userId);
    return this.http.get<any[]>(`${this.base}/emergency`, { params });
  }

  // Returns PharmacyStock[] — nested: id, pharmacy{id,name,address}, medicine{id,name,genericName}, quantity, price
  searchKeyword(keyword: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/keyword`, { params: new HttpParams().set('keyword', keyword) });
  }

  // Returns MedicineSearchResult[] filtered by radius (km)
  filterByRadius(keyword: string, lat: number, lng: number, radius: number, userId?: string | null): Observable<any[]> {
    let params = new HttpParams().set('keyword', keyword).set('lat', lat).set('lng', lng).set('radius', radius);
    if (userId) params = params.set('userId', userId);
    return this.http.get<any[]>(`${this.base}/filter`, { params });
  }

  // Returns PriceComparisonDto[]: pharmacyName, pharmacyAddress, price, quantity, distance
  getPricesByMedicine(medicineId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/medicine/${medicineId}/prices`);
  }

  // Returns MedicineAlternativeDto[]: brandMedicine, alternativeMedicine, equivalenceNote
  getAlternatives(brandName: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/alternatives`, { params: new HttpParams().set('brandName', brandName) });
  }

  // Returns MedicineCompareResponseDto: medicine, alternatives[], pharmacies[]
  compareMedicine(medicineId: string): Observable<any> {
    return this.http.get<any>(`http://localhost:8082/api/auth/medicines/${medicineId}/compare`);
  }
}
