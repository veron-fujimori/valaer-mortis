# VALAER MORTIS - JAVA CLI + JDBC  
## (DOKUMENTASI FINAL LENGKAP: SISTEM LOGIN, PROGRESI, BARRACK, PASUKAN, STORAGE, MINING, CREATURE, WAKTU, MEKANIKA ATTACK & LOSS)

**Dokumentasi Terakhir Diperbarui:** 2025-06-02 16:15:00 UTC  
**Dibuat oleh:** OrionHoshizora

---

## 0. RINGKASAN

**Valaer Mortis** adalah game simulasi manajemen pasukan fantasi berbasis Java CLI & JDBC, berfokus pada pembangunan base, manajemen resource, pelatihan pasukan, eksplorasi area tambang, dan pertempuran melawan creature.  
Semua mekanika berjalan secara berbasis waktu (dalam detik, untuk simulasi cepat), dengan sistem progresi terstruktur, resource management, dan perhitungan outcome yang realistis. **SEMUA AKTIVITAS MAKSIMAL 10 DETIK**.

---

## 1. FITUR LOGIN & REGISTER
- User wajib login/register sebelum bermain.
- Setiap user hanya punya satu dunia/base (semua progress per user).
- Login dan register sangat sederhana (`username` & `password`).
- Semua progress, building, pasukan, resource, dan misi disimpan per-user.
---

## 2. STRUKTUR PROGRESI UTAMA


### 2.1. **Townhall**
- **Inti progresi base.**
- Menentukan fitur yang terbuka (unlock barrack & pasukan baru di level tertentu).
- Menentukan batas maksimal storage dan jumlah/level barak.
- **Setiap upgrade Townhall SELALU meningkatkan kapasitas level storage maksimal.**

### 2.2. **Unlock Barrack & Pasukan**

| Townhall Lv | Barrack yang Terbuka      | Pasukan yang Terbuka |
|-------------|---------------------------|----------------------|
| 1           | Barbarian Barrack         | Barbarian            |
| 2           | Archer Barrack            | Archer               |
| 4           | Mage Barrack              | Mage                 |
| 7           | Knight Barrack            | Knight               |
| 9           | Healer Barrack            | Healer               |
| 10          | Semua terbuka             | Semua terbuka        |

- Barrack/pasukan baru hanya bisa diakses jika Townhall sudah memenuhi syarat level.

### 2.3. **Batas Maksimal Barak dan Storage per Townhall Level**

| Townhall Lv | Max Barrack per Type | Max Barrack Level | Max Storage Level |
|:-----------:|:-------------------:|:-----------------:|:-----------------:|
| 1           | 1                   | 2                 | 1                 |
| 2           | 2                   | 3                 | 2                 |
| 3           | 2                   | 3                 | 3                 |
| 4           | 3                   | 4                 | 4                 |
| 5           | 3                   | 4                 | 5                 |
| 6           | 4                   | 5                 | 6                 |
| 7           | 4                   | 5                 | 7                 |
| 8           | 5                   | 6                 | 8                 |
| 9           | 5                   | 7                 | 9                 |
| 10          | 6                   | 8                 | 10                |

---

## 3. BANGUNAN DAN UPGRADE

### 3.1. **Biaya Upgrade Townhall**

