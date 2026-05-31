USE santiyeos;

ALTER TABLE kullanici
    ADD COLUMN sifre_degistirmeli TINYINT(1) NOT NULL DEFAULT 0 AFTER aktif;

DROP PROCEDURE IF EXISTS sp_auth_kullanici_email_getir;
DROP PROCEDURE IF EXISTS sp_auth_kullanici_id_getir;
DROP PROCEDURE IF EXISTS sp_kullanici_sifre_guncelle;
DROP PROCEDURE IF EXISTS sp_kullanici_listele;
DROP PROCEDURE IF EXISTS sp_kullanici_getir;
DROP PROCEDURE IF EXISTS sp_kullanici_ekle;

DELIMITER //

CREATE PROCEDURE sp_auth_kullanici_email_getir(
    IN p_email VARCHAR(150)
)
BEGIN
    SELECT
        k.kullanici_id,
        k.firma_id,
        k.taseron_id,
        k.ad,
        k.soyad,
        k.email,
        k.sifre_hash,
        k.rol,
        k.telefon,
        k.aktif,
        k.sifre_degistirmeli,
        k.son_giris,
        k.created_at,
        k.updated_at
    FROM kullanici k
    LEFT JOIN firma f ON f.firma_id = k.firma_id
    WHERE LOWER(k.email) = LOWER(TRIM(p_email))
      AND k.aktif = 1
      AND (k.firma_id IS NULL OR f.aktif = 1)
    LIMIT 1;
END //

CREATE PROCEDURE sp_auth_kullanici_id_getir(
    IN p_kullanici_id INT
)
BEGIN
    SELECT
        k.kullanici_id,
        k.firma_id,
        k.taseron_id,
        k.ad,
        k.soyad,
        k.email,
        k.sifre_hash,
        k.rol,
        k.telefon,
        k.aktif,
        k.sifre_degistirmeli,
        k.son_giris,
        k.created_at,
        k.updated_at
    FROM kullanici k
    LEFT JOIN firma f ON f.firma_id = k.firma_id
    WHERE k.kullanici_id = p_kullanici_id
      AND k.aktif = 1
      AND (k.firma_id IS NULL OR f.aktif = 1)
    LIMIT 1;
END //

CREATE PROCEDURE sp_kullanici_sifre_guncelle(
    IN p_kullanici_id INT,
    IN p_sifre_hash VARCHAR(255),
    IN p_sifre_degistirmeli TINYINT
)
BEGIN
    UPDATE kullanici
    SET sifre_hash = p_sifre_hash,
        sifre_degistirmeli = COALESCE(p_sifre_degistirmeli, 0)
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_kullanici_listele(
    IN p_firma_id INT,
    IN p_rol VARCHAR(50),
    IN p_aktif TINYINT,
    IN p_limit INT,
    IN p_offset INT,
    OUT p_toplam INT
)
BEGIN
    SET p_limit = COALESCE(NULLIF(p_limit, 0), 20);
    SET p_offset = COALESCE(p_offset, 0);

    IF p_limit > 100 THEN SET p_limit = 100; END IF;
    IF p_offset < 0 THEN SET p_offset = 0; END IF;

    SELECT COUNT(*) INTO p_toplam
    FROM kullanici k
    WHERE (p_firma_id IS NULL OR k.firma_id = p_firma_id)
      AND (p_rol IS NULL OR k.rol = p_rol)
      AND (p_aktif IS NULL OR k.aktif = p_aktif);

    SELECT
        k.kullanici_id, k.firma_id, k.taseron_id,
        k.ad, k.soyad, k.email, k.rol, k.telefon,
        k.aktif, k.sifre_degistirmeli, k.son_giris, k.created_at, k.updated_at,
        f.ad AS firma_ad,
        t.ad AS taseron_ad
    FROM kullanici k
    LEFT JOIN firma f ON f.firma_id = k.firma_id
    LEFT JOIN taseron t ON t.taseron_id = k.taseron_id AND t.firma_id = k.firma_id
    WHERE (p_firma_id IS NULL OR k.firma_id = p_firma_id)
      AND (p_rol IS NULL OR k.rol = p_rol)
      AND (p_aktif IS NULL OR k.aktif = p_aktif)
    ORDER BY k.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END //

CREATE PROCEDURE sp_kullanici_getir(
    IN p_kullanici_id INT,
    IN p_firma_id INT
)
BEGIN
    SELECT
        k.kullanici_id, k.firma_id, k.taseron_id,
        k.ad, k.soyad, k.email, k.rol, k.telefon,
        k.aktif, k.sifre_degistirmeli, k.son_giris, k.created_at, k.updated_at,
        f.ad AS firma_ad,
        t.ad AS taseron_ad
    FROM kullanici k
    LEFT JOIN firma f ON f.firma_id = k.firma_id
    LEFT JOIN taseron t ON t.taseron_id = k.taseron_id AND t.firma_id = k.firma_id
    WHERE k.kullanici_id = p_kullanici_id
      AND (p_firma_id IS NULL OR k.firma_id = p_firma_id);
END //

CREATE PROCEDURE sp_kullanici_ekle(
    IN p_firma_id INT,
    IN p_taseron_id INT,
    IN p_ad VARCHAR(100),
    IN p_soyad VARCHAR(100),
    IN p_email VARCHAR(150),
    IN p_sifre_hash VARCHAR(255),
    IN p_rol VARCHAR(50),
    IN p_telefon VARCHAR(20)
)
BEGIN
    DECLARE v_id INT;

    IF p_rol NOT IN ('SUPER_ADMIN','FIRMA_ADMIN','PROJE_YONETICISI','SAHA_PERSONELI','TASERON_TEMSILCI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecersiz kullanici rolu.';
    END IF;

    IF p_rol <> 'SUPER_ADMIN' AND p_firma_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Firma bilgisi zorunludur.';
    END IF;

    IF p_rol = 'TASERON_TEMSILCI' AND p_taseron_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Taseron temsilcisi icin taseron zorunludur.';
    END IF;

    IF p_rol <> 'TASERON_TEMSILCI' AND p_taseron_id IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Sadece taseron temsilcisi taserona baglanabilir.';
    END IF;

    IF p_taseron_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM taseron
        WHERE taseron_id = p_taseron_id
          AND firma_id = p_firma_id
          AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Taseron bulunamadi veya pasif.';
    END IF;

    INSERT INTO kullanici (
        firma_id, taseron_id, ad, soyad, email, sifre_hash, rol, telefon, sifre_degistirmeli
    ) VALUES (
        p_firma_id, p_taseron_id, p_ad, p_soyad,
        LOWER(TRIM(p_email)), p_sifre_hash, p_rol, p_telefon,
        CASE WHEN p_rol = 'SUPER_ADMIN' THEN 0 ELSE 1 END
    );

    SET v_id = LAST_INSERT_ID();
    SELECT v_id AS kullanici_id;
END //

DELIMITER ;
