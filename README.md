# Hava'lı-App ☂️

🌀**Hava durumunu söyleyen değil, gününü havaya göre kuran uygulama.** 🌀

Gerçekten HAVAlı!

Hava uygulaması sana "hava yağmurlu" der ve orada bırakır. Gerisini sen düşünürsün: Kızlarla buluşmaya kaçta çıkmalıyım? Motorla yollar kaygan mı? Gruptakiler cuma deniz planına uyacak mu? İstanbul'a valize kaban koymalı mıyım? Ah şu deli sorular!
Havalı bu soruların hepsini **tek tahminden** cevaplar:

- 🗺️ Haritada havayı **görürsün**: yağmur bulutu Urla'dan Bornova'ya ilerler, Alaçatı'da rüzgâr eser.
- ⏰ Planın için **ne zaman çıkacağını** öğrenirsin: yağış, akşam trafiği ve yoldaki raporlar hesaba katılır.
- 🥂 Arkadaşlarınla **kaydırarak karar verirsin**: herkes oy verdiğinde plan takvime kendiliğinden eklenir.
- 💃 **Sana göre** hazırlanırsın: çabuk üşüdüğünü öğrenir, kıyafet ve valiz önerisini buna göre değiştirir.

Kotlin · Jetpack Compose · Material 3 · MVVM + katmanlı mimari · Coroutines / StateFlow · Retrofit + Gson · Open-Meteo

## 🎬 Demo videosu

> Uygulamanın tamamını 1–2 dakikada gösteren kayıt. Harita açılışı (Türkiye → İzmir), şehir çipleri ve Gemlik, kaydırarak grup oylaması, trafik haritası ve alternatif rota, kedili uyarı bildirimi, açık/koyu tema ve yan çevirme sırasıyla gösterilir.

<!-- Videoyu buraya ekle: GitHub'da düzenleme ekranına .mp4 dosyasını sürükleyip bırakman yeterli. -->
>FPS düşmemesi için 4 part olarak video yüklendi.

<div align="center">
<table>
  <tr>
    <th align="center">1. PART</th>
    <th align="center">2. PART</th>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/d3013df3-d79c-4c17-b709-970d3903ac1b" width="300" /></td>
    <td><img src="https://github.com/user-attachments/assets/57f365a8-1081-461d-9c8d-cfabd843276f" width="300"  width="300" /></td>
  </tr>
  <tr>
    <th align="center">3. PART</th>
    <th align="center">4. PART</th>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/33d7a2b7-3b3d-4f03-8e6b-651e049d11fa" width="300" width="300" /></td>
    <td><img src="https://github.com/user-attachments/assets/96539fd3-e4bb-478d-88a9-dde5020779bb"  width="300" /></td>
  </tr>
</table>

<p align="center">
  

## Çalıştırma

