import { Component, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { SearchService } from '../../../services/search.service';
import { ReservationService } from '../../../services/reservation.service';
import { AuthService } from '../../../services/auth.service';
import { ThemeService } from '../../../services/theme.service';

interface ResultCard {
  stockId: string;
  pharmacyId: string;
  medicineId: string;
  medicineName: string;
  genericName: string;
  pharmacyName: string;
  pharmacyAddress: string;
  quantity: number;
  price: number;
  distance?: number;
}

type SortOption = 'default' | 'price-asc' | 'price-desc' | 'distance' | 'stock-desc';
type SearchMode = 'keyword' | 'nearest' | 'emergency';

@Component({
  selector: 'app-search',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './search.html',
  styleUrl: './search.css'
})
export class SearchPage implements OnDestroy {
  keyword = '';
  mode: SearchMode = 'keyword';
  radius = 10;

  rawResults: ResultCard[] = [];
  results: ResultCard[] = [];
  sortBy: SortOption = 'default';

  loading = false;
  searched = false;
  sidebarOpen = false;

  reservingId: string | null = null;
  reserveQty = 1;
  reserveLoading = false;

  // Price comparison panel
  priceCompareId: string | null = null;
  priceCompareData: any[] = [];
  priceCompareLoading = false;

  // Generic alternatives panel
  alternativesId: string | null = null;
  alternativesData: any[] = [];
  alternativesLoading = false;

  toast: { msg: string; type: 'success' | 'error' } | null = null;
  private toastTimer: any;

  user: any;

  constructor(
    private searchService: SearchService,
    private reservationService: ReservationService,
    private authService: AuthService,
    public themeService: ThemeService,
    private router: Router,
    private ngZone: NgZone
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngOnDestroy() {
    clearTimeout(this.toastTimer);
  }

  private applySort() {
    const arr = [...this.rawResults];
    switch (this.sortBy) {
      case 'price-asc':  this.results = arr.sort((a, b) => a.price - b.price); break;
      case 'price-desc': this.results = arr.sort((a, b) => b.price - a.price); break;
      case 'distance':   this.results = arr.filter(r => r.distance != null).sort((a, b) => a.distance! - b.distance!); break;
      case 'stock-desc': this.results = arr.sort((a, b) => b.quantity - a.quantity); break;
      default:           this.results = arr;
    }
  }

  setMode(m: SearchMode) {
    this.mode = m;
    this.rawResults = [];
    this.results = [];
    this.searched = false;
    this.reservingId = null;
    this.priceCompareId = null;
    this.alternativesId = null;
    this.clearToast();
    this.sortBy = m !== 'keyword' ? 'distance' : 'default';
  }

  onSortChange() {
    this.applySort();
  }

  search() {
    const kw = this.keyword.trim();
    if (!kw) return;
    this.rawResults = [];
    this.results = [];
    this.searched = false;
    this.reservingId = null;
    this.priceCompareId = null;
    this.alternativesId = null;
    this.clearToast();
    this.sortBy = this.mode !== 'keyword' ? 'distance' : 'default';
    this.mode === 'keyword' ? this.doKeywordSearch(kw) : this.doGpsSearch(kw);
  }

  private doKeywordSearch(kw: string) {
    this.loading = true;
    this.searchService.searchKeyword(kw).subscribe({
      next: (items) => {
        this.rawResults = items.map(item => ({
          stockId: item.id,
          pharmacyId: item.pharmacy?.id ?? '',
          medicineId: item.medicine?.id ?? '',
          medicineName: item.medicine?.name ?? '',
          genericName: item.medicine?.genericName ?? '',
          pharmacyName: item.pharmacy?.name ?? '',
          pharmacyAddress: item.pharmacy?.address ?? '',
          quantity: item.quantity,
          price: item.price
        }));
        this.applySort();
        this.loading = false;
        this.searched = true;
      },
      error: (err) => {
        this.loading = false;
        this.searched = true;
        this.showToast(typeof err.error === 'string' ? err.error : 'Search failed. Please try again.', 'error');
      }
    });
  }

  private doGpsSearch(kw: string) {
    if (!navigator.geolocation) {
      this.showToast('Geolocation is not supported by your browser.', 'error');
      return;
    }
    this.loading = true;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.ngZone.run(() => {
          const lat = pos.coords.latitude;
          const lng = pos.coords.longitude;

          let obs;
          if (this.mode === 'emergency') {
            obs = this.searchService.searchEmergency(kw, lat, lng, this.user?.id);
          } else if (this.radius < 50) {
            // Use radius filter for "nearest" mode when a custom radius is set
            obs = this.searchService.filterByRadius(kw, lat, lng, this.radius, this.user?.id);
          } else {
            obs = this.searchService.searchClosest(kw, lat, lng, this.user?.id);
          }

          obs.subscribe({
            next: (items) => {
              this.rawResults = items.map(item => ({
                stockId: item.stockId,
                pharmacyId: item.pharmacyId,
                medicineId: item.medicineId,
                medicineName: item.medicineName,
                genericName: item.genericName ?? '',
                pharmacyName: item.pharmacyName,
                pharmacyAddress: item.pharmacyAddress,
                quantity: item.quantity,
                price: item.price,
                distance: item.distance
              }));
              this.applySort();
              this.loading = false;
              this.searched = true;
            },
            error: (err) => {
              this.loading = false;
              this.searched = true;
              this.showToast(typeof err.error === 'string' ? err.error : 'Search failed. Please try again.', 'error');
            }
          });
        });
      },
      () => {
        this.ngZone.run(() => {
          this.loading = false;
          this.showToast('Location access denied. Allow location access or switch to Keyword mode.', 'error');
        });
      },
      { timeout: 5000, maximumAge: 30000, enableHighAccuracy: false }
    );
  }

  // Price comparison — toggled per card
  togglePriceCompare(card: ResultCard) {
    if (this.priceCompareId === card.stockId) {
      this.priceCompareId = null;
      return;
    }
    this.priceCompareId = card.stockId;
    this.priceCompareData = [];
    this.priceCompareLoading = true;
    this.searchService.getPricesByMedicine(card.medicineId).subscribe({
      next: (data) => { this.priceCompareData = data; this.priceCompareLoading = false; },
      error: () => { this.priceCompareLoading = false; }
    });
  }

  // Generic alternatives — toggled per card
  toggleAlternatives(card: ResultCard) {
    if (this.alternativesId === card.stockId) {
      this.alternativesId = null;
      return;
    }
    this.alternativesId = card.stockId;
    this.alternativesData = [];
    this.alternativesLoading = true;
    this.searchService.getAlternatives(card.medicineName).subscribe({
      next: (data) => { this.alternativesData = data; this.alternativesLoading = false; },
      error: () => { this.alternativesLoading = false; }
    });
  }

  startReserve(stockId: string) {
    if (!this.user?.id) {
      this.router.navigate(['/login']);
      return;
    }
    this.reservingId = this.reservingId === stockId ? null : stockId;
    this.reserveQty = 1;
  }

  cancelReserve() {
    this.reservingId = null;
  }

  confirmReserve(card: ResultCard) {
    if (this.reserveQty < 1 || this.reserveQty > card.quantity) {
      this.showToast('Quantity must be between 1 and ' + card.quantity, 'error');
      return;
    }
    this.reserveLoading = true;
    this.reservationService.create(this.user.id!, card.pharmacyId, card.medicineId, this.reserveQty).subscribe({
      next: (res) => {
        this.reserveLoading = false;
        this.reservingId = null;
        const expiry = this.parseDateTime(res.expiresAt);
        this.showToast(
          `Reserved ${this.reserveQty}x ${card.medicineName} at ${card.pharmacyName}. Hold expires at ${expiry}.`,
          'success'
        );
        const found = this.rawResults.find(r => r.stockId === card.stockId);
        if (found) { found.quantity -= this.reserveQty; this.applySort(); }
      },
      error: (err) => {
        this.reserveLoading = false;
        this.showToast(typeof err.error === 'string' ? err.error : 'Reservation failed. Please try again.', 'error');
      }
    });
  }

  minPrice(list: any[]): number {
    return list.reduce((min, p) => p.price < min ? p.price : min, list[0]?.price ?? 0);
  }

  parseDateTime(dt: any): string {
    if (!dt) return 'unknown';
    if (Array.isArray(dt)) {
      const d = new Date(dt[0], dt[1] - 1, dt[2], dt[3] ?? 0, dt[4] ?? 0);
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
    return new Date(dt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  private showToast(msg: string, type: 'success' | 'error') {
    clearTimeout(this.toastTimer);
    this.toast = { msg, type };
    this.toastTimer = setTimeout(() => (this.toast = null), 5000);
  }

  private clearToast() {
    clearTimeout(this.toastTimer);
    this.toast = null;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
