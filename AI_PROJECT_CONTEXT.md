# 🤖 AI PROJECT CONTEXT — ESNAFIM ANDROID APPLICATION

> **ÖNEMLİ:** Bu belge, projenin tamamını ve mimarisini başka bir Yapay Zekâ (AI) ajanına veya yeni bir geliştiriciye tek seferde aktarmak için hazırlanmıştır. Bu belgeyi okuyan AI, projeyi sıfırdan keşfetmek zorunda kalmadan güvenli ve tutarlı geliştirmeler yapabilir.

---

## 1. PROJENİN AMACI VE GENEL BİLGİLER
- **Uygulama Adı:** Esnafım
- **Application ID:** `com.example.esnafm`
- **Amaç:** Yerel esnafların (terzi, kuaför, bakkal, fırın, tesisatçı vb.) ürün ve hizmetlerini sergilemesini, müşterilerin ise bu ürün/hizmetleri inceleyip ileri tarihli randevu talebi oluşturmasını ve takip etmesini sağlayan pazaryeri/platform uygulaması.
- **Kullanıcı Rolleri:** `CUSTOMER` (Müşteri) ve `MERCHANT` (Esnaf).

---

## 2. GERÇEK TEKNOLOJİ YIĞINI (TECH STACK)
- **Dil & Platform:** Kotlin (`2.2.10`), Native Android (SDK 24 - 37).
- **UI Framework:** Jetpack Compose (`BOM 2026.02.01`) + Material 3.
- **Görsel Yükleme (Image Loading):** Coil Compose (`2.7.0` - `io.coil-kt:coil-compose`).
- **Gezinme (Navigation):** Navigation Compose (`2.8.8` - `androidx.navigation:navigation-compose`).
- **Mimari:** MVVM (Model-View-ViewModel) + Clean Repository Katmanı.
- **Backend & Veritabanı:**
  - Firebase Authentication (`33.5.1` BOM)
  - Cloud Firestore (`33.5.1` BOM)
  - Firebase Storage (`33.5.1` BOM)
- **Asenkron Yapı:** Kotlin Coroutines (`Dispatchers.IO`) + Play Services (`kotlinx-coroutines-play-services:1.10.1`).
- **Bağımlılık Yönetimi:** Gradle Version Catalog (`gradle/libs.versions.toml`).

---

## 3. MİMARİ KARARLAR VE KRİTİK İLKELER

1. **Main Thread Koruması (Lazy Firebase Initialization):**
   - `AuthRepository`, `ItemRepository`, `ServiceRepository`, `AppointmentRepository` ve `StorageRepository` içerisinde `FirebaseAuth.getInstance()`, `FirebaseFirestore.getInstance()` ve `FirebaseStorage.getInstance()` nesneleri **`by lazy`** ile başlatılır.
   - Tüm asenkron I/O ve veritabanı işlemleri `withContext(Dispatchers.IO)` veya `flowOn(Dispatchers.IO)` ile sarılmıştır. **Asla ana iş parçacığında Firebase başlatması veya senkron engelleme yapılmaz.**

2. **Canlı Veritabanı Senkronizasyonu (Real-time SnapshotListeners):**
   - `ItemRepository.getItemsFlow()`, `ServiceRepository.getServicesFlow()`, `AppointmentRepository.getCustomerAppointmentsFlow()` ve `getMerchantAppointmentsFlow()` metodları **Firestore `addSnapshotListener`** kullanarak `callbackFlow` döndürür.
   - Veritabanında ürün/hizmet/randevu eklendiğinde, silindiğinde veya güncellendiğinde tüm aktif ekranlar (`CustomerHomeScreen`, `TradesmanPanelScreen`) kendiliğinden reaktif olarak güncellenir.

3. **Esnek Rol Ayrıştırması (`UserRole.fromRoleString`):**
   - Firestore'dan gelen rol dizeleri büyük/küçük harf duyarsız ayrıştırılır (`"MERCHANT"`, `"merchant"`, `"ESNAF"`, `"esnaf"`, `"TRADESMAN"` ➔ `UserRole.MERCHANT`).

---

## 4. PROJE KLASÖR HARİTASI VE KRİTİK DOSYALAR