1. Repoyu indir: `git clone "https://github.com/nilufereksi/havali-android-app.git"` (ya da GitHub'da **Code › Download ZIP**).
2. Android Studio › **File › Open** › proje klasörü. Gradle senkronizasyonunu bekle (JDK 17, AGP 8.13.1, Kotlin 2.2.20).
3. Emülatör ya da telefon seç, **Run ▶**. Komut satırından: `./gradlew assembleDebug`, testler: `./gradlew testDebugUnitTest`.

Uygulama **DEMO** veriyle açılır, internet gerekmez. Haritadaki **DEMO / CANLI** anahtarı gerçek Open-Meteo verisine geçirir. CANLI'dayken uçak modunu açarsan hata ekranı ve **Tekrar dene** görünür.

## Özellikler

### 1. Bugün: canlı hava haritası
<!-- Ekran görüntüsü: ![Bugün](docs/bugun.png) -->
<p align="center">
  <img src="https://github.com/user-attachments/assets/6ea9e6cc-3a86-438a-8a31-55adc7b50637" width="300" />
</p>


- **Gerçek Türkiye haritası:** İl ve ilçe sınırları uygulamanın içinde gelir, internet olmadan da çizilir.
- **Her yerin kendi havası:** Yağmurda damlalar düşer, güneşte ışınlar döner, karda kar yağar, rüzgârda çizgiler akar.
- **Tek dokunuşla şehir:** Üstteki çiplerle İzmir, Alsancak, Gemlik, İstanbul, Ankara, Trabzon… arasında geçersin. Şehre dokununca harita yaklaşır, boş bir yere dokununca geri çıkar.
- **Zaman çubuğu:** Önümüzdeki 12 saati oynatırsın; yağmurun ne zaman nereye geleceğini görürsün.
- **Şehir arama:** Yazdıkça süzülür; bulunamazsa internette arar.
- **Bugün ne yapsam?** Havaya göre sörf, SUP, kayak, koşu ya da kapalı havuz önerir. Şehre yaklaşınca **kamp alanları** çadır simgesiyle çıkar.
- **Sana göre hissedilen:** "13°" yerine "sana 8° gibi gelecek" der.

### 2. Planlar: kaydır, eşleş, plan oluşsun
<!-- Ekran görüntüsü: ![Planlar](docs/planlar.png) -->
<p align="center">
  <img src="https://github.com/user-attachments/assets/1cac98b1-2f37-4c3d-b6c5-911142fe3341"  width="300" />
</p>

- **Grup oylaması:** "Beyza plan oylaması başlattı!" bildirimi kediyle gelir. Kartı sağa kaydırınca varsın, sola kaydırınca yoksun. Karar çıkınca plan takvime eklenir; istersen **kararını geri alırsın**.
- **Akıllı çıkış saati:** Yağış, akşam yoğunluğu ve yoldaki raporlarla "17:30 değil, 17:11'de çık" der; hatırlatıcıyı açarsan zamanı gelince bildirim gönderir.
- **Plan puanı:** Her plan yağış, trafik ve sıcaklığa göre 100 üzerinden puan alır; daha rahat bir saat varsa önerir.
- **Havaya göre öneri:** Yağmura denk gelen pikniği kuru bir güne taşımayı teklif eder.

### 3. Yol & Sağlık: yola çıkmadan önce
<!-- Ekran görüntüsü: ![Yol ve Sağlık](docs/yol.png) -->
<p align="center">
  <img src="https://github.com/user-attachments/assets/13498b0a-364b-4baa-84b5-052ba759aecf" width="300" />
</p>


- **Trafik haritası:** İzmir ve Gemlik'in ana yolları akıcıyken turkuaz, yoğunken turuncu, tıkalıyken kırmızıdır; üzerlerinde araçlar akar. Yol tıkalıysa **saks mavisi alternatif rota** önerir, tek dokunuşla seçersin.
- **Uyarılar:** Fırtına, kıyıda dalga ve sis için kırmızı ünlemli uyarı gösterir.
- **Ulaşıma göre risk:** Motor, bisiklet, araç ya da yürüyüş için saat saat yol riski ve gereken ekipman.
- **Hava muhbiri:** Sürücüler su birikintisi, kaygan yol, kaza gibi durumları bildirir ve onaylar.
- **Sağlık:** UV ve güneş kremi hatırlatması, cilt ipuçları, hava kalitesi ve polen.

### 4. Hazırlık: kapıdan çıkmadan
<!-- Ekran görüntüsü: ![Hazırlık](docs/hazirlik.png) -->
<p align="center">
  <img src="https://github.com/user-attachments/assets/1cf0bfb6-3547-4763-8d12-0bee6514789a"  width="300" />
</p>


- **Hazır mısın? kartı:** Bugünün havası canlı çizilir; listeyi işaretledikçe halka dolar.
- **Ne giysem?** Sana göre hissedilen sıcaklık ve yağmura göre üç parça önerir.
- **Ulaşıma göre liste:** Motorsan koruyucu ekipman, kilit, lastik kontrolü; toplu taşımadaysan kart bakiyesi.
- **Valiz:** Nereden → nereye ve kaç gece seçersin; iki şehri karşılaştırır, "kışlık ağırlıklı" der ve listeyi hazırlar.

**Her ekranda:** Koyu ve açık tema düğmesi, sağda kaydırma çubuğu; telefon yan çevrilince iki sütunlu düzen açılır.

### 5. Açık/Koyu Tema, arama çubuğu ve responsive tasarm
<table>
  <tr>
    <th>1. PART</th>
    <th>2. PART</th>
    <th>3. PART</th>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/2450d912-e5b7-4b29-89fb-d6cd4ba4eb58" width="300" /></td>
    <td><img src="https://github.com/user-attachments/assets/c0f98b91-81d1-4a22-8067-0a2b8f35a317" width="300" /></td>
    <td><img src="https://github.com/user-attachments/assets/3ff22787-1442-4c1c-abf1-8088b77e9a2d" width="300" /></td>
  </tr>
  <tr>
    <td colspan="3" align="center">
      <img src="https://github.com/user-attachments/assets/cfd1b56b-8f53-4057-a946-9f2fb1111f90" width="765" />
    </td>
  </tr>

  <!-- Alttaki 2. Yatay Görsel (3 Sütunu Kaplar) -->
  <tr>
    <td colspan="3" align="center">
      <img src="https://github.com/user-attachments/assets/bd79a750-3887-45ae-a94e-1f5f06795f0a" width="765" />
    </td>
  </tr>
</table>


## Mimari

```
presentation  →  domain  ←  data
(Compose + ViewModel)  (saf Kotlin: model, usecase, repository arayüzleri)  (Retrofit, assets, SharedPreferences, demo veri)
```

- **domain** Android'i bilmez; iş kuralları burada ve birim testlidir.
- **data** domain arayüzlerini uygular; DEMO/CANLI seçimi Strategy deseniyle yapılır.
- **presentation** ekranları durumsuzdur: `UiState` okur, `onEvent` ile bildirir.
- **di/AppContainer.kt** bütün bağımlılıkları tek yerde kurar.
- **Temiz kod:** projede toplam 8 `if` var ve hiçbir dosyada birden fazla yok. Sonuçta ne kadar fazla if, o kadar fazlalık; kararlar `when`, enum ve kural nesneleriyle verilir. Metinler `strings.xml`'de, sabitler adlandırılmış, biçim ktlint ile denetlenir.

## Ders konuları → dosyalar

| Konu | Dosya |
|---|---|
| Activity, yaşam döngüsü, Logcat | `MainActivity.kt` (`Log.d("Lifecycle", …)`), `HavaliApplication.kt` |
| BroadcastReceiver | `notification/DepartureReminderReceiver.kt`, `AlarmReminderScheduler.kt` |
| Service / ContentProvider | Kullanılmadı: uzun arka plan işi ve dış veri paylaşımı yok |
| AndroidManifest (label, INTERNET, izinler) | `app/src/main/AndroidManifest.xml` |
| Çalışma anı izni (POST_NOTIFICATIONS) | `presentation/common/Permissions.kt`, `notification/NotificationHelper.kt` |
| res/ (strings, colors, themes, drawable) | `app/src/main/res/` |
| build.gradle.kts, libs.versions.toml | `app/build.gradle.kts`, `gradle/libs.versions.toml` |
| minSdk / targetSdk / compileSdk | `app/build.gradle.kts` (26 / 36 / 36) |
| Debug / Release, BuildConfig | `app/build.gradle.kts`, `proguard-rules.pro`, `data/remote/ApiClient.kt` |
| APK / AAB, imzalama, versionCode / Name | `app/build.gradle.kts`, `keystore.properties.example` |
| Compose & State | Bütün ekranlar; örnek `today/MapSection.kt` |
| remember / rememberSaveable | `components/DropBanner.kt` / `today/MapSearchPanel.kt`, `MainActivity.kt` (tema) |
| ViewModel + StateFlow | `today/TodayViewModel.kt`, `plans/PlansViewModel.kt`, `road/RoadHealthViewModel.kt`, `prepare/PrepareViewModel.kt` |
| Coroutine, suspend, Main / IO | `data/repository/OpenMeteoRepositories.kt` (`withContext(IO)`), `viewModelScope.launch` |
| @Composable, Text / Button, Column / Row / Box, Modifier | `presentation/components/` |
| @Preview | `StateViews.kt`, `Selectors.kt`, `CityCard.kt`, `MapSearchPanel.kt`, `TodaySections.kt`, `Checklist.kt` |
| LazyColumn + CityCard | `today/MapSearchPanel.kt`, `components/CityCard.kt` |
| OutlinedTextField arama, filtreleme, boş durum | `today/MapSearchPanel.kt`, `domain/usecase/place/PlaceUseCases.kt` |
| Recomposition, state hoisting | `today/TodayScreen.kt`, `today/map/WeatherMap.kt` (animasyon yalnızca çizimde okunur) |
| Tema, koyu / açık tema, renkler | `presentation/theme/` |
| Retrofit + Gson, JSON, DTO | `data/remote/api`, `data/remote/dto`, `data/remote/mapper` |
| LoadState (Loading / Success / Empty / Error), Tekrar dene, uçak modu | `presentation/common/LoadState.kt`, `components/StateViews.kt` |
| Responsive / yan çevirme | `navigation/HavaliApp.kt` (WindowSizeClass), `today/map/MapCamera.kt` |
| Birim testleri | `app/src/test/` (38 test: domain, veri dönüşümü, ViewModel) |

## Veri ve lisanslar

Hava verisi: [Open-Meteo](https://open-meteo.com) (CC BY 4.0) · Türkiye sınırları: [borders-of-turkey](https://github.com/uyasarkocal/borders-of-turkey) (CC0) · Komşu ülkeler: [Natural Earth](https://www.naturalearthdata.com) (kamu malı). Trafik ve çıkış tahmini kural tabanlıdır; grup oyları ve raporlar demo için bellekte tutulur.