| Level | Biaya Upgrade (Makanan/Kayu/Batu) | Waktu Upgrade (detik) | Penjelasan                                                |
|-------|-----------------------------------|----------------------|-----------------------------------------------------------|
| 1→2   | 800 / 1,000 / 500                 | 5                    | Membuka Archer Barrack + Max Storage Lv.2 + Max Barrack per Type: 2 + Max Barrack Level: 3 |
| 2→3   | 1,500 / 2,000 / 1,000             | 6                    | Max Storage Lv.3                                         |
| 3→4   | 3,000 / 4,000 / 2,000             | 7                    | Membuka Mage Barrack + Max Storage Lv.4 + Max Barrack per Type: 3 + Max Barrack Level: 4 |
| 4→5   | 5,000 / 7,000 / 3,000             | 7                    | Max Storage Lv.5                                         |
| 5→6   | 8,000 / 10,000 / 5,000            | 8                    | Max Storage Lv.6 + Max Barrack per Type: 4 + Max Barrack Level: 5 |
| 6→7   | 12,000 / 15,000 / 7,000           | 8                    | Membuka Knight Barrack + Max Storage Lv.7               |
| 7→8   | 16,000 / 20,000 / 10,000          | 9                    | Max Storage Lv.8 + Max Barrack per Type: 5 + Max Barrack Level: 6 |
| 8→9   | 24,000 / 30,000 / 15,000          | 9                    | Membuka Healer Barrack + Max Storage Lv.9 + Max Barrack Level: 7 |
| 9→10  | 32,000 / 40,000 / 20,000          | 10                   | Level maksimal + Max Storage Lv.10 + Max Barrack per Type: 6 + Max Barrack Level: 8 |

### 3.2. **Biaya Pembangunan Barrack**

| Barrack         | Townhall Min Lv | Biaya Bangun (Makanan/Kayu/Batu) | Waktu Bangun (detik) |
|-----------------|-----------------|----------------------------------|---------------------|
| Barbarian       | 1               | 500 / 800 / 400                  | 3                   |
| Archer          | 2               | 600 / 1,000 / 500               | 4                   |
| Mage            | 4               | 800 / 1,500 / 800               | 5                   |
| Knight          | 7               | 1,000 / 2,000 / 1,000           | 6                   |
| Healer          | 9               | 900 / 1,800 / 900               | 6                   |

### 3.3. **Upgrade Barrack**

| Barrack Lv | Kapasitas Pasukan | Durasi Latihan (detik) | Biaya Upgrade (Makanan/Kayu/Batu) | Waktu Upgrade (detik) |
|:----------:|:----------------:|:----------------------:|:---------------------------------:|:--------------------:|
| 1          | 15               | 3                      | 400 / 500 / 200                   | 2                    |
| 2          | 30               | 3                      | 800 / 1,000 / 500                 | 3                    |
| 3          | 50               | 4                      | 1,500 / 2,000 / 1,000             | 4                    |
| 4          | 80               | 4                      | 3,000 / 4,000 / 2,000             | 5                    |
| 5          | 120              | 5                      | 6,000 / 8,000 / 4,000             | 6                    |
| 6          | 170              | 5                      | 12,000 / 15,000 / 7,500           | 7                    |
| 7          | 230              | 6                      | 20,000 / 25,000 / 12,500          | 8                    |
| 8          | 300              | 6                      | 32,000 / 40,000 / 20,000          | 9                    |

### 3.4. **Storage Level dan Kapasitas**

| Storage Lv | Max Food | Max Wood | Max Stone | Syarat Townhall Lv | Biaya Upgrade (Makanan/Kayu/Batu) | Waktu Upgrade (detik) |
|:----------:|:--------:|:--------:|:---------:|:------------------:|:---------------------------------:|:--------------------:|
| 1          | 5,000    | 7,000    | 3,000     | 1                  | 600 / 800 / 400                   | 2                    |
| 2          | 12,000   | 16,000   | 8,000     | 2                  | 1,200 / 1,500 / 800               | 3                    |
| 3          | 25,000   | 35,000   | 18,000    | 3                  | 2,400 / 3,000 / 1,500             | 4                    |
| 4          | 50,000   | 70,000   | 35,000    | 4                  | 4,800 / 6,000 / 3,000             | 5                    |
| 5          | 100,000  | 140,000  | 70,000    | 5                  | 9,600 / 12,000 / 6,000            | 6                    |
| 6          | 200,000  | 280,000  | 140,000   | 6                  | 19,200 / 25,000 / 12,500          | 7                    |
| 7          | 350,000  | 490,000  | 245,000   | 7                  | 32,000 / 40,000 / 20,000          | 8                    |
| 8          | 600,000  | 840,000  | 420,000   | 8                  | 54,000 / 70,000 / 35,000          | 9                    |
| 9          | 1,000,000| 1,400,000| 700,000   | 9                  | 90,000 / 120,000 / 60,000         | 10                   |
| 10         | 1,600,000| 2,240,000| 1,120,000 | 10                 | 150,000 / 200,000 / 100,000       | 10                   |

