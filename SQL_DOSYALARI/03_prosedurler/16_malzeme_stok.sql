-- SantiyeOS Malzeme/Stok Module Migration
-- Bu dosya tablo verisini silmez. Java tarafinin cagirdigi stored procedure sozlesmesini gunceller.

USE santiyeos;

DROP TRIGGER IF EXISTS trg_stok_giris_after_insert;
DROP TRIGGER IF EXISTS trg_stok_hareket_koruma;

DROP PROCEDURE IF EXISTS sp_malzeme_kategori_ekle;
DROP PROCEDURE IF EXISTS sp_malzeme_kategori_guncelle;
DROP PROCEDURE IF EXISTS sp_malzeme_kategori_sil;
DROP PROCEDURE IF EXISTS sp_malzeme_kategori_listele;
DROP PROCEDURE IF EXISTS sp_malzeme_kategori_getir;
DROP PROCEDURE IF EXISTS sp_malzeme_ekle;
DROP PROCEDURE IF EXISTS sp_malzeme_guncelle;
DROP PROCEDURE IF EXISTS sp_malzeme_sil;
DROP PROCEDURE IF EXISTS sp_malzeme_listele;
DROP PROCEDURE IF EXISTS sp_malzeme_getir;
DROP PROCEDURE IF EXISTS sp_stok_hareket_ekle;
DROP PROCEDURE IF EXISTS sp_stok_hareket_listele;
DROP PROCEDURE IF EXISTS sp_stok_hareket_getir;

DELIMITER //

CREATE PROCEDURE sp_malzeme_kategori_listele(
    IN p_firma_id   INT,
    IN p_limit      INT,
    IN p_offset     INT,
    OUT p_toplam    INT
)
BEGIN
    SET p_limit = COALESCE(NULLIF(p_limit, 0), 20);
    SET p_offset = COALESCE(p_offset, 0);

    IF p_limit > 100 THEN
        SET p_limit = 100;
    END IF;

    IF p_offset < 0 THEN
        SET p_offset = 0;
    END IF;

    SELECT COUNT(*) INTO p_toplam
    FROM malzeme_kategori mk
    WHERE mk.firma_id = p_firma_id
      AND mk.aktif = 1;

    SELECT
        mk.kategori_id,
        mk.firma_id,
        mk.ad,
        mk.aciklama,
        mk.aktif,
        mk.created_at,
        COUNT(DISTINCT m.malzeme_id) AS malzeme_sayisi
    FROM malzeme_kategori mk
    LEFT JOIN malzeme m ON m.kategori_id = mk.kategori_id
        AND m.firma_id = mk.firma_id
        AND m.aktif = 1
    WHERE mk.firma_id = p_firma_id
      AND mk.aktif = 1
    GROUP BY mk.kategori_id, mk.firma_id, mk.ad, mk.aciklama, mk.aktif, mk.created_at
    ORDER BY mk.ad ASC
    LIMIT p_limit OFFSET p_offset;
END //

CREATE PROCEDURE sp_malzeme_kategori_getir(
    IN p_kategori_id    INT,
    IN p_firma_id       INT
)
BEGIN
    SELECT
        mk.kategori_id,
        mk.firma_id,
        mk.ad,
        mk.aciklama,
        mk.aktif,
        mk.created_at,
        COUNT(DISTINCT m.malzeme_id) AS malzeme_sayisi
    FROM malzeme_kategori mk
    LEFT JOIN malzeme m ON m.kategori_id = mk.kategori_id
        AND m.firma_id = mk.firma_id
        AND m.aktif = 1
    WHERE mk.kategori_id = p_kategori_id
      AND mk.firma_id = p_firma_id
      AND mk.aktif = 1
    GROUP BY mk.kategori_id, mk.firma_id, mk.ad, mk.aciklama, mk.aktif, mk.created_at;
END //

CREATE PROCEDURE sp_malzeme_kategori_ekle(
    IN p_firma_id   INT,
    IN p_rol        VARCHAR(50),
    IN p_ad         VARCHAR(100),
    IN p_aciklama   TEXT
)
BEGIN
    DECLARE v_id INT;

    IF p_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF p_ad IS NULL OR TRIM(p_ad) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Kategori adi zorunludur.';
    END IF;

    INSERT INTO malzeme_kategori (firma_id, ad, aciklama)
    VALUES (p_firma_id, TRIM(p_ad), p_aciklama);

    SET v_id = LAST_INSERT_ID();

    SELECT v_id AS kategori_id;
