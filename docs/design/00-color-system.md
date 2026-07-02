# 00 — Rencar Renk Sistemi

> Rencar.html tasarım dosyasından çıkarılan bütün renk token'larını içerir.
> Tüm renklerin iki modu vardır: **Light** ve **Dark**.
> Kod yazılırken bu dosya BAĞLAYICI referanstır — renk uydurmak yasaktır.

---

## 1) MARKA / PRIMARY

| Token | Light | Dark |
|---|---|---|
| Primary | `#0B6BCB` | `#0B6BCB` |
| Primary Light | `#1E7FE0` | `#3B8EF0` |
| Primary On-Dark | — | `#4C95F0` |
| Primary Gradient | `linear-gradient(160deg, #1E7FE0, #0B6BCB)` | `linear-gradient(160deg, #3B8EF0, #0B6BCB)` |

> Primary rengi her iki modda da `#0B6BCB`'dir. Dark modda tıklanabilir/odak öğeler için `#4C95F0` kullanılır.

---

## 2) ARKA PLAN (Background)

| Token | Light | Dark |
|---|---|---|
| Background | `#EEF1F6` / `#F4F6F9` | `#0C0F14` |
| Surface (kart, sheet) | `#FFFFFF` | `#171C24` |
| Surface Elevated | `#F1F4F8` | `#1A212A` |
| Surface Pressed / İkincil | `#F4F6F9` | `#1F262F` |
| Map Background | `#E9EDF2` | `#10151B` |
| Top Bar / Nav Bar | `#FFFFFF` | `#10151B` |
| Bottom Sheet | `#FFFFFF` | `#171C24` |

---

## 3) METİN (Text)

| Token | Light | Dark |
|---|---|---|
| Text Primary | `#101620` | `#F3F6FA` |
| Text Primary Alt | `#141A22` | `#EAEEF3` |
| Text Secondary | `#3A4452` | `#B6BFCB` |
| Text Tertiary | `#5C6675` | `#98A2B0` |
| Text Hint / Disabled | `#8A929E` | `#7A828F` |
| Text On-Primary | `#FFFFFF` | `#FFFFFF` |

---

## 4) KENARLIK / AYRAÇ (Border / Divider)

| Token | Light | Dark |
|---|---|---|
| Border Default | `#E3E8EF` | `#2A313B` |
| Border Subtle | `#EEF1F5` | `#232A33` |
| Border Strong | `#C7CFDA` | `#252D38` |
| Divider | `#F0F2F6` | `#232A33` |
| Nav Bar Top Border | `#EEF1F5` | `#1E252E` |
| Dashed Divider | `#E3E8EF` | `#2C333D` |

---

## 5) ARAÇ KATEGORİ RENKLERİ

Harita üzerindeki pin'ler ve filtre chip'leri bu renkleri kullanır.

| Kategori | Renk | Hex |
|---|---|---|
| Ekonomik | Turuncu | `#F5821F` |
| Konfor / Premium | Mor | `#7C5CE6` |
| SUV | Altın | `#E6A700` |
| Kullanımda | Gri | `#9AA3AE` (Light) · `#5A636F` (Dark) |
| Elektrik / Diğer | Teal | `#0AB5A6` |

---

## 6) SEMANTİK RENKLER

### Başarı (Success)
| Token | Light | Dark |
|---|---|---|
| Success Default | `#1FB370` | `#1FB370` |
| Success Strong | `#1A9E63` | `#34C98A` |
| Success Background | `#E7F4EC` | `#173726` |
| Success Text | `#1A9E63` | `#34C98A` |

### Hata (Error / Danger)
| Token | Light | Dark |
|---|---|---|
| Error Default | `#E5484D` | `#E5484D` |
| Error Text | `#E5484D` | `#F0575B` |
| Error Background | `#FBEDED` | `#2E1A1B` |

### Uyarı (Warning)
| Token | Light | Dark |
|---|---|---|
| Warning | `#E6A700` | `#E6A700` |

### Bilgi (Info)
| Token | Light | Dark |
|---|---|---|
| Info Background | `#EAF2FC` | `#14233A` |
| Info Text | `#2C5A8C` | `#9CC3F0` |
| Info Icon | `#0B6BCB` | `#4C95F0` |

---

## 7) DURUM BADGELERI

| Durum | Background (Light) | Text (Light) | Background (Dark) | Text (Dark) |
|---|---|---|---|---|
| Müsait | `#E7F4EC` | `#1A9E63` | `#173726` | `#34C98A` |
| Varsayılan kart | `#E7F4EC` | `#1A9E63` | `#173726` | `#34C98A` |

---

## 8) ÖZEL / GRADİENT

| Kullanım | Değer |
|---|---|
| App Icon / Logo Gradient | `linear-gradient(160deg, #1E7FE0, #0B6BCB)` |
| Cüzdan Kartı (Light) | `linear-gradient(145deg, #1E7FE0, #0B6BCB)` |
| Cüzdan Kartı (Dark) | `linear-gradient(145deg, #2479DC, #0B5AAE)` |
| VISA Kart | `linear-gradient(135deg, #1A1F71, #0B6BCB)` |
| MasterCard | `linear-gradient(135deg, #EB001B, #F79E1B)` |
| Kullanıcı konumu halkası (Light) | `rgba(11,107,203,.18)` |
| Kullanıcı konumu halkası (Dark) | `rgba(76,149,240,.22)` |
| Primary glow (harita pin shadow) | `box-shadow: 0 6px 16px rgba(11,107,203,.45)` |

---

## 9) OVERLAY

| Kullanım | Değer |
|---|---|
| Harita üzerinde araç detay sheet'i karartma (Light) | `rgba(16,24,40,.42)` |
| Harita üzerinde araç detay sheet'i karartma (Dark) | `rgba(4,7,11,.55)` |
| Harita üst gradient (Light) | `linear-gradient(180deg, rgba(233,237,242,.95), rgba(233,237,242,0))` |
| Harita üst gradient (Dark) | `linear-gradient(180deg, rgba(16,21,27,.95), rgba(16,21,27,0))` |