---

## 4. PASUKAN & SISTEM LATIHAN

### 4.1. **Tipe Pasukan dan Resource Latihan**

| Tipe      | Makanan | Kayu | Batu | Durasi Latihan (detik) |
|-----------|---------|------|------|------------------------|
| Barbarian | 50      | 0    | 30   | 3                      |
| Archer    | 40      | 20   | 0    | 3                      |
| Mage      | 60      | 20   | 20   | 4                      |
| Knight    | 55      | 0    | 35   | 4                      |
| Healer    | 45      | 0    | 0    | 3                      |

- **TIDAK ADA EXP DARI LATIHAN.** Latihan hanya mengubah status pasukan dari TRAINING ke IDLE.
- **Latihan hanya dapat dilakukan jika resource & slot barak cukup.**

### 4.2. **Atribut Pasukan untuk Misi**

| Tipe      | Kapasitas Angkut/Unit | Kecepatan (unit/detik) | Attack Power/Unit | Defense Modifier |
|-----------|----------------------|------------------------|-------------------|------------------|
| Barbarian | 50                   | 1.0                    | 25                | 1.0              |
| Archer    | 30                   | 1.2                    | 20                | 1.2              |
| Mage      | 20                   | 1.0                    | 35                | 1.4              |
| Knight    | 40                   | 0.7                    | 30                | 0.8              |
| Healer    | 20                   | 1.1                    | 10                | 1.6              |

- **Kecepatan misi = kecepatan pasukan yang paling lambat** dalam batch yang dikirim.

---

## 5. SISTEM AREA MINING

### 5.1. **Area Mining Properties**

- **Tipe Resource:** food, wood, stone.
- **Level Area:**
  - **Level 1:** Townhall 1–5 (stok awal: 80,000 resource)
  - **Level 2:** Townhall 6–10 (stok awal: 200,000 resource)
- **Jarak:** 1–3 unit (untuk menjaga waktu misi maksimal 10 detik)
- **Jumlah area aktif:** 5 sekaligus
- **Regenerasi:** Area baru muncul otomatis jika stok habis, sesuai aturan Townhall.

### 5.2. **Contoh Daftar Area Mining**

| ID | Tipe  | Level | Stok Tersisa | Jarak (unit) | Status |
|----|-------|-------|--------------|--------------|--------|
| 1  | Food  | 1     | 75,000       | 2            | Aktif  |
| 2  | Wood  | 1     | 60,000       | 3            | Aktif  |
| 3  | Stone | 2     | 180,000      | 2            | Aktif  |
| ... | ...   | ...   | ...          | ...          | ...    |

### 5.3. **Efisiensi Mining (Static)**

- **Rate mining per unit pasukan:** 20 resource per detik (dipercepat)
- **Total rate mining:** jumlah_pasukan × 20 resource/detik
- **Waktu mining:** min(kapasitas_angkut_total, stok_area) / total_rate_mining

---

## 6. SISTEM CREATURE (MONSTER)

### 6.1. **Spawn Creature Berdasarkan Townhall Level**

- **Townhall Level 1:** Creature hanya level 1
- **Level 2:** Creature level 1–2 (random)
- **Level 3:** Creature level 1–3 (random)
- **Level 4:** Creature baru yang spawn hanya level 2–4 (level 1 lama tetap jika belum dibunuh)
- **Level 5:** Creature baru hanya 3–5, dst.
- **Jumlah creature aktif:** 5 sekaligus
- **Regenerasi:** Creature baru muncul otomatis setelah yang lama dikalahkan.

### 6.2. **Atribut Creature**

