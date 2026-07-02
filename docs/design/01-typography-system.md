# 01 — Rencar Tipografi Sistemi

> Rencar.html tasarım dosyasından çıkarılan bütün tipografi kurallarını içerir.
> Kod yazılırken bu dosya BAĞLAYICI referanstır — font büyüklüğü veya ağırlığı uydurmak yasaktır.

---

## 1) FONT AİLELERİ

| Rol | Aile | Kullanım Alanı | Ağırlıklar |
|---|---|---|---|
| Display / Heading | **Sora** | Uygulama adı, büyük sayılar, ekran başlıkları, fiyat/bakiye gösterimi, istatistik değerleri | 500, 600, 700, 800 |
| Body / UI | **Plus Jakarta Sans** | Gövde metni, butonlar, etiketler, chip'ler, açıklamalar, form öğeleri | 400, 500, 600, 700, 800 |

> **Kural:** Heading / Display hiyerarşisindeki bütün öğeler Sora kullanır. Bunların dışındaki tüm UI öğeleri Plus Jakarta Sans kullanır. İki fontu karıştırmak yasaktır.

---

## 2) TİPOGRAFİ SKALASI

### 2.1) Display (Sora)

Büyük sayısal bilgiler, ana ekran başlıkları ve logo için kullanılır.

| Token | Boyut | Ağırlık | Letter-Spacing | Kullanım Yeri |
|---|---|---|---|---|
| `displayXL` | 46sp | 800 | −1.0px | Aktif kiralama sayacı ("00:24:18") |
| `displayL` | 38sp | 800 | −1.5px | Uygulama adı / logo ("Rencar") |
| `displayM` | 34sp | 800 | −1.0px | Cüzdan bakiyesi ("₺340,00") |
| `displayS` | 27sp | 700 | −0.6px | Giriş ekranı selamlama ("Tekrar hoş geldin") |

### 2.2) Heading (Sora)

Ekran başlıkları ve bölüm başlıkları için kullanılır.

| Token | Boyut | Ağırlık | Letter-Spacing | Kullanım Yeri |
|---|---|---|---|---|
| `headingXL` | 24sp | 700–800 | −0.5px | Ekran başlıkları: Cüzdan, Kiralamalarım |
| `headingL` | 21sp | 700 | −0.4px | Araç detay sayfası araç adı ("Renault Clio") |
| `headingM` | 20sp | 700 | 0 | Harita başlığı, aktif kiralama maliyeti |
| `headingS` | 19sp | 700 | 0 | Orta sayfa başlıkları: Araç durumu, Ehliyet doğrulama |
| `headingXS` | 18sp | 700 | 0 | Bottom sheet başlıkları: Rezervasyon Onayı |

### 2.3) Price / Stat (Sora)

Parasal ve sayısal öne çıkarma öğeleri için ayrı tanımlanmıştır.

| Token | Boyut | Ağırlık | Letter-Spacing | Kullanım Yeri |
|---|---|---|---|---|
| `priceL` | 24sp | 800 | −0.5px | Araç birim fiyatı ("₺4,50 / dk") |
| `priceM` | 22sp | 800 | 0 | Ödeme toplamı |
| `statValue` | 17sp | 800 | 0 | Yakıt %, menzil (~480 km) |

### 2.4) Title (Plus Jakarta Sans)

Buton etiketleri, araç kart başlıkları, liste öğesi başlıkları için kullanılır.

| Token | Boyut | Ağırlık | Letter-Spacing | Kullanım Yeri |
|---|---|---|---|---|
| `titleL` | 16.5sp | 700 | 0 | Primary CTA butonu |
| `titleM` | 15.5sp | 700 | 0 | Araç adı (liste kartı) |
| `titleS` | 15sp | 700 | 0 | Sekonder buton, kart başlıkları |
| `titleXS` | 14.5sp | 700 | 0 | Küçük aksiyon butonları |

### 2.5) Body (Plus Jakarta Sans)

Açıklayıcı metinler ve form öğeleri için kullanılır.

