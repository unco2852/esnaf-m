package com.example.esnafm.data.importer

import com.example.esnafm.data.model.ItemModel
import com.example.esnafm.data.model.ServiceModel
import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firestore örnek veri içe aktarım sınıfı.
 * Örnek işletme, ürün ve hizmet ekler; mevcut kullanıcı verilerini temizlemez.
 */
object FirestoreSeeder {

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun seedDataIfNeeded(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 2. ADIM: 5 Yeni İşletme Profilini Firestore 'users' koleksiyonuna ekle/güncelle
            val merchants = listOf(
                UserProfile(
                    uid = "merchant_dogan_tesisat",
                    email = "dogan.tesisat@esnafm.com",
                    name = "Doğan Usta",
                    role = UserRole.MERCHANT,
                    businessName = "Doğan Tesisat",
                    category = "Tesisat & Bakım",
                    address = "Sıhhi Tesisat ve Bakım Servisi"
                ),
                UserProfile(
                    uid = "merchant_bastug_yapi",
                    email = "bastug.yapi@esnafm.com",
                    name = "Baştuğ Usta",
                    role = UserRole.MERCHANT,
                    businessName = "Baştuğ Yapı Malzemeleri",
                    category = "Tesisat & Bakım",
                    address = "İnşaat ve Yapı Malzemeleri Deposu"
                ),
                UserProfile(
                    uid = "merchant_nas_terzi",
                    email = "nas.terzi@esnafm.com",
                    name = "Nas Usta",
                    role = UserRole.MERCHANT,
                    businessName = "Nas Terzi",
                    category = "Terzi & Tekstil",
                    address = "Kumaş ve Dikiş Atölyesi"
                ),
                UserProfile(
                    uid = "merchant_yokyok_anahtar",
                    email = "yokyok.anahtar@esnafm.com",
                    name = "Yok-Yok Çilingir",
                    role = UserRole.MERCHANT,
                    businessName = "Yok-Yok Anahtar",
                    category = "Tesisat & Bakım",
                    address = "Kilit, Anahtar ve Çilingir Servisi"
                ),
                UserProfile(
                    uid = "merchant_engin_kuafor",
                    email = "engin.kuafor@esnafm.com",
                    name = "Engin Usta",
                    role = UserRole.MERCHANT,
                    businessName = "Engin Kuaför",
                    category = "Berber & Kuaför",
                    address = "Saç ve Sakal Bakım Salonu"
                )
            )

            for (merchant in merchants) {
                val merchantData = merchant.toMap().toMutableMap().apply {
                    // Seed verileri tekrar yazılırken esnafın profilden kaydettiği konum korunur.
                    remove("businessLatitude")
                    remove("businessLongitude")
                }
                firestore.collection("users")
                    .document(merchant.uid)
                    .set(merchantData, SetOptions.merge())
                    .await()
            }

            // 3. ADIM: Ürünleri 'items' koleksiyonuna ekle/güncelle (Doğan Tesisat + Baştuğ Yapı)
            val products = listOf(
                // Doğan Tesisat Ürünleri (5 Adet)
                ItemModel(
                    id = "item_dogan_1",
                    merchantId = "merchant_dogan_tesisat",
                    title = "Ara musluğu",
                    price = 100.0,
                    stock = 10,
                    category = "Tesisat & Bakım",
                    isActive = true
                ),
                ItemModel(
                    id = "item_dogan_2",
                    merchantId = "merchant_dogan_tesisat",
                    title = "Duş başlığı",
                    price = 200.0,
                    stock = 10,
                    category = "Tesisat & Bakım",
                    isActive = true
                ),
                ItemModel(
                    id = "item_dogan_3",
                    merchantId = "merchant_dogan_tesisat",
                    title = "Musluk başlığı",
                    price = 100.0,
                    stock = 10,
                    category = "Tesisat & Bakım",
                    isActive = true
                ),
                ItemModel(
                    id = "item_dogan_4",
                    merchantId = "merchant_dogan_tesisat",
                    title = "Musluk gövdesi",
                    price = 200.0,
                    stock = 10,
                    category = "Tesisat & Bakım",
                    isActive = true
                ),
                ItemModel(
                    id = "item_dogan_5",
                    merchantId = "merchant_dogan_tesisat",
                    title = "Su bataryası",
                    price = 750.0,
                    stock = 10,
                    category = "Tesisat & Bakım",
                    isActive = true
                ),

                // Baştuğ Yapı Malzemeleri Ürünleri (5 Adet)
                ItemModel(
                    id = "item_bastug_1",
                    merchantId = "merchant_bastug_yapi",
                    title = "DYO boya",
                    price = 3500.0,
                    stock = 10,
                    category = "Tesisat & Bakım",
                    description = "1 gün teslimat süresi",
                    isActive = true
                ),
                ItemModel(
                    id = "item_bastug_2",
                    merchantId = "merchant_bastug_yapi",
                    title = "Astar",
                    price = 1200.0,
                    stock = 10,
                    category = "Tesisat & Bakım",
                    isActive = true
                ),
                ItemModel(
                    id = "item_bastug_3",
                    merchantId = "merchant_bastug_yapi",
                    title = "Yağlı boya",
                    price = 1100.0,
                    stock = 10,
                    category = "Tesisat & Bakım",
                    isActive = true
                ),
                ItemModel(
                    id = "item_bastug_4",
                    merchantId = "merchant_bastug_yapi",
                    title = "Yalıtım malzemesi",
                    price = 380.0,
                    stock = 10,
                    category = "Tesisat & Bakım",
                    isActive = true
                ),
                ItemModel(
                    id = "item_bastug_5",
                    merchantId = "merchant_bastug_yapi",
                    title = "Hırdavat",
                    price = 250.0,
                    stock = 10,
                    category = "Tesisat & Bakım",
                    isActive = true
                )
            )

            for (product in products) {
                firestore.collection("items")
                    .document(product.id)
                    .set(product.toMap())
                    .await()
            }

            // 4. ADIM: Hizmetleri 'services' koleksiyonuna ekle/güncelle (Nas Terzi + Yok-Yok Anahtar + Engin Kuaför)
            val services = listOf(
                // Nas Terzi Hizmetleri (4 Adet)
                ServiceModel(
                    id = "service_nas_1",
                    merchantId = "merchant_nas_terzi",
                    title = "Paça kesimi",
                    price = 125.0,
                    duration = "1 gün",
                    category = "Terzi & Tekstil",
                    isActive = true
                ),
                ServiceModel(
                    id = "service_nas_2",
                    merchantId = "merchant_nas_terzi",
                    title = "Pantolon dikimi",
                    price = 2000.0,
                    duration = "1 gün",
                    category = "Terzi & Tekstil",
                    isActive = true
                ),
                ServiceModel(
                    id = "service_nas_3",
                    merchantId = "merchant_nas_terzi",
                    title = "Pantolon daraltma",
                    price = 240.0,
                    duration = "1 gün",
                    category = "Terzi & Tekstil",
                    isActive = true
                ),
                ServiceModel(
                    id = "service_nas_4",
                    merchantId = "merchant_nas_terzi",
                    title = "Ütü yapımı",
                    price = 150.0,
                    duration = "1 gün",
                    category = "Terzi & Tekstil",
                    isActive = true
                ),

                // Yok-Yok Anahtar Hizmetleri (4 Adet)
                ServiceModel(
                    id = "service_yokyok_1",
                    merchantId = "merchant_yokyok_anahtar",
                    title = "Çilingir",
                    price = 600.0,
                    duration = "1 gün",
                    category = "Tesisat & Bakım",
                    isActive = true
                ),
                ServiceModel(
                    id = "service_yokyok_2",
                    merchantId = "merchant_yokyok_anahtar",
                    title = "Anahtar üretimi",
                    price = 80.0,
                    duration = "1 gün",
                    category = "Tesisat & Bakım",
                    isActive = true
                ),
                ServiceModel(
                    id = "service_yokyok_3",
                    merchantId = "merchant_yokyok_anahtar",
                    title = "Bıçak bileme",
                    price = 80.0,
                    duration = "1 gün",
                    category = "Tesisat & Bakım",
                    description = "Bıçak çarklama",
                    isActive = true
                ),
                ServiceModel(
                    id = "service_yokyok_4",
                    merchantId = "merchant_yokyok_anahtar",
                    title = "Kapı kolu değişimi",
                    price = 500.0,
                    duration = "1 gün",
                    category = "Tesisat & Bakım",
                    isActive = true
                ),

                // Engin Kuaför Hizmetleri (4 Adet)
                ServiceModel(
                    id = "service_engin_1",
                    merchantId = "merchant_engin_kuafor",
                    title = "Saç kesimi",
                    price = 400.0,
                    duration = "45 dakika",
                    category = "Berber & Kuaför",
                    isActive = true
                ),
                ServiceModel(
                    id = "service_engin_2",
                    merchantId = "merchant_engin_kuafor",
                    title = "Sakal tıraşı",
                    price = 200.0,
                    duration = "45 dakika",
                    category = "Berber & Kuaför",
                    isActive = true
                ),
                ServiceModel(
                    id = "service_engin_3",
                    merchantId = "merchant_engin_kuafor",
                    title = "Saç yıkama ve fön",
                    price = 200.0,
                    duration = "45 dakika",
                    category = "Berber & Kuaför",
                    isActive = true
                ),
                ServiceModel(
                    id = "service_engin_4",
                    merchantId = "merchant_engin_kuafor",
                    title = "Kaş alma",
                    price = 150.0,
                    duration = "45 dakika",
                    category = "Berber & Kuaför",
                    isActive = true
                )
            )

            for (service in services) {
                firestore.collection("services")
                    .document(service.id)
                    .set(service.toMap())
                    .await()
            }

            Result.success("✅ Veri aktarımı başarıyla tamamlandı: 5 İşletme, 10 Ürün, 12 Hizmet güncellendi/eklendi.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