| Level | HP    | Attack Power | Reward Food | Reward Wood | Reward Stone | Jarak (unit) | Waktu Battle (detik) |
|-------|-------|--------------|-------------|-------------|--------------|--------------|----------------------|
| 1     | 400   | 15           | 150         | 100         | 50           | 1            | 3                    |
| 2     | 700   | 25           | 300         | 200         | 100          | 1            | 3                    |
| 3     | 1,200 | 40           | 500         | 350         | 175          | 2            | 4                    |
| 4     | 2,000 | 60           | 800         | 550         | 275          | 2            | 4                    |
| 5     | 3,200 | 85           | 1,200       | 800         | 400          | 2            | 5                    |
| 6     | 5,000 | 120          | 1,800       | 1,200       | 600          | 3            | 5                    |
| 7     | 7,500 | 160          | 2,700       | 1,800       | 900          | 3            | 6                    |
| 8     | 11,000| 210          | 4,000       | 2,700       | 1,350        | 3            | 6                    |
| 9     | 16,000| 280          | 6,000       | 4,000       | 2,000        | 3            | 7                    |
| 10    | 25,000| 370          | 9,000       | 6,000       | 3,000        | 3            | 7                    |

---

## 7. ALUR MISI: MINING & ATTACK

### 7.1. **Kalkulasi Waktu (Semua Satuan Detik)**

- **waktu_per_unit_jarak:** 1 detik (dipercepat untuk simulasi)
- **Kecepatan misi:** Min(kecepatan semua tipe pasukan dalam batch) = kecepatan yang paling lambat
- **Waktu pergi:** jarak × waktu_per_unit_jarak / kecepatan_misi
- **Waktu pulang:** Sama seperti waktu pergi

#### **Total waktu misi = waktu_pergi + waktu_mining/attack + waktu_pulang**

### 7.2. **Misi Mining (Detail Flow)**

1. **Pilih area mining & batch pasukan (bisa campuran tipe)**
2. **Hitung kecepatan misi:** Min(kecepatan semua pasukan dalam batch)
3. **Hitung kapasitas angkut total:** Σ(jumlah_unit × kapasitas_per_unit)
4. **Hitung waktu pergi:** jarak_area × 1 / kecepatan_misi
5. **Hitung hasil mining:** min(kapasitas_angkut_total, stok_area)
6. **Hitung waktu mining:** hasil_mining / (total_pasukan × 20)
7. **Hitung waktu pulang:** sama dengan waktu pergi
8. **Total waktu misi:** pergi + mining + pulang
9. **Setelah selesai:**
   - Resource masuk storage (maksimal kapasitas storage)
   - Stok area berkurang
   - Pasukan kembali ke status IDLE
   - Jika stok area habis, generate area baru

#### **Contoh Kasus Mining:**
- Kirim: 10 Barbarian (angkut 50/unit, speed 1.0) + 5 Archer (angkut 30/unit, speed 1.2)
- Kecepatan misi: min(1.0, 1.2) = 1.0
- Kapasitas total: (10×50) + (5×30) = 650
- Area: Wood, jarak 2, stok 5,000
- Waktu pergi: 2 × 1 / 1.0 = 2 detik
- Hasil mining: min(650, 5,000) = 650
- Waktu mining: 650 / (15 × 20) = 2.17 → 3 detik
- Waktu pulang: 2 detik
- **Total: 7 detik**

### 7.3. **Misi Attack Creature (Detail Flow dengan Loss Calculation)**
1. **Pilih creature target dan batch pasukan (bisa campuran)**
2. **Hitung kecepatan misi:** Min(kecepatan semua pasukan dalam batch)
3. **Hitung waktu pergi:** jarak_creature × 1 / kecepatan_misi
4. **Hitung total attack power pasukan:** Σ(jumlah_unit × attack_power_per_unit)
5. **Simulasi battle (waktu maksimal = waktu_battle dari tabel creature):**
    - **Damage per detik dari pasukan ke creature:** total_attack_power_pasukan
    - **Damage per detik dari creature ke pasukan:** creature.attack_power
    - Selama waktu battle (misal 4 detik), pasukan dan creature saling serang.
    - **Jika dalam waktu battle, HP creature habis/turun sampai 0:**
        - Creature kalah, pasukan menang, reward didapat.
    - **Jika waktu battle habis & HP creature masih > 0:**
        - Pasukan dianggap kalah, semua pasukan balik (yang selamat), **tidak ada reward**.
    - **Total damage yang diterima pasukan:** creature.attack_power × waktu_battle

