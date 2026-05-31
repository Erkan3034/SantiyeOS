USE santiyeos;

DROP PROCEDURE IF EXISTS sp_taseron_ekle;
DROP PROCEDURE IF EXISTS sp_taseron_guncelle;
DROP PROCEDURE IF EXISTS sp_taseron_sil;
DROP PROCEDURE IF EXISTS sp_taseron_listele;
DROP PROCEDURE IF EXISTS sp_taseron_getir;

DELIMITER //

CREATE PROCEDURE sp_taseron_ekle(
    IN p_firma_id   INT,
    IN p_ad         VARCHAR(200),
    IN p_vergi_no   VARCHAR(20),
    IN p_yetkili    VARCHAR(200),
    IN p_telefon    VARCHAR(20),
    IN p_email      VARCHAR(150),
    IN p_uzmanlik   VARCHAR(200)
)
BEGIN
    DECLARE v_taseron_id INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_firma_id IS NULL OR NOT EXISTS (
        SELECT 1 FROM firma WHERE firma_id = p_firma_id AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Firma bulunamadi veya pasif.';
    END IF;

    IF p_ad IS NULL OR TRIM(p_ad) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Taseron adi zorunludur.';
    END IF;

    IF p_vergi_no IS NOT NULL AND TRIM(p_vergi_no) <> '' AND EXISTS (
        SELECT 1 FROM taseron
        WHERE firma_id = p_firma_id
          AND vergi_no = p_vergi_no
          AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu vergi numarasi ile kayitli aktif taseron zaten var.';
    END IF;

    START TRANSACTION;
        INSERT INTO taseron (
            firma_id, ad, vergi_no, yetkili_ad, telefon, email, uzmanlik
        ) VALUES (
            p_firma_id, TRIM(p_ad), NULLIF(TRIM(p_vergi_no), ''),
            NULLIF(TRIM(p_yetkili), ''), NULLIF(TRIM(p_telefon), ''),
            NULLIF(TRIM(p_email), ''), NULLIF(TRIM(p_uzmanlik), '')
        );

        SET v_taseron_id = LAST_INSERT_ID();
    COMMIT;

    SELECT v_taseron_id AS taseron_id;
END //

CREATE PROCEDURE sp_taseron_guncelle(
    IN p_taseron_id INT,
    IN p_firma_id   INT,
    IN p_ad         VARCHAR(200),
    IN p_vergi_no   VARCHAR(20),
    IN p_yetkili    VARCHAR(200),
    IN p_telefon    VARCHAR(20),
    IN p_email      VARCHAR(150),
    IN p_uzmanlik   VARCHAR(200)
)
BEGIN
    IF p_taseron_id IS NULL OR p_taseron_id <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecerli bir taseron id giriniz.';
    END IF;

    IF p_firma_id IS NULL OR NOT EXISTS (
        SELECT 1 FROM firma WHERE firma_id = p_firma_id AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Firma bulunamadi veya pasif.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM taseron
        WHERE taseron_id = p_taseron_id
          AND firma_id = p_firma_id
          AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Taseron bulunamadi.';
    END IF;

    IF p_ad IS NULL OR TRIM(p_ad) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Taseron adi zorunludur.';
    END IF;

    IF p_vergi_no IS NOT NULL AND TRIM(p_vergi_no) <> '' AND EXISTS (
        SELECT 1 FROM taseron
        WHERE firma_id = p_firma_id
          AND taseron_id <> p_taseron_id
          AND vergi_no = p_vergi_no
          AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu vergi numarasi ile kayitli aktif taseron zaten var.';
    END IF;

    UPDATE taseron
    SET ad = TRIM(p_ad),
        vergi_no = NULLIF(TRIM(p_vergi_no), ''),
        yetkili_ad = NULLIF(TRIM(p_yetkili), ''),
        telefon = NULLIF(TRIM(p_telefon), ''),
        email = NULLIF(TRIM(p_email), ''),
        uzmanlik = NULLIF(TRIM(p_uzmanlik), '')
    WHERE taseron_id = p_taseron_id
      AND firma_id = p_firma_id
      AND aktif = 1;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_taseron_sil(
    IN p_taseron_id     INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT
)
BEGIN
    IF NOT fn_kullanici_yetki_kontrol_firma(
        p_kullanici_id,
        p_firma_id,
        'SUPER_ADMIN,FIRMA_ADMIN'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM taseron
        WHERE taseron_id = p_taseron_id
          AND firma_id = p_firma_id
          AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Taseron bulunamadi.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM is_emri
        WHERE taseron_id = p_taseron_id
          AND firma_id = p_firma_id
          AND durum NOT IN ('TAMAMLANDI', 'IPTAL')
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Aktif is emirleri olan taseron silinemez.';
    END IF;

    UPDATE taseron
    SET aktif = 0
    WHERE taseron_id = p_taseron_id
      AND firma_id = p_firma_id
      AND aktif = 1;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_taseron_listele(
    IN p_firma_id   INT,
    IN p_limit      INT,
    IN p_offset     INT,
    OUT p_toplam    INT
)
BEGIN
    SET p_limit = IFNULL(NULLIF(p_limit, 0), 20);
    SET p_offset = IFNULL(p_offset, 0);

    IF p_limit < 0 THEN SET p_limit = 20; END IF;
    IF p_limit > 100 THEN SET p_limit = 100; END IF;
    IF p_offset < 0 THEN SET p_offset = 0; END IF;

    SELECT COUNT(*) INTO p_toplam
    FROM taseron
    WHERE (p_firma_id IS NULL OR firma_id = p_firma_id)
      AND aktif = 1;

    SELECT
        t.taseron_id,
        t.firma_id,
        t.ad,
        t.vergi_no,
        t.yetkili_ad,
        t.telefon,
        t.email,
        t.uzmanlik,
        t.performans_skoru,
        t.aktif,
        t.created_at,
        t.updated_at,
        COUNT(DISTINCT ie.is_emri_id) AS toplam_is,
        COUNT(DISTINCT CASE WHEN ie.durum = 'TAMAMLANDI' THEN ie.is_emri_id END) AS tamamlanan_is,
        COALESCE(SUM(CASE WHEN h.onay_durumu = 'ONAYLANDI' THEN h.tutar ELSE 0 END), 0) AS toplam_hakedis,
        COALESCE(SUM(o.tutar), 0) AS toplam_odeme,
        fn_taseron_bakiye(t.taseron_id, t.firma_id) AS odenmemis_bakiye
    FROM taseron t
    LEFT JOIN is_emri ie ON ie.taseron_id = t.taseron_id AND ie.firma_id = t.firma_id
    LEFT JOIN hakedis h  ON h.is_emri_id = ie.is_emri_id AND h.firma_id = t.firma_id
    LEFT JOIN odeme o    ON o.hakedis_id = h.hakedis_id AND o.firma_id = t.firma_id
    WHERE (p_firma_id IS NULL OR t.firma_id = p_firma_id)
      AND t.aktif = 1
    GROUP BY
        t.taseron_id, t.firma_id, t.ad, t.vergi_no, t.yetkili_ad,
        t.telefon, t.email, t.uzmanlik, t.performans_skoru,
        t.aktif, t.created_at, t.updated_at
    ORDER BY t.performans_skoru DESC
    LIMIT p_limit OFFSET p_offset;
END //

CREATE PROCEDURE sp_taseron_getir(
    IN p_taseron_id INT,
    IN p_firma_id   INT
)
BEGIN
    IF p_taseron_id IS NULL OR p_taseron_id <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecerli bir taseron id giriniz.';
    END IF;

    SELECT
        t.taseron_id,
        t.firma_id,
        t.ad,
        t.vergi_no,
        t.yetkili_ad,
        t.telefon,
        t.email,
        t.uzmanlik,
        t.performans_skoru,
        t.aktif,
        t.created_at,
        t.updated_at,
        fn_taseron_bakiye(t.taseron_id, t.firma_id) AS odenmemis_bakiye
    FROM taseron t
    WHERE t.taseron_id = p_taseron_id
      AND (p_firma_id IS NULL OR t.firma_id = p_firma_id)
      AND t.aktif = 1;
END //

DELIMITER ;
