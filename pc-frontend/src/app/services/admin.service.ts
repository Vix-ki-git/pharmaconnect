import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private sellerBase = 'http://localhost:8082/api/auth/admin/sellers';
  private medBase = 'http://localhost:8082/api/admin/medicines';

  constructor(private http: HttpClient) {}

  // PendingSellerApplicationDto: pharmacyId, pharmacyName, pharmacyAddress, pharmacyPhone, is247,
  //   registeredAt, ownerName, ownerEmail, ownerPhone, documents[], overallStatus
  getPendingSellers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.sellerBase}/pending`);
  }

  verifySeller(pharmacyId: string): Observable<any> {
    return this.http.patch<any>(`${this.sellerBase}/${pharmacyId}/verify`, {});
  }

  rejectSeller(pharmacyId: string): Observable<any> {
    return this.http.patch<any>(`${this.sellerBase}/${pharmacyId}/reject`, {});
  }

  // Medicine: id, name, genericName, category, manufacturer, dosageForm, strength
  getAllMedicines(): Observable<any[]> {
    return this.http.get<any[]>(this.medBase);
  }

  addMedicine(dto: { name: string; genericName: string; category: string; manufacturer: string; dosageForm: string; strength: string }): Observable<any> {
    return this.http.post<any>(this.medBase, dto);
  }

  // MedicineAlternativeLinkDto: medicineId, alternativeId, equivalenceNote
  linkAlternatives(dto: { medicineId: string; alternativeId: string; equivalenceNote: string }): Observable<any> {
    return this.http.post<any>(`${this.medBase}/link-alternative`, dto);
  }

  // DocumentReviewDto[]: id, pharmacyId, pharmacyName, documentType, fileUrl, status, uploadedAt
  getPendingDocuments(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8082/api/auth/admin/documents/pending');
  }

  updateDocumentStatus(documentId: string, status: 'APPROVED' | 'REJECTED'): Observable<any> {
    return this.http.patch<any>(`http://localhost:8082/api/auth/admin/documents/${documentId}/status`, { status });
  }
}
