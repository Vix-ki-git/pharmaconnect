import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ThemeService } from '../../../services/theme.service';
import { InventoryService } from '../../../services/inventory.service';
import { AdminService } from '../../../services/admin.service';
import { SellerService } from '../../../services/seller.service';

@Component({
  selector: 'app-seller-inventory',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './seller-inventory.html',
  styleUrl: './seller-inventory.css'
})
export class SellerInventory implements OnInit {
  user: any;
  pharmacyId = '';
  isVerified = false;
  sidebarOpen = false;

  stock: any[] = [];
  allMedicines: any[] = [];
  loading = true;
  error = '';

  // Inline editing
  editingId: string | null = null;
  editQty = 0;
  editPrice = 0;
  savingId: string | null = null;

  // Add form
  showAddForm = false;
  addMedicineId = '';
  addQty = 0;
  addPrice = 0;
  addMfgDate = '';
  addExpDate = '';
  addLoading = false;
  addError = '';

  // Bulk upload
  bulkFile: File | null = null;
  bulkLoading = false;
  bulkMsg = '';
  showBulkHelp = false;

  filterQuery = '';

  toast: { msg: string; type: 'success' | 'error' } | null = null;
  private toastTimer: any;

  constructor(
    private authService: AuthService,
    public themeService: ThemeService,
    private inventoryService: InventoryService,
    private adminService: AdminService,
    private sellerService: SellerService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngOnInit() {
    if (!this.user?.id) { this.router.navigate(['/login']); return; }
    this.sellerService.getDashboard(this.user.email).subscribe({
      next: (d) => {
        this.pharmacyId = d.pharmacyId;
        this.isVerified = d.isPharmacyVerified;
        this.loadStock();
        this.adminService.getAllMedicines().subscribe({
          next: (m) => this.allMedicines = m,
          error: () => {}
        });
      },
      error: () => {
        this.loading = false;
        this.error = 'Could not load pharmacy info.';
      }
    });
  }

  loadStock() {
    this.loading = true;
    this.inventoryService.getByPharmacy(this.pharmacyId).subscribe({
      next: (data) => { this.loading = false; this.stock = data; },
      error: () => { this.loading = false; this.error = 'Failed to load inventory.'; }
    });
  }

  startEdit(item: any) {
    this.editingId = item.stockId;
    this.editQty = item.quantity;
    this.editPrice = item.price;
  }

  cancelEdit() { this.editingId = null; }

  saveEdit(item: any) {
    this.savingId = item.stockId;
    const qtyChanged = this.editQty !== item.quantity;
    const priceChanged = this.editPrice !== item.price;

    if (!qtyChanged && !priceChanged) {
      this.editingId = null;
      this.savingId = null;
      return;
    }

    const done = () => {
      this.savingId = null;
      this.editingId = null;
      this.loadStock();
    };

    if (qtyChanged) {
      this.inventoryService.updateQuantity(item.stockId, this.editQty).subscribe({
        next: () => {
          if (priceChanged) {
            this.inventoryService.updatePrice(item.stockId, this.editPrice).subscribe({
              next: () => { this.showToast('Item updated.', 'success'); done(); },
              error: () => { this.savingId = null; this.showToast('Price update failed.', 'error'); }
            });
          } else {
            this.showToast('Quantity updated.', 'success');
            done();
          }
        },
        error: () => { this.savingId = null; this.showToast('Quantity update failed.', 'error'); }
      });
    } else if (priceChanged) {
      this.inventoryService.updatePrice(item.stockId, this.editPrice).subscribe({
        next: () => { this.showToast('Price updated.', 'success'); done(); },
        error: () => { this.savingId = null; this.showToast('Price update failed.', 'error'); }
      });
    }
  }

  adjust(item: any, delta: number) {
    this.inventoryService.adjustQuantity(item.stockId, delta).subscribe({
      next: () => { this.showToast(`Stock ${delta > 0 ? '+' + delta : delta}.`, 'success'); this.loadStock(); },
      error: (err) => this.showToast(typeof err.error === 'string' ? err.error : 'Adjust failed.', 'error')
    });
  }

  deleteItem(item: any) {
    if (!confirm(`Remove ${item.medicineName} from inventory?`)) return;
    this.inventoryService.deleteItem(item.stockId).subscribe({
      next: () => { this.showToast('Item removed.', 'success'); this.loadStock(); },
      error: () => this.showToast('Delete failed.', 'error')
    });
  }

  submitAdd() {
    if (!this.addMedicineId || this.addQty < 1 || this.addPrice <= 0) {
      this.addError = 'Select a medicine and enter valid quantity and price.';
      return;
    }
    if (this.addMfgDate && this.addExpDate && this.addExpDate < this.addMfgDate) {
      this.addError = 'Expiry date must be on or after the manufacturing date.';
      return;
    }
    this.addLoading = true;
    this.addError = '';
    this.inventoryService.addItem({
      pharmacy: { id: this.pharmacyId },
      medicine: { id: this.addMedicineId },
      quantity: this.addQty,
      price: this.addPrice,
      manufacturingDate: this.addMfgDate || null,
      expiryDate: this.addExpDate || null
    }).subscribe({
      next: () => {
        this.addLoading = false;
        this.showAddForm = false;
        this.addMedicineId = '';
        this.addQty = 0;
        this.addPrice = 0;
        this.addMfgDate = '';
        this.addExpDate = '';
        this.showToast('Item added to inventory.', 'success');
        this.loadStock();
      },
      error: (err) => {
        this.addLoading = false;
        this.addError = typeof err.error === 'string' ? err.error : 'Failed to add item.';
      }
    });
  }

  isExpired(item: any): boolean {
    if (!item.expiryDate) return false;
    const exp = this.toDate(item.expiryDate);
    if (!exp) return false;
    return exp.getTime() < Date.now();
  }

  isExpiringSoon(item: any): boolean {
    if (!item.expiryDate) return false;
    const exp = this.toDate(item.expiryDate);
    if (!exp) return false;
    const days = (exp.getTime() - Date.now()) / 86400000;
    return days >= 0 && days <= 30;
  }

  get expiredCount(): number {
    return this.stock.filter(s => this.isExpired(s)).length;
  }

  formatDateOnly(d: any): string {
    const dt = this.toDate(d);
    if (!dt) return '—';
    return dt.toLocaleDateString([], { year: 'numeric', month: 'short', day: 'numeric' });
  }

  private toDate(d: any): Date | null {
    if (!d) return null;
    if (Array.isArray(d)) return new Date(d[0], d[1] - 1, d[2] ?? 1);
    return new Date(d);
  }

  downloadSampleCsv() {
    const csv = 'medicineName,quantity,price,manufacturingDate,expiryDate\nParacetamol,100,25.00,2025-01-15,2027-01-15\nIbuprofen,50,42.00,2025-03-01,2027-03-01\n';
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'pharmaconnect-inventory-sample.csv';
    a.click();
    URL.revokeObjectURL(url);
  }

  onFileChange(event: any) {
    this.bulkFile = event.target.files[0] ?? null;
  }

  uploadBulk() {
    if (!this.bulkFile) return;
    this.bulkLoading = true;
    this.bulkMsg = '';
    this.inventoryService.bulkUpload(this.pharmacyId, this.bulkFile).subscribe({
      next: (msg) => {
        this.bulkLoading = false;
        this.bulkMsg = msg;
        this.bulkFile = null;
        this.showToast('Bulk upload successful.', 'success');
        this.loadStock();
      },
      error: (err) => {
        this.bulkLoading = false;
        this.bulkMsg = typeof err.error === 'string' ? err.error : 'Bulk upload failed.';
        this.showToast(this.bulkMsg, 'error');
      }
    });
  }

  parseDate(dt: any): string {
    if (!dt) return '—';
    if (Array.isArray(dt)) {
      const d = new Date(dt[0], dt[1] - 1, dt[2], dt[3] ?? 0, dt[4] ?? 0);
      return d.toLocaleString([], { dateStyle: 'short', timeStyle: 'short' });
    }
    return new Date(dt).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' });
  }

  private showToast(msg: string, type: 'success' | 'error') {
    clearTimeout(this.toastTimer);
    this.toast = { msg, type };
    this.toastTimer = setTimeout(() => (this.toast = null), 4000);
  }

  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