6. **Kalkulasi loss pasukan:**
    - **Total defense:** Σ(jumlah_unit × (100 / defense_modifier))
    - **Damage yang diserap per tipe:**
      ```
      damage_per_tipe = total_damage × (unit_tipe × (100 / defense_modifier_tipe)) / total_defense
      ```
    - **Loss per tipe:**
      ```
      loss_tipe = ceil(damage_per_tipe / 100)
      loss_tipe = min(loss_tipe, jumlah_unit_tipe)
      ```

7. **Update jumlah pasukan:** jumlah_unit_tipe -= loss_tipe

8. **Cek hasil battle:**
    - **Total damage ke creature:** total_attack_power_pasukan × waktu_battle
    - **Jika damage ke creature >= HP creature:** Menang
        - Creature kalah, **reward HANYA material** masuk storage
        - **TIDAK ADA EXP** yang didapat pasukan
    - **Jika damage ke creature < HP creature:** Kalah
        - Creature tidak kalah, misi gagal, tidak ada reward

9. **Waktu pulang:** sama dengan waktu pergi

10. **Pasukan yang selamat kembali ke status IDLE**

---

#### **Contoh Kasus Attack (Campuran Pasukan):**

- **Target:** Creature Level 3 (HP: 1,200, Attack: 40, Battle Time: 4 detik, Jarak: 2)
- **Pasukan yang dikirim:**
    - 15 Barbarian (Attack: 25/unit, Defense Modifier: 1.0, Speed: 1.0)
    - 10 Archer (Attack: 20/unit, Defense Modifier: 1.2, Speed: 1.2)

**Perhitungan:**
- Kecepatan misi: min(1.0, 1.2) = **1.0**
- Waktu pergi: 2 × 1 / 1.0 = **2 detik**
- Total attack: (15 × 25) + (10 × 20) = **575 per detik**
- **Total damage ke creature:** 575 × 4 = **2,300** (> 1,200 HP → **MENANG**)  
  *Jika hasil kurang dari HP, berarti kalah dan tidak dapat reward!*
- **Total damage ke pasukan:** 40 × 4 = **160**

**Loss Calculation:**
- Defense Barbarian: 15 × (100 / 1.0) = **1,500**
- Defense Archer: 10 × (100 / 1.2) = **833.33**
- Total Defense: **2,333.33**
- Damage ke Barbarian: 160 × (1,500 / 2,333.33) = **102.86**
- Damage ke Archer: 160 × (833.33 / 2,333.33) = **57.14**
- Loss Barbarian: ceil(102.86 / 100) = **2 unit**
- Loss Archer: ceil(57.14 / 100) = **1 unit**

**Hasil Akhir:**
- **Sisa Barbarian:** 15 - 2 = **13 unit**
- **Sisa Archer:** 10 - 1 = **9 unit**
- **Total waktu:** 2 + 4 + 2 = **8 detik**
- **Reward:** 500 Food, 350 Wood, 175 Stone (HANYA MATERIAL, jika menang)
- **EXP:** **TIDAK ADA**

---

## 8. MENU SISTEM & FLOW PEMAIN

### 8.1. **Main Menu CLI**

```
===== VALAER MORTIS =====
Base: [NamaPlayer] | Townhall Lv.[X] | Storage: [Food/Wood/Stone]

[1] Status Base & Resource
[2] Upgrade Townhall
[3] Build/Upgrade Barrack (hanya yang sudah terbuka)
[4] Upgrade Storage
[5] Latih Pasukan (hanya tipe & barrack yang sudah tersedia)
[6] Kirim Pasukan: Mining
[7] Kirim Pasukan: Attack Creature
[8] Cek Status Pasukan & Misi
[9] Keluar

Pilih menu: _
```

### 8.2. **Menu Mining**