END //

CREATE PROCEDURE sp_malzeme_kategori_guncelle(
    IN p_kategori_id    INT,
    IN p_firma_id       INT,
    IN p_rol            VARCHAR(50),
    IN p_ad             VARCHAR(100),
    IN p_aciklama       TEXT
)
BEGIN
    IF p_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF p_ad IS NULL OR TRIM(p_ad) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Kategori adi zorunludur.';
    END IF;

    UPDATE malzeme_kategori
    SET ad = TRIM(p_ad),
        aciklama = p_aciklama
    WHERE kategori_id = p_kategori_id
      AND firma_id = p_firma_id
      AND aktif = 1;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_malzeme_kategori_sil(
    IN p_kategori_id    INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50)
)
BEGIN
    IF p_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI')
       OR NOT fn_kullanici_yetki_kontrol_firma(
            p_kullanici_id,
            p_firma_id,
            'SUPER_ADMIN,FIRMA_ADMIN,PROJE_YONETICISI'
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM malzeme
        WHERE kategori_id = p_kategori_id
          AND firma_id = p_firma_id
          AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Aktif malzemesi olan kategori silinemez.';
    END IF;

    UPDATE malzeme_kategori
    SET aktif = 0
    WHERE kategori_id = p_kategori_id
      AND firma_id = p_firma_id
      AND aktif = 1;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_malzeme_listele(
    IN p_firma_id       INT,
    IN p_kategori_id    INT,
    IN p_kritik_stok    TINYINT,
    IN p_limit          INT,
    IN p_offset         INT,
    OUT p_toplam        INT
)
BEGIN
    SET p_limit = COALESCE(NULLIF(p_limit, 0), 20);
    SET p_offset = COALESCE(p_offset, 0);

    IF p_limit > 100 THEN
        SET p_limit = 100;
    END IF;

    IF p_offset < 0 THEN
        SET p_offset = 0;
    END IF;

    SELECT COUNT(*) INTO p_toplam
    FROM malzeme m
    WHERE m.firma_id = p_firma_id
      AND (p_kategori_id IS NULL OR m.kategori_id = p_kategori_id)
      AND (p_kritik_stok IS NULL OR (m.stok_miktari <= m.min_stok) = p_kritik_stok)
      AND m.aktif = 1;

    SELECT
        m.malzeme_id,
        m.firma_id,
        m.kategori_id,
        m.ad,
        m.birim,
        m.birim_fiyat,
        m.stok_miktari,
        m.min_stok,
        (m.stok_miktari <= m.min_stok) AS kritik_stok,
        m.aktif,
        m.created_at,
        m.updated_at,
        mk.ad AS kategori_ad
    FROM malzeme m
    LEFT JOIN malzeme_kategori mk ON mk.kategori_id = m.kategori_id
        AND mk.firma_id = m.firma_id
    WHERE m.firma_id = p_firma_id
      AND (p_kategori_id IS NULL OR m.kategori_id = p_kategori_id)
      AND (p_kritik_stok IS NULL OR (m.stok_miktari <= m.min_stok) = p_kritik_stok)
      AND m.aktif = 1
    ORDER BY m.ad ASC
    LIMIT p_limit OFFSET p_offset;
END //

CREATE PROCEDURE sp_malzeme_getir(
    IN p_malzeme_id INT,
    IN p_firma_id   INT
)
BEGIN
    SELECT
        m.malzeme_id,
        m.firma_id,
        m.kategori_id,
        m.ad,
        m.birim,
        m.birim_fiyat,
        m.stok_miktari,
        m.min_stok,
        (m.stok_miktari <= m.min_stok) AS kritik_stok,
        m.aktif,
        m.created_at,
        m.updated_at,
        mk.ad AS kategori_ad
    FROM malzeme m
    LEFT JOIN malzeme_kategori mk ON mk.kategori_id = m.kategori_id
        AND mk.firma_id = m.firma_id
    WHERE m.malzeme_id = p_malzeme_id
      AND m.firma_id = p_firma_id
      AND m.aktif = 1;
END //

CREATE PROCEDURE sp_malzeme_ekle(
    IN p_firma_id       INT,
    IN p_rol            VARCHAR(50),
    IN p_kategori_id    INT,
    IN p_ad             VARCHAR(200),
    IN p_birim          VARCHAR(20),
    IN p_birim_fiyat    DECIMAL(15,2),
    IN p_min_stok       DECIMAL(15,3)
)
BEGIN
    DECLARE v_id INT;
    DECLARE v_birim VARCHAR(20);

    SET v_birim = UPPER(COALESCE(NULLIF(TRIM(p_birim), ''), 'ADET'));

    IF p_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF p_ad IS NULL OR TRIM(p_ad) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Malzeme adi zorunludur.';
    END IF;

    IF v_birim NOT IN ('ADET', 'KG', 'TON', 'METRE', 'M2', 'M3', 'LITRE') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecersiz malzeme birimi.';
    END IF;

    IF p_birim_fiyat IS NULL OR p_birim_fiyat < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Birim fiyat negatif olamaz.';
    END IF;

    IF p_min_stok IS NULL OR p_min_stok < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Minimum stok negatif olamaz.';
    END IF;

    IF p_kategori_id IS NOT NULL
       AND NOT EXISTS (
            SELECT 1
            FROM malzeme_kategori
            WHERE kategori_id = p_kategori_id
              AND firma_id = p_firma_id
              AND aktif = 1
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Malzeme kategorisi bulunamadi.';
    END IF;

    INSERT INTO malzeme (
        firma_id, kategori_id, ad, birim, birim_fiyat, min_stok
    ) VALUES (
        p_firma_id, p_kategori_id, TRIM(p_ad), v_birim, p_birim_fiyat, p_min_stok
    );

    SET v_id = LAST_INSERT_ID();

    SELECT v_id AS malzeme_id;
END //

CREATE PROCEDURE sp_malzeme_guncelle(
    IN p_malzeme_id     INT,
    IN p_firma_id       INT,
    IN p_rol            VARCHAR(50),
    IN p_kategori_id    INT,
    IN p_ad             VARCHAR(200),
    IN p_birim          VARCHAR(20),
    IN p_birim_fiyat    DECIMAL(15,2),
    IN p_min_stok       DECIMAL(15,3)
)
BEGIN
    DECLARE v_birim VARCHAR(20);

    SET v_birim = UPPER(COALESCE(NULLIF(TRIM(p_birim), ''), 'ADET'));

    IF p_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF p_ad IS NULL OR TRIM(p_ad) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Malzeme adi zorunludur.';
    END IF;

    IF v_birim NOT IN ('ADET', 'KG', 'TON', 'METRE', 'M2', 'M3', 'LITRE') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecersiz malzeme birimi.';
    END IF;

    IF p_birim_fiyat IS NULL OR p_birim_fiyat < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Birim fiyat negatif olamaz.';
    END IF;

    IF p_min_stok IS NULL OR p_min_stok < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Minimum stok negatif olamaz.';
    END IF;

    IF p_kategori_id IS NOT NULL
       AND NOT EXISTS (
            SELECT 1
            FROM malzeme_kategori
            WHERE kategori_id = p_kategori_id
              AND firma_id = p_firma_id
              AND aktif = 1
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Malzeme kategorisi bulunamadi.';
    END IF;

    UPDATE malzeme
    SET kategori_id = p_kategori_id,
        ad = TRIM(p_ad),
        birim = v_birim,
        birim_fiyat = p_birim_fiyat,
        min_stok = p_min_stok
    WHERE malzeme_id = p_malzeme_id
      AND firma_id = p_firma_id
      AND aktif = 1;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_malzeme_sil(
    IN p_malzeme_id     INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50)
)
BEGIN
    IF p_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI')
       OR NOT fn_kullanici_yetki_kontrol_firma(
            p_kullanici_id,
            p_firma_id,
            'SUPER_ADMIN,FIRMA_ADMIN,PROJE_YONETICISI'
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM stok_hareket
        WHERE malzeme_id = p_malzeme_id
          AND firma_id = p_firma_id
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Stok hareketi olan malzeme silinemez.';
    END IF;

    UPDATE malzeme
    SET aktif = 0
    WHERE malzeme_id = p_malzeme_id
      AND firma_id = p_firma_id
      AND aktif = 1;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_stok_hareket_listele(
    IN p_firma_id       INT,
    IN p_malzeme_id     INT,
    IN p_proje_id       INT,
    IN p_is_emri_id     INT,
    IN p_hareket_tipi   VARCHAR(20),
    IN p_limit          INT,
    IN p_offset         INT,
    OUT p_toplam        INT
)
BEGIN
    DECLARE v_hareket_tipi VARCHAR(20);

    SET v_hareket_tipi = UPPER(NULLIF(TRIM(p_hareket_tipi), ''));
    SET p_limit = COALESCE(NULLIF(p_limit, 0), 20);
    SET p_offset = COALESCE(p_offset, 0);

    IF p_limit > 100 THEN
        SET p_limit = 100;
    END IF;

    IF p_offset < 0 THEN
        SET p_offset = 0;
    END IF;

    IF v_hareket_tipi IS NOT NULL AND v_hareket_tipi NOT IN ('GIRIS', 'CIKIS') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecersiz stok hareket tipi.';
    END IF;

    SELECT COUNT(*) INTO p_toplam
    FROM stok_hareket sh
    WHERE sh.firma_id = p_firma_id
      AND (p_malzeme_id IS NULL OR sh.malzeme_id = p_malzeme_id)
      AND (p_proje_id IS NULL OR sh.proje_id = p_proje_id)
      AND (p_is_emri_id IS NULL OR sh.is_emri_id = p_is_emri_id)
      AND (v_hareket_tipi IS NULL OR sh.hareket_tipi = v_hareket_tipi);

    SELECT
        sh.hareket_id,
        sh.firma_id,
        sh.malzeme_id,
        sh.proje_id,
        sh.is_emri_id,
        sh.kaydeden_id,
        sh.hareket_tipi,
        sh.miktar,
        sh.birim_fiyat,
        sh.aciklama,
        sh.created_at,
        m.ad AS malzeme_ad,
        m.birim,
        p.ad AS proje_ad,
        ie.baslik AS is_emri_baslik,
        CONCAT(k.ad, ' ', k.soyad) AS kaydeden
    FROM stok_hareket sh
    INNER JOIN malzeme m ON m.malzeme_id = sh.malzeme_id AND m.firma_id = sh.firma_id
    LEFT JOIN proje p ON p.proje_id = sh.proje_id AND p.firma_id = sh.firma_id
    LEFT JOIN is_emri ie ON ie.is_emri_id = sh.is_emri_id AND ie.firma_id = sh.firma_id
    LEFT JOIN kullanici k ON k.kullanici_id = sh.kaydeden_id
    WHERE sh.firma_id = p_firma_id
      AND (p_malzeme_id IS NULL OR sh.malzeme_id = p_malzeme_id)
      AND (p_proje_id IS NULL OR sh.proje_id = p_proje_id)
      AND (p_is_emri_id IS NULL OR sh.is_emri_id = p_is_emri_id)
      AND (v_hareket_tipi IS NULL OR sh.hareket_tipi = v_hareket_tipi)
    ORDER BY sh.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END //

CREATE PROCEDURE sp_stok_hareket_getir(
    IN p_hareket_id INT,
    IN p_firma_id   INT
)
BEGIN
    SELECT
        sh.hareket_id,
        sh.firma_id,
        sh.malzeme_id,
        sh.proje_id,
        sh.is_emri_id,
        sh.kaydeden_id,
        sh.hareket_tipi,
        sh.miktar,
        sh.birim_fiyat,
        sh.aciklama,
        sh.created_at,
        m.ad AS malzeme_ad,
        m.birim,
        p.ad AS proje_ad,
        ie.baslik AS is_emri_baslik,
        CONCAT(k.ad, ' ', k.soyad) AS kaydeden
    FROM stok_hareket sh
    INNER JOIN malzeme m ON m.malzeme_id = sh.malzeme_id AND m.firma_id = sh.firma_id
    LEFT JOIN proje p ON p.proje_id = sh.proje_id AND p.firma_id = sh.firma_id
    LEFT JOIN is_emri ie ON ie.is_emri_id = sh.is_emri_id AND ie.firma_id = sh.firma_id
    LEFT JOIN kullanici k ON k.kullanici_id = sh.kaydeden_id
    WHERE sh.hareket_id = p_hareket_id
      AND sh.firma_id = p_firma_id;
END //

CREATE PROCEDURE sp_stok_hareket_ekle(
    IN p_firma_id       INT,
    IN p_malzeme_id     INT,
    IN p_proje_id       INT,
    IN p_is_emri_id     INT,
    IN p_kaydeden_id    INT,
    IN p_rol            VARCHAR(50),
    IN p_hareket_tipi   VARCHAR(20),
    IN p_miktar         DECIMAL(15,3),
    IN p_birim_fiyat    DECIMAL(15,2),
    IN p_aciklama       TEXT
)
BEGIN
    DECLARE v_id INT;
    DECLARE v_hareket_tipi VARCHAR(20);
    DECLARE v_stok_miktari DECIMAL(15,3);
    DECLARE v_birim_fiyat DECIMAL(15,2);
    DECLARE v_is_emri_proje_id INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    SET v_hareket_tipi = UPPER(COALESCE(NULLIF(TRIM(p_hareket_tipi), ''), ''));

    IF p_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI', 'SAHA_PERSONELI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF NOT fn_kullanici_yetki_kontrol_firma(
        p_kaydeden_id,
        p_firma_id,
        'SUPER_ADMIN,FIRMA_ADMIN,PROJE_YONETICISI,SAHA_PERSONELI'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF v_hareket_tipi NOT IN ('GIRIS', 'CIKIS') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecersiz stok hareket tipi.';
    END IF;

    IF p_miktar IS NULL OR p_miktar <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Miktar sifirdan buyuk olmalidir.';
    END IF;

    IF p_birim_fiyat IS NOT NULL AND p_birim_fiyat < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Birim fiyat negatif olamaz.';
    END IF;

    IF p_proje_id IS NOT NULL
       AND NOT EXISTS (
            SELECT 1
            FROM proje
            WHERE proje_id = p_proje_id
              AND firma_id = p_firma_id
              AND durum <> 'IPTAL'
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Proje bulunamadi.';
    END IF;

    IF p_is_emri_id IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1
            FROM is_emri
            WHERE is_emri_id = p_is_emri_id
              AND firma_id = p_firma_id
        ) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Is emri bulunamadi.';
        END IF;

        SELECT proje_id INTO v_is_emri_proje_id
        FROM is_emri
        WHERE is_emri_id = p_is_emri_id
          AND firma_id = p_firma_id;

        IF p_proje_id IS NOT NULL AND v_is_emri_proje_id <> p_proje_id THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Is emri secilen projeye ait degil.';
        END IF;
    END IF;

    START TRANSACTION;
        SELECT stok_miktari, birim_fiyat
        INTO v_stok_miktari, v_birim_fiyat
        FROM malzeme
        WHERE malzeme_id = p_malzeme_id
          AND firma_id = p_firma_id
          AND aktif = 1
        FOR UPDATE;

        IF v_stok_miktari IS NULL THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Malzeme bulunamadi.';
        END IF;

        IF v_hareket_tipi = 'CIKIS' AND v_stok_miktari < p_miktar THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetersiz stok.';
        END IF;

        INSERT INTO stok_hareket (
            firma_id, malzeme_id, proje_id, is_emri_id, kaydeden_id,
            hareket_tipi, miktar, birim_fiyat, aciklama
        ) VALUES (
            p_firma_id, p_malzeme_id, p_proje_id, p_is_emri_id, p_kaydeden_id,
            v_hareket_tipi, p_miktar, COALESCE(p_birim_fiyat, v_birim_fiyat), p_aciklama
        );

        SET v_id = LAST_INSERT_ID();
    COMMIT;

    SELECT v_id AS hareket_id;
END //

CREATE TRIGGER trg_stok_giris_after_insert
AFTER INSERT ON stok_hareket
FOR EACH ROW
BEGIN
    IF NEW.hareket_tipi = 'GIRIS' THEN
        UPDATE malzeme
        SET stok_miktari = stok_miktari + NEW.miktar
        WHERE malzeme_id = NEW.malzeme_id
          AND firma_id = NEW.firma_id;
    ELSEIF NEW.hareket_tipi = 'CIKIS' THEN
        UPDATE malzeme
        SET stok_miktari = stok_miktari - NEW.miktar
        WHERE malzeme_id = NEW.malzeme_id
          AND firma_id = NEW.firma_id;
    END IF;
END //

CREATE TRIGGER trg_stok_hareket_koruma
BEFORE DELETE ON stok_hareket
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Stok hareketleri silinemez; duzeltme icin ters hareket giriniz.';
END //

DELIMITER ;