| Token | Boyut | Ağırlık | Line-Height | Kullanım Yeri |
|---|---|---|---|---|
| `bodyL` | 15sp | 500 | 1.55 | Ana gövde metni |
| `bodyM` | 13.5sp | 500–600 | 1.5 | Alt başlık, ikincil bilgi |
| `bodyS` | 13sp | 500–600 | 1.5 | Bölüm etiketleri, form yardım metni |
| `bodyXS` | 12.5sp | 500 | 1.4 | Bilgi / hint metni |

### 2.6) Label / Caption (Plus Jakarta Sans)

Küçük etiketler, badge'ler ve alt navigasyon sekme etiketleri için kullanılır.

| Token | Boyut | Ağırlık | Letter-Spacing | Kullanım Yeri |
|---|---|---|---|---|
| `labelM` | 12sp | 500–600 | 0 | Caption, ikincil açıklama |
| `labelS` | 11.5sp | 500–600 | 0 | Küçük etiket |
| `labelXS` | 11sp | 700–800 | 0 | Badge metni |
| `labelMicro` | 10.5sp | 600–700 | 0 | Alt navigasyon sekme etiketi, harita pin metni |

---

## 3) AĞIRLIK REHBERİ

| Ağırlık | Sayısal | Tipik Kullanım |
|---|---|---|
| Regular | 400 | Yalnızca Plus Jakarta Sans; uzun açıklama paragrafları |
| Medium | 500 | Varsayılan gövde metni; ikincil bilgi |
| SemiBold | 600 | Vurgulu gövde; chip/filtre etiketi; section başlığı |
| Bold | 700 | Başlık, buton, araç adı, CTA |
| ExtraBold | 800 | Sayısal ekran değerleri, fiyat, bakiye, badge, uygulama adı |

---

## 4) LETTER-SPACING KURALLARI

| Boyut Aralığı | Letter-Spacing | Gerekçe |
|---|---|---|
| 46sp ve üzeri | −1.0px | Negatif takip büyük sayıları sıkıştırır, okunabilirliği artırır |
| 38sp | −1.5px | En güçlü sıkıştırma; yalnızca logo/uygulama adı |
| 34sp | −1.0px | Büyük bakiye gösterimi |
| 27sp | −0.6px | Ekran selamlama başlığı |
| 24sp Heading | −0.5px | Ekran başlığı düzeyi |
| 21sp | −0.4px | Araç adı detay sayfası |
| 20sp ve altı | 0 | Negatif takip uygulanmaz |

> **Kural:** Pozitif letter-spacing Rencar tasarımında kullanılmaz. Büyük Display ve Heading token'larının dışında spacing değeri her zaman 0'dır.

---

## 5) LINE-HEIGHT KURALLARI

| Metin Türü | Line-Height | Gerekçe |
|---|---|---|
| Display / Heading | 1.2–1.3 | Sıkı satır aralığı, tek satır başlık görünümü |
| Body L | 1.55 | Uzun okuma için rahat boşluk |
| Body M–S | 1.5 | Orta uzunluk metinler |
| Body XS ve altı | 1.4 | Kısa etiket/bilgi parçacıkları |

---

## 6) UYGULAMA NOTALARI (Android / Compose)

- Sora ve Plus Jakarta Sans fontları `res/font/` altına yerleştirilmeli ve `FontFamily` olarak tanımlanmalıdır.
- Compose'da `sp` birimi kullanılır; bu dosyadaki `sp` değerleri birebir `TextUnit` olarak haritalanır.
- Letter-spacing Compose'da `TextStyle(letterSpacing = (-0.6).sp)` biçiminde verilir.
- `MaterialTheme.typography` içinde yukarıdaki token isimleriyle (`displayXL`, `headingM` vb.) özel `Typography` nesnesi tanımlanmalıdır.
- Hiçbir ekranda hardcoded `fontSize`, `fontWeight` veya `fontFamily` kullanılamaz; her zaman `MaterialTheme.typography.*` üzerinden erişilir.