```
===== AREA MINING =====
[1] Food Area Lv.1 - Stok: 75,000 - Jarak: 2 unit
[2] Wood Area Lv.1 - Stok: 60,000 - Jarak: 3 unit
[3] Stone Area Lv.2 - Stok: 180,000 - Jarak: 2 unit
...

Pilih area: [1]
Pasukan tersedia:
- Barbarian: 25 (IDLE)
- Archer: 15 (IDLE)

Kirim Barbarian: [10]
Kirim Archer: [5]

Kalkulasi:
- Kecepatan misi: 1.0 (paling lambat)
- Kapasitas angkut: 650
- Waktu pergi: 2 detik
- Waktu mining: 3 detik
- Waktu pulang: 2 detik
- TOTAL WAKTU: 7 detik
- Hasil: 650 Food

Konfirmasi? (Y/N): _
```

### 8.3. **Menu Attack**

```
===== CREATURE ATTACK =====
[1] Goblin Lv.1 - HP: 400 - Jarak: 1 unit - Reward: 150/100/50
[2] Orc Lv.2 - HP: 700 - Jarak: 1 unit - Reward: 300/200/100
...

Pilih creature: [2]
Pasukan tersedia:
- Barbarian: 25 (IDLE)
- Knight: 10 (IDLE)

Kirim Barbarian: [20]
Kirim Knight: [0]

Kalkulasi:
- Kecepatan misi: 1.0
- Total attack: 500/detik
- Waktu pergi: 1 detik
- Waktu battle: 3 detik
- Estimasi loss: ~1 Barbarian
- Waktu pulang: 1 detik
- TOTAL WAKTU: 5 detik
- Hasil jika menang: 300 Food, 200 Wood, 100 Stone (TANPA EXP)

Konfirmasi? (Y/N): _
```

---

## 9. TIPS STRATEGI & GAMEPLAY

### 9.1. **Early Game (Townhall 1-3)**
- Fokus upgrade Storage dan Townhall untuk membuka Archer
- Mining Wood/Stone untuk kebutuhan upgrade
- Latih Barbarian untuk attack creature level 1-2
- **Semua aktivitas cepat, maksimal 10 detik**

### 9.2. **Mid Game (Townhall 4-7)**
- Buka Mage untuk attack yang lebih kuat
- Mulai attack creature level tinggi untuk resource
- Balance antara mining dan attack
- **Storage upgrade otomatis dengan Townhall**

### 9.3. **Late Game (Townhall 8-10)**
- Unlock Knight dan Healer
- Attack creature level tinggi dengan strategi campuran pasukan
- Maksimalkan Storage dan Barrack
- **Manfaatkan kapasitas storage yang besar**

---

## 10. CATATAN TEKNIS

- **Semua waktu dalam detik, MAKSIMAL 10 DETIK** untuk simulasi sangat cepat
- **Database menyimpan:** status misi, waktu mulai, durasi, status pasukan, status upgrade
- **Real-time check:** Sistem cek apakah misi/upgrade sudah selesai berdasarkan waktu sistem
- **Balancing:** Semua angka dapat di-tune untuk gameplay yang optimal
- **Reward system:** Attack creature **HANYA memberikan material**, tidak ada exp
- **Resource requirement:** Semua upgrade (Townhall, Barrack, Storage) membutuhkan makanan + kayu + batu
- **Time-based building:** Semua upgrade bangunan memiliki waktu tunggu

---

## 11. RANGKUMAN FINAL

- **Progression jelas:** Townhall → Storage Level Auto Increase → Unlock → Build → Train → Mission
- **Resource management:** Storage capacity selalu meningkat dengan Townhall, semua upgrade butuh 3 resource
- **Combat system:** Realistic loss calculation, risk vs reward
- **Time-based:** Semua aktivitas memakan waktu nyata (detik), maksimal 10 detik
- **Material-only rewards:** Attack creature hanya memberikan food/wood/stone
- **Fast-paced:** Gameplay sangat cepat untuk simulasi dan testing
- **Scalable:** Mudah ditambah fitur baru (equipment, research, dll)

---

**DOKUMENTASI FINAL LENGKAP - SIAP IMPLEMENTASI**  
**Terakhir diperbarui:** 2025-06-02 15:56:05 UTC oleh OrionHoshizora