```
app/src/main/java/com/example/esnafm/
├── MainActivity.kt                       # Tek Activity, enableEdgeToEdge(), EsnafımTheme(dynamicColor=false) ve AppNavigation()
├── data/
│   ├── model/
│   │   ├── UserProfile.kt                # UserRole enum & UserProfile data class
│   │   ├── ItemModel.kt                  # Firestore "items" koleksiyonu modeli (Ürünler)
│   │   ├── ServiceModel.kt               # Firestore "services" koleksiyonu modeli (Hizmetler)
│   │   └── AppointmentModel.kt           # Firestore "appointments" koleksiyonu modeli (Randevular)
│   └── repository/
│       ├── AuthRepository.kt             # Firebase Auth & Firestore "users" CRUD
│       ├── ItemRepository.kt             # Firestore "items" CRUD & getItemsFlow()
│       ├── ServiceRepository.kt          # Firestore "services" CRUD & getServicesFlow()
│       ├── AppointmentRepository.kt      # Firestore "appointments" CRUD & SnapshotListeners
│       └── StorageRepository.kt          # Firebase Storage resim yükleme (uploadImage)
├── navigation/
│   └── AppNavigation.kt                  # NavHost ve rotalar (role_selection, auth/{role}, customer_home, tradesman_panel, profile)
└── ui/
    ├── auth/                             # AuthScreen.kt & AuthViewModel.kt
    ├── customer/                         # CustomerHomeScreen.kt & CustomerHomeViewModel.kt
    ├── profile/                          # ProfileScreen.kt & ProfileViewModel.kt
    ├── roleselection/                    # RoleSelectionScreen.kt
    ├── theme/                            # Color.kt, Theme.kt, Type.kt
    └── tradesman/                        # TradesmanPanelScreen.kt & TradesmanPanelViewModel.kt
```

---

## 5. FIRESTORE VERİTABANI ŞEMASI

1. **`users/{uid}`:** `uid`, `email`, `name`, `role` ("CUSTOMER"/"MERCHANT"), `businessName`, `category`, `address`.
2. **`items/{itemId}`:** `id`, `merchantId`, `title`, `price`, `stock`, `description`, `imageUrl`, `isActive`, `createdAt`.
3. **`services/{serviceId}`:** `id`, `merchantId`, `title`, `description`, `price`, `duration`, `imageUrl`, `isActive`, `createdAt`.
4. **`appointments/{appointmentId}`:** `id`, `customerId`, `customerEmail`, `merchantId`, `serviceId`, `serviceTitle`, `price`, `date`, `time`, `note`, `status` ("pending"/"approved"/"rejected"), `createdAt`.

---

## 6. GEZİNME ROTALARI (NAVIGATION ROUTES)

- `role_selection` ➔ Rol Seçimi Karşılama Ekranı
- `auth/{role}` ➔ Giriş / Kayıt Ekranı (`role`: `CUSTOMER` veya `MERCHANT`)
- `customer_home` ➔ Müşteri Portalı (Keşfet, Kategoriler, Randevularım, Favoriler)
- `tradesman_panel` ➔ Esnaf Yönetim Paneli (Ürünlerim, Hizmetlerim, Siparişler, Randevular, İşletme Ayarları)
- `profile` ➔ Kullanıcı Profil Detayları & Oturum Kapatma

---

## 7. BİLİNEN DURUMLAR VE EKSİK ÖZELLİKLER

- **Görsel Yükleme (Storage):** Firebase Console tarafında Storage servisinin aktifleştirilmesi gereklidir.
- **Arama & Kategori Filtreleme:** Arayüzde arama çubuğu ve kategori çipleri mevcuttur; ancak Firestore sorgularına henüz dinamik olarak bağlanmamıştır (Tüm canlı kayıtlar listelenmektedir).
- **Favoriler:** Müşteri tarafında favoriye ekleme yerel bellek `StateFlow` üzerinde çalışmaktadır; Firestore `favorites` koleksiyonuna henüz yazılmamaktadır.
- **Sepet ve Ödeme:** Henüz kodlanmamıştır.
- **Anlık Bildirimler (FCM):** Henüz eklenmemiştir.
- **Atıl Kod:** `BakingScreen.kt` ve `BakingViewModel.kt` ilk Android Studio şablonundan kalma unused dosyalardır.

---

## 8. KOD DEĞİŞTİRMEDEN ÖNCE BİLİNMESİ GEREKENLER

1. **Kodu Değiştirirken Sırasını Koruyun:** Repository sınıflarındaki `by lazy` ve `withContext(Dispatchers.IO)` yapısını bozmayın; aksi takdirde Android emülatörlerinde EGL kare atlama (skipped frames) ve Main Thread kilitlenmeleri yaşanabilir.
2. **Kullanıcı Rolü:** Müşteri ve Esnaf ekranları iki ayrı APK değil, `UserProfile.role` değerine göre yönlendirilen tek bir uygulamadır.
3. **Firebase Console Rules:** İstemci tarafı güvenlik kontrolleri mevcuttur ancak Firebase Console'da Firestore ve Storage Security Rules tanımlanması şarttır.
