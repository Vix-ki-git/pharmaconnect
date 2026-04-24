import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private base = 'http://localhost:8081/api/search';

  constructor(private http: HttpClient) {}

  // Returns MedicineSearchResult[] — flat projection with stockId, pharmacyId, medicineId, distance
  searchClosest(keyword: string, lat: number, lng: number, userId?: string | null): Observable<any[]> {
    let params = new HttpParams().set('keyword', keyword).set('lat', lat).set('lng', lng);
    if (userId) params = params.set('userId', userId);
    return this.http.get<any[]>(`${this.base}/closest`, { params });
  }

  // Returns MedicineSearchResult[] — 24/7 pharmacies only, sorted by distance
  searchEmergency(keyword: string, lat: number, lng: number, userId?: string | null): Observable<any[]> {
    let params = new HttpParams().set('keyword', keyword).set('lat', lat).set('lng', lng);
    if (userId) params = params.set('userId', userId);
    return this.http.get<any[]>(`${this.base}/emergency`, { params });
  }

  // Returns PharmacyStock[] — full entity with nested pharmacy{id,name,address} and medicine{id,name,genericName}
  searchKeyword(keyword: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/keyword`, { params: new HttpParams().set('keyword', keyword) });
  }

  // Returns MedicineSearchResult[] — GPS + radius bounded
  searchFilter(keyword: string, lat: number, lng: number, radius?: number): Observable<any[]> {
    let params = new HttpParams().set('keyword', keyword).set('lat', lat).set('lng', lng);
    if (radius) params = params.set('radius', radius);
    return this.http.get<any[]>(`${this.base}/filter`, { params });
  }
}
