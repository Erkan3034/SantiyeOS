USE santiyeos;

DROP PROCEDURE IF EXISTS sp_odeme_ekle;
DROP PROCEDURE IF EXISTS sp_odeme_sil;
DROP PROCEDURE IF EXISTS sp_odeme_listele;
DROP PROCEDURE IF EXISTS sp_odeme_getir;

DELIMITER //

CREATE PROCEDURE sp_odeme_ekle(
    IN p_firma_id       INT,
    IN p_hakedis_id     INT,
    IN p_kaydeden_id    INT,
    IN p_rol            VARCHAR(50),
    IN p_tutar          DECIMAL(15,2),
    IN p_tarih          DATE,
    IN p_yontem         VARCHAR(20),
    IN p_aciklama       TEXT
)
BEGIN
    DECLARE v_id INT;
    DECLARE v_hakedis_tutar DECIMAL(15,2);
    DECLARE v_odenen_tutar DECIMAL(15,2);
    DECLARE v_onay_durumu VARCHAR(50);
    DECLARE v_yontem VARCHAR(20);
    DECLARE v_rol VARCHAR(50);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    SET v_rol = UPPER(TRIM(p_rol));
    SET v_yontem = UPPER(COALESCE(NULLIF(TRIM(p_yontem), ''), 'HAVALE'));

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF p_tutar IS NULL OR p_tutar <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Odeme tutari sifirdan buyuk olmalidir.';
    END IF;

    IF v_yontem NOT IN ('HAVALE', 'EFT', 'CEK', 'NAKIT') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecersiz odeme yontemi.';
    END IF;

    SELECT h.tutar, h.onay_durumu, COALESCE(SUM(o.tutar), 0)
    INTO v_hakedis_tutar, v_onay_durumu, v_odenen_tutar
    FROM hakedis h
    LEFT JOIN odeme o ON o.hakedis_id = h.hakedis_id AND o.firma_id = h.firma_id
    WHERE h.hakedis_id = p_hakedis_id
      AND h.firma_id = p_firma_id
    GROUP BY h.hakedis_id, h.tutar, h.onay_durumu;

    IF v_hakedis_tutar IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hakedis bulunamadi.';
    END IF;

    IF v_onay_durumu <> 'ONAYLANDI' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Sadece onaylanmis hakedislere odeme yapilabilir.';
    END IF;

    IF (v_odenen_tutar + p_tutar) > v_hakedis_tutar THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Odeme tutari hakedis tutarini asamaz.';
    END IF;

    START TRANSACTION;
        INSERT INTO odeme (
            firma_id, hakedis_id, kaydeden_id,
            tutar, odeme_tarihi, odeme_yontemi, aciklama
        ) VALUES (
            p_firma_id, p_hakedis_id, p_kaydeden_id,
            p_tutar, COALESCE(p_tarih, CURDATE()), v_yontem, p_aciklama
        );

        SET v_id = LAST_INSERT_ID();
    COMMIT;

    SELECT v_id AS odeme_id;
END //

