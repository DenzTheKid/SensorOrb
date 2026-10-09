# 🌟 Sensory Orb - Android Jetpack Compose

Sensory Orb adalah aplikasi Android interaktif berbasis **Kotlin** dan **Jetpack Compose (Material 3)**. Aplikasi ini menampilkan *orb* bercahaya futuristik di tengah layar yang bereaksi secara *real-time* terhadap 3 sensor dari 3 kategori berbeda dengan estetika *glassmorphism* dan *neon glow*.

---

## 📱 Daftar Sensor yang Digunakan

Aplikasi mengintegrasikan 3 sensor hardware dari 3 kategori berbeda:

| Kategori | Tipe Sensor Android | Parameter Delay | Efek Langsung ke UI |
| :--- | :--- | :--- | :--- |
| **1. MOTION** | `Sensor.TYPE_ACCELEROMETER` | `SENSOR_DELAY_GAME` (~20ms) | Menggerakkan orb sesuai kemiringan HP (*tilt physics*), meninggalkan jejak cahaya (*comet trail*), dan mendeteksi *shake* (guncangan kuat) yang memicu ledakan partikel + gelombang *shockwave* + efek suara & *haptic feedback*. |
| **2. ENVIRONMENT** | `Sensor.TYPE_LIGHT` | `SENSOR_DELAY_UI` (~60ms) | Mengubah tema aplikasi secara dinamis (*Night* < 20 lx, *Twilight* 20–1000 lx, *Day* > 1000 lx) serta intensitas *neon glow* secara halus menggunakan `animateColorAsState` + suara *chime*. |
| **3. POSITION** | `Sensor.TYPE_MAGNETIC_FIELD` + `Sensor.TYPE_ACCELEROMETER` | `SENSOR_DELAY_GAME` (~20ms) | Memutar ring kompas mini (*azimuth heading*) di sekeliling orb, menampilkan penanda kardinal (N/E/S/W) dan derajat arah yang dihaluskan dengan *low-pass filter*. |

---

## 🔄 Alur Lifecycle Sensor (Poin Presentasi Penting)

Seluruh lifecycle pendaftaran sensor dikelola melalui `SensorController` yang dihubungkan ke `MainActivity`:

```
┌─────────────────────────────────────────────────────────┐
│                    MainActivity                         │
├────────────────────────────┬────────────────────────────┤
│         onResume()         │         onPause()          │
│             │              │             │              │
│             ▼              │             ▼              │
│  sensorController.start()  │   sensorController.stop()  │
│             │              │             │              │
│             ▼              │             ▼              │
│  registerListener(...)     │   unregisterListener(...)  │
└────────────────────────────┴────────────────────────────┘
```

1. **`onResume()` → `sensorController.start()`**:
   - Dipanggil saat aplikasi masuk ke *foreground*.
   - Mendaftarkan listener sensor hardware agar UI responsif.
2. **`onPause()` → `sensorController.stop()`**:
   - Dipanggil segera saat Activity kehilangan fokus atau masuk background.
   - Wajib melepaskan (`unregisterListener`) seluruh sensor untuk **mencegah kebocoran daya baterai (*battery drain*)** dan beban CPU di latar belakang.

---

## 🧪 Dukungan Emulator & Fallback Mode

Jika dijalankan di Android Emulator atau perangkat fisik tanpa sensor cahaya fisik:
- Aplikasi **tidak akan crash** (`<uses-feature android:required="false">`).
- Terdapat panel **"Kontrol Demo / Simulator"**:
  - Slider intensitas Lux (0–1500 lx) untuk mendemokan transisi tema **Night**, **Twilight**, dan **Day**.
  - Tombol **"Simulasikan Shake"** untuk memicu animasi ledakan orb dan shockwave.
  - Arena orb mendukung **touch drag** dengan mouse/jari sehingga bola dapat digerakkan secara manual.
