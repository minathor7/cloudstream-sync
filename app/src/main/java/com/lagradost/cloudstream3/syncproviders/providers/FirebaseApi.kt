package com.lagradost.cloudstream3.syncproviders.providers

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import com.lagradost.cloudstream3.syncproviders.AuthLoginResponse
import com.lagradost.cloudstream3.syncproviders.SyncApi
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.utils.AppUtils.context

class FirebaseApi : SyncApi() {
    // Uygulama içinde görünecek isim ve kimlik
    override var name = "Firebase"
    override var idPrefix = "firebase"
    override var mainUrl = "https://firebase.google.com"
    override var requirePassword = true
    override var requireUsername = true

    // Ayarlardan gelen verilerle Firebase'i başlatan fonksiyon
    override suspend fun login(login: AuthLoginResponse): Boolean {
        return try {
            val options = FirebaseOptions.Builder()
                .setProjectId(login.username!!)   // Project ID
                .setApiKey(login.password!!)     // API Key
                .setDatabaseUrl(login.server!!)   // Database URL
                .setApplicationId(login.email!!)  // App ID
                .build()

            // Eğer daha önce başlatılmadıysa Firebase'i yapılandır
            if (FirebaseApp.getApps(context!!).isEmpty()) {
                FirebaseApp.initializeApp(context!!, options)
            }
            
            showToast("Firebase Bağlantısı Başarılı")
            true
        } catch (e: Exception) {
            logError(e)
            showToast("Bağlantı Hatası: ${e.message}")
            false
        }
    }

    // İzleme geçmişini Firebase'e gönderen kısım (Karakter hatasını burası çözer)
    override suspend fun score(id: String, status: Int, score: Int?, episodes: Int?): Boolean {
        return try {
            val database = FirebaseDatabase.getInstance().reference
            val userId = "user_history" // Şimdilik tek kullanıcı için sabitledik

            val data = mapOf(
                "animeId" to id,
                "status" to status,
                "score" to score,
                "watchedEpisodes" to episodes,
                "lastUpdate" to System.currentTimeMillis()
            )

            // Veriyi JSON olarak itiyoruz, böylece Türkçe karakterler bozulmaz
            database.child(userId).child(id).setValue(data)
            true
        } catch (e: Exception) {
            logError(e)
            false
        }
    }

    // Firebase'den çıkış yapma
    override fun logout() {
        // Firebase yerel oturumu kapatma işlemleri buraya gelebilir
    }

    override suspend fun getStatus(id: String): SyncStatus? {
        // İleride Firebase'den veri çekmek için burası doldurulabilir
        return null
    }
}