CREATE PROCEDURE sp_odeme_sil(
    IN p_odeme_id       INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50)
)
BEGIN
    DECLARE v_rol VARCHAR(50);

    SET v_rol = UPPER(TRIM(p_rol));

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM odeme
        WHERE odeme_id = p_odeme_id
          AND firma_id = p_firma_id
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Odeme bulunamadi.';
    END IF;

    DELETE FROM odeme
    WHERE odeme_id = p_odeme_id
      AND firma_id = p_firma_id;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_odeme_listele(
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50),
    IN p_taseron_id     INT,
    IN p_hakedis_id     INT,
    IN p_limit          INT,
    IN p_offset         INT,
    OUT p_toplam        INT
)
BEGIN
    DECLARE v_rol VARCHAR(50);

    SET v_rol = UPPER(TRIM(p_rol));
    SET p_limit = COALESCE(NULLIF(p_limit, 0), 20);
    SET p_offset = COALESCE(p_offset, 0);

    IF p_limit > 100 THEN SET p_limit = 100; END IF;
    IF p_offset < 0 THEN SET p_offset = 0; END IF;

    SELECT COUNT(DISTINCT o.odeme_id) INTO p_toplam
    FROM odeme o
    INNER JOIN hakedis h ON h.hakedis_id = o.hakedis_id AND h.firma_id = o.firma_id
    INNER JOIN is_emri ie ON ie.is_emri_id = h.is_emri_id AND ie.firma_id = o.firma_id
    WHERE o.firma_id = p_firma_id
      AND (p_hakedis_id IS NULL OR o.hakedis_id = p_hakedis_id)
      AND (p_taseron_id IS NULL OR ie.taseron_id = p_taseron_id)
      AND (
          v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
          OR (v_rol = 'PROJE_YONETICISI'
              AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, ie.proje_id, o.firma_id) = 1)
          OR (v_rol = 'SAHA_PERSONELI' AND ie.atanan_kullanici_id = p_kullanici_id)
          OR (v_rol = 'TASERON_TEMSILCI' AND p_taseron_id IS NOT NULL AND ie.taseron_id = p_taseron_id)
      );

    SELECT
        o.odeme_id,
        o.firma_id,
        o.hakedis_id,
        o.kaydeden_id,
        o.tutar,
        o.odeme_tarihi,
        o.odeme_yontemi,
        o.aciklama,
        o.created_at,
        h.tutar AS hakedis_tutari,
        h.onay_durumu AS hakedis_onay_durumu,
        ie.is_emri_id,
        ie.baslik AS is_emri_baslik,
        p.proje_id,
        p.ad AS proje_ad,
        t.taseron_id,
        t.ad AS taseron_ad,
        CONCAT(k.ad, ' ', k.soyad) AS kaydeden,
        COALESCE((
            SELECT SUM(o2.tutar)
            FROM odeme o2
            WHERE o2.hakedis_id = h.hakedis_id
              AND o2.firma_id = h.firma_id
        ), 0) AS toplam_odenen,
        h.tutar - COALESCE((
            SELECT SUM(o3.tutar)
            FROM odeme o3
            WHERE o3.hakedis_id = h.hakedis_id
              AND o3.firma_id = h.firma_id
        ), 0) AS kalan_tutar
    FROM odeme o
    INNER JOIN hakedis h ON h.hakedis_id = o.hakedis_id AND h.firma_id = o.firma_id
    INNER JOIN is_emri ie ON ie.is_emri_id = h.is_emri_id AND ie.firma_id = o.firma_id
    INNER JOIN proje p ON p.proje_id = ie.proje_id AND p.firma_id = o.firma_id
    INNER JOIN taseron t ON t.taseron_id = ie.taseron_id AND t.firma_id = o.firma_id
    LEFT JOIN kullanici k ON k.kullanici_id = o.kaydeden_id
    WHERE o.firma_id = p_firma_id
      AND (p_hakedis_id IS NULL OR o.hakedis_id = p_hakedis_id)
      AND (p_taseron_id IS NULL OR ie.taseron_id = p_taseron_id)
      AND (
          v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
          OR (v_rol = 'PROJE_YONETICISI'
              AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, ie.proje_id, o.firma_id) = 1)
          OR (v_rol = 'SAHA_PERSONELI' AND ie.atanan_kullanici_id = p_kullanici_id)
          OR (v_rol = 'TASERON_TEMSILCI' AND p_taseron_id IS NOT NULL AND ie.taseron_id = p_taseron_id)
      )
    ORDER BY o.odeme_tarihi DESC, o.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END //

CREATE PROCEDURE sp_odeme_getir(
    IN p_odeme_id       INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50),
    IN p_taseron_id     INT
)
BEGIN
    DECLARE v_rol VARCHAR(50);

    SET v_rol = UPPER(TRIM(p_rol));

    SELECT
        o.odeme_id,
        o.firma_id,
        o.hakedis_id,
        o.kaydeden_id,
        o.tutar,
        o.odeme_tarihi,
        o.odeme_yontemi,
        o.aciklama,
        o.created_at,
        h.tutar AS hakedis_tutari,
        h.onay_durumu AS hakedis_onay_durumu,
        ie.is_emri_id,
        ie.baslik AS is_emri_baslik,
        p.proje_id,
        p.ad AS proje_ad,
        t.taseron_id,
        t.ad AS taseron_ad,
        CONCAT(k.ad, ' ', k.soyad) AS kaydeden,
        COALESCE((
            SELECT SUM(o2.tutar)
            FROM odeme o2
            WHERE o2.hakedis_id = h.hakedis_id
              AND o2.firma_id = h.firma_id
        ), 0) AS toplam_odenen,
        h.tutar - COALESCE((
            SELECT SUM(o3.tutar)
            FROM odeme o3
            WHERE o3.hakedis_id = h.hakedis_id
              AND o3.firma_id = h.firma_id
        ), 0) AS kalan_tutar
    FROM odeme o
    INNER JOIN hakedis h ON h.hakedis_id = o.hakedis_id AND h.firma_id = o.firma_id
    INNER JOIN is_emri ie ON ie.is_emri_id = h.is_emri_id AND ie.firma_id = o.firma_id
    INNER JOIN proje p ON p.proje_id = ie.proje_id AND p.firma_id = o.firma_id
    INNER JOIN taseron t ON t.taseron_id = ie.taseron_id AND t.firma_id = o.firma_id
    LEFT JOIN kullanici k ON k.kullanici_id = o.kaydeden_id
    WHERE o.odeme_id = p_odeme_id
      AND o.firma_id = p_firma_id
      AND (p_taseron_id IS NULL OR ie.taseron_id = p_taseron_id)
      AND (
          v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
          OR (v_rol = 'PROJE_YONETICISI'
              AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, ie.proje_id, o.firma_id) = 1)
          OR (v_rol = 'SAHA_PERSONELI' AND ie.atanan_kullanici_id = p_kullanici_id)
          OR (v_rol = 'TASERON_TEMSILCI' AND p_taseron_id IS NOT NULL AND ie.taseron_id = p_taseron_id)
      );
END //

DELIMITER ;