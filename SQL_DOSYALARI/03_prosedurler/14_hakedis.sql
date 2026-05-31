USE santiyeos;

DROP PROCEDURE IF EXISTS sp_santiyeos_hakedis_unique_fix;
DROP PROCEDURE IF EXISTS sp_hakedis_ekle;
DROP PROCEDURE IF EXISTS sp_hakedis_onayla;
DROP PROCEDURE IF EXISTS sp_hakedis_reddet;
DROP PROCEDURE IF EXISTS sp_hakedis_sil;
DROP PROCEDURE IF EXISTS sp_hakedis_listele;
DROP PROCEDURE IF EXISTS sp_hakedis_getir;

DROP TRIGGER IF EXISTS trg_hakedis_active_unique_insert;
DROP TRIGGER IF EXISTS trg_hakedis_active_unique_update;

DELIMITER //

CREATE PROCEDURE sp_santiyeos_hakedis_unique_fix()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'hakedis'
          AND index_name = 'uq_hakedis_emri'
    )
    AND NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'hakedis'
          AND index_name = 'idx_hakedis_is_emri_fk'
    ) THEN
        CREATE INDEX idx_hakedis_is_emri_fk ON hakedis (is_emri_id);
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'hakedis'
          AND index_name = 'uq_hakedis_emri'
    ) THEN
        ALTER TABLE hakedis DROP INDEX uq_hakedis_emri;
    END IF;
END //

CALL sp_santiyeos_hakedis_unique_fix() //
DROP PROCEDURE sp_santiyeos_hakedis_unique_fix //

CREATE TRIGGER trg_hakedis_active_unique_insert
BEFORE INSERT ON hakedis
FOR EACH ROW
BEGIN
    IF NEW.onay_durumu IN ('BEKLIYOR', 'ONAYLANDI', 'ITIRAZDA')
       AND EXISTS (
           SELECT 1
           FROM hakedis
           WHERE is_emri_id = NEW.is_emri_id
             AND onay_durumu IN ('BEKLIYOR', 'ONAYLANDI', 'ITIRAZDA')
           LIMIT 1
       )
    THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Bu is emri icin aktif hakedis zaten var.';
    END IF;
END //

CREATE TRIGGER trg_hakedis_active_unique_update
BEFORE UPDATE ON hakedis
FOR EACH ROW
BEGIN
    IF NEW.onay_durumu IN ('BEKLIYOR', 'ONAYLANDI', 'ITIRAZDA')
       AND EXISTS (
           SELECT 1
           FROM hakedis
           WHERE hakedis_id <> OLD.hakedis_id
             AND is_emri_id = NEW.is_emri_id
             AND onay_durumu IN ('BEKLIYOR', 'ONAYLANDI', 'ITIRAZDA')
           LIMIT 1
       )
    THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Bu is emri icin aktif hakedis zaten var.';
    END IF;
END //

CREATE PROCEDURE sp_hakedis_listele(
    IN p_firma_id INT,
    IN p_kullanici_id INT,
    IN p_rol VARCHAR(50),
    IN p_taseron_id INT,
    IN p_onay_durumu VARCHAR(50),
    IN p_limit INT,
    IN p_offset INT,
    OUT p_toplam INT
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    DECLARE v_onay_durumu VARCHAR(50);

    SET v_rol = UPPER(TRIM(p_rol));
    SET v_onay_durumu = UPPER(NULLIF(TRIM(p_onay_durumu), ''));
    SET p_limit = COALESCE(NULLIF(p_limit, 0), 20);
    SET p_offset = COALESCE(p_offset, 0);

    IF p_limit > 100 THEN SET p_limit = 100; END IF;
    IF p_offset < 0 THEN SET p_offset = 0; END IF;

    IF v_onay_durumu IS NOT NULL
       AND v_onay_durumu NOT IN ('BEKLIYOR', 'ONAYLANDI', 'REDDEDILDI', 'ITIRAZDA') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecersiz hakedis onay durumu.';
    END IF;

    SELECT COUNT(DISTINCT h.hakedis_id) INTO p_toplam
    FROM hakedis h
    INNER JOIN is_emri ie ON ie.is_emri_id = h.is_emri_id AND ie.firma_id = h.firma_id
    WHERE h.firma_id = p_firma_id
      AND (p_taseron_id IS NULL OR ie.taseron_id = p_taseron_id)
      AND (v_onay_durumu IS NULL OR h.onay_durumu = v_onay_durumu)
      AND (
          v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
          OR (v_rol = 'PROJE_YONETICISI'
              AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, ie.proje_id, h.firma_id) = 1)
          OR (v_rol = 'SAHA_PERSONELI' AND ie.atanan_kullanici_id = p_kullanici_id)
          OR (v_rol = 'TASERON_TEMSILCI' AND p_taseron_id IS NOT NULL AND ie.taseron_id = p_taseron_id)
      );

    SELECT
        h.hakedis_id,
        h.firma_id,
        h.is_emri_id,
        h.talep_eden_id,
        h.onaylayan_id,
        h.tutar,
        h.onay_durumu,
        h.onay_tarihi,
        h.aciklama,
        h.red_gerekce,
        h.created_at,
        h.updated_at,
        ie.baslik AS is_emri_baslik,
        ie.tamamlanma_tarihi,
        p.proje_id,
        p.ad AS proje_ad,
        t.taseron_id,
        t.ad AS taseron_ad,
        CONCAT(te.ad, ' ', te.soyad) AS talep_eden,
        CONCAT(oy.ad, ' ', oy.soyad) AS onaylayan,
        COALESCE(SUM(o.tutar), 0) AS odenen_tutar,
        h.tutar - COALESCE(SUM(o.tutar), 0) AS kalan_tutar
    FROM hakedis h
    INNER JOIN is_emri ie ON ie.is_emri_id = h.is_emri_id AND ie.firma_id = h.firma_id
    INNER JOIN proje p ON p.proje_id = ie.proje_id AND p.firma_id = h.firma_id
    INNER JOIN taseron t ON t.taseron_id = ie.taseron_id AND t.firma_id = h.firma_id
    INNER JOIN kullanici te ON te.kullanici_id = h.talep_eden_id
    LEFT JOIN kullanici oy ON oy.kullanici_id = h.onaylayan_id
    LEFT JOIN odeme o ON o.hakedis_id = h.hakedis_id AND o.firma_id = h.firma_id
    WHERE h.firma_id = p_firma_id
      AND (p_taseron_id IS NULL OR ie.taseron_id = p_taseron_id)
      AND (v_onay_durumu IS NULL OR h.onay_durumu = v_onay_durumu)
      AND (
          v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
          OR (v_rol = 'PROJE_YONETICISI'
              AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, ie.proje_id, h.firma_id) = 1)
          OR (v_rol = 'SAHA_PERSONELI' AND ie.atanan_kullanici_id = p_kullanici_id)
          OR (v_rol = 'TASERON_TEMSILCI' AND p_taseron_id IS NOT NULL AND ie.taseron_id = p_taseron_id)
      )
    GROUP BY
        h.hakedis_id, h.firma_id, h.is_emri_id, h.talep_eden_id,
        h.onaylayan_id, h.tutar, h.onay_durumu, h.onay_tarihi,
        h.aciklama, h.red_gerekce, h.created_at, h.updated_at,
        ie.baslik, ie.tamamlanma_tarihi, p.proje_id, p.ad,
        t.taseron_id, t.ad, te.ad, te.soyad, oy.ad, oy.soyad
    ORDER BY h.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END //

CREATE PROCEDURE sp_hakedis_getir(
    IN p_hakedis_id INT,
    IN p_firma_id INT,
    IN p_kullanici_id INT,
    IN p_rol VARCHAR(50),
    IN p_taseron_id INT
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    SET v_rol = UPPER(TRIM(p_rol));

    SELECT
        h.hakedis_id,
        h.firma_id,
        h.is_emri_id,
        h.talep_eden_id,
        h.onaylayan_id,
        h.tutar,
        h.onay_durumu,
        h.onay_tarihi,
        h.aciklama,
        h.red_gerekce,
        h.created_at,
        h.updated_at,
        ie.baslik AS is_emri_baslik,
        ie.tamamlanma_tarihi,
        p.proje_id,
        p.ad AS proje_ad,
        t.taseron_id,
        t.ad AS taseron_ad,
        CONCAT(te.ad, ' ', te.soyad) AS talep_eden,
        CONCAT(oy.ad, ' ', oy.soyad) AS onaylayan,
        COALESCE(SUM(o.tutar), 0) AS odenen_tutar,
        h.tutar - COALESCE(SUM(o.tutar), 0) AS kalan_tutar
    FROM hakedis h
    INNER JOIN is_emri ie ON ie.is_emri_id = h.is_emri_id AND ie.firma_id = h.firma_id
    INNER JOIN proje p ON p.proje_id = ie.proje_id AND p.firma_id = h.firma_id
    INNER JOIN taseron t ON t.taseron_id = ie.taseron_id AND t.firma_id = h.firma_id
    INNER JOIN kullanici te ON te.kullanici_id = h.talep_eden_id
    LEFT JOIN kullanici oy ON oy.kullanici_id = h.onaylayan_id
    LEFT JOIN odeme o ON o.hakedis_id = h.hakedis_id AND o.firma_id = h.firma_id
    WHERE h.hakedis_id = p_hakedis_id
      AND h.firma_id = p_firma_id
      AND (p_taseron_id IS NULL OR ie.taseron_id = p_taseron_id)
      AND (
          v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
          OR (v_rol = 'PROJE_YONETICISI'
              AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, ie.proje_id, h.firma_id) = 1)
          OR (v_rol = 'SAHA_PERSONELI' AND ie.atanan_kullanici_id = p_kullanici_id)
          OR (v_rol = 'TASERON_TEMSILCI' AND p_taseron_id IS NOT NULL AND ie.taseron_id = p_taseron_id)
      )
    GROUP BY
        h.hakedis_id, h.firma_id, h.is_emri_id, h.talep_eden_id,
        h.onaylayan_id, h.tutar, h.onay_durumu, h.onay_tarihi,
        h.aciklama, h.red_gerekce, h.created_at, h.updated_at,
        ie.baslik, ie.tamamlanma_tarihi, p.proje_id, p.ad,
        t.taseron_id, t.ad, te.ad, te.soyad, oy.ad, oy.soyad;
END //

CREATE PROCEDURE sp_hakedis_ekle(
    IN p_firma_id INT,
    IN p_is_emri_id INT,
    IN p_talep_eden_id INT,
    IN p_rol VARCHAR(50),
    IN p_tutar DECIMAL(15,2),
    IN p_aciklama TEXT
)
BEGIN
    DECLARE v_id INT;
    DECLARE v_durum VARCHAR(50);
    DECLARE v_proje_id INT;
    DECLARE v_taseron_id INT;
    DECLARE v_kullanici_taseron_id INT;
    DECLARE v_rol VARCHAR(50);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    SET v_rol = UPPER(TRIM(p_rol));

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI', 'TASERON_TEMSILCI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF p_tutar IS NULL OR p_tutar <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hakedis tutari sifirdan buyuk olmalidir.';
    END IF;

    SELECT ie.durum, ie.proje_id, ie.taseron_id
    INTO v_durum, v_proje_id, v_taseron_id
    FROM is_emri ie
    WHERE ie.is_emri_id = p_is_emri_id
      AND ie.firma_id = p_firma_id;

    IF v_proje_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Is emri bulunamadi.';
    END IF;

    IF v_durum <> 'TAMAMLANDI' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Hakedis sadece tamamlanmis is emirleri icin olusturulabilir.';
    END IF;

    IF v_rol = 'PROJE_YONETICISI'
       AND fn_kullanici_proje_yetkili_mi(p_talep_eden_id, v_proje_id, p_firma_id) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu is emri icin yetkiniz yok.';
    END IF;

    IF v_rol = 'TASERON_TEMSILCI' THEN
        SELECT taseron_id INTO v_kullanici_taseron_id
        FROM kullanici
        WHERE kullanici_id = p_talep_eden_id
          AND firma_id = p_firma_id;

        IF v_kullanici_taseron_id IS NULL OR v_kullanici_taseron_id <> v_taseron_id THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu is emri bu taseron kullanicisina ait degil.';
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM hakedis
        WHERE firma_id = p_firma_id
          AND is_emri_id = p_is_emri_id
          AND onay_durumu IN ('BEKLIYOR', 'ONAYLANDI', 'ITIRAZDA')
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Bu is emri icin aktif hakedis zaten var.';
    END IF;

    START TRANSACTION;
        INSERT INTO hakedis (firma_id, is_emri_id, talep_eden_id, tutar, aciklama)
        VALUES (p_firma_id, p_is_emri_id, p_talep_eden_id, p_tutar, p_aciklama);

        SET v_id = LAST_INSERT_ID();

        UPDATE is_emri
        SET durum = 'HAKEDISTE'
        WHERE is_emri_id = p_is_emri_id
          AND firma_id = p_firma_id;

        INSERT INTO is_emri_durum_log (
            firma_id, is_emri_id, yapan_id, eski_durum, yeni_durum, aciklama
        ) VALUES (
            p_firma_id, p_is_emri_id, p_talep_eden_id,
            'TAMAMLANDI', 'HAKEDISTE', 'Hakedis talebi olusturuldu'
        );

        INSERT INTO hakedis_log (
            firma_id, hakedis_id, islem, yapan_id, yeni_durum
        ) VALUES (
            p_firma_id, v_id, 'OLUSTURULDU', p_talep_eden_id, 'BEKLIYOR'
        );
    COMMIT;

    SELECT v_id AS hakedis_id;
END //

CREATE PROCEDURE sp_hakedis_onayla(
    IN p_hakedis_id INT,
    IN p_firma_id INT,
    IN p_onaylayan_id INT,
    IN p_rol VARCHAR(50)
)
BEGIN
    DECLARE v_eski VARCHAR(50);
    DECLARE v_proje_id INT;
    DECLARE v_rol VARCHAR(50);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    SET v_rol = UPPER(TRIM(p_rol));

    SELECT h.onay_durumu, ie.proje_id
    INTO v_eski, v_proje_id
    FROM hakedis h
    INNER JOIN is_emri ie ON ie.is_emri_id = h.is_emri_id AND ie.firma_id = h.firma_id
    WHERE h.hakedis_id = p_hakedis_id
      AND h.firma_id = p_firma_id;

    IF v_proje_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hakedis bulunamadi.';
    END IF;

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF v_rol = 'PROJE_YONETICISI'
       AND fn_kullanici_proje_yetkili_mi(p_onaylayan_id, v_proje_id, p_firma_id) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu proje icin yetkiniz yok.';
    END IF;

    IF v_eski NOT IN ('BEKLIYOR', 'ITIRAZDA') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Sadece bekleyen veya itirazdaki hakedis onaylanabilir.';
    END IF;

    START TRANSACTION;
        UPDATE hakedis
        SET onay_durumu = 'ONAYLANDI',
            onaylayan_id = p_onaylayan_id,
            onay_tarihi = NOW(),
            red_gerekce = NULL
        WHERE hakedis_id = p_hakedis_id
          AND firma_id = p_firma_id;

        INSERT INTO hakedis_log (
            firma_id, hakedis_id, islem, yapan_id, eski_durum, yeni_durum
        ) VALUES (
            p_firma_id, p_hakedis_id, 'ONAYLANDI',
            p_onaylayan_id, v_eski, 'ONAYLANDI'
        );
    COMMIT;

    SELECT 1 AS etkilenen_satir;
END //

CREATE PROCEDURE sp_hakedis_reddet(
    IN p_hakedis_id INT,
    IN p_firma_id INT,
    IN p_onaylayan_id INT,
    IN p_rol VARCHAR(50),
    IN p_red_gerekce TEXT
)
BEGIN
    DECLARE v_eski VARCHAR(50);
    DECLARE v_is_emri_id INT;
    DECLARE v_proje_id INT;
    DECLARE v_rol VARCHAR(50);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    SET v_rol = UPPER(TRIM(p_rol));

    IF p_red_gerekce IS NULL OR TRIM(p_red_gerekce) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Red gerekcesi zorunludur.';
    END IF;

    SELECT h.onay_durumu, h.is_emri_id, ie.proje_id
    INTO v_eski, v_is_emri_id, v_proje_id
    FROM hakedis h
    INNER JOIN is_emri ie ON ie.is_emri_id = h.is_emri_id AND ie.firma_id = h.firma_id
    WHERE h.hakedis_id = p_hakedis_id
      AND h.firma_id = p_firma_id;

    IF v_proje_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hakedis bulunamadi.';
    END IF;

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF v_rol = 'PROJE_YONETICISI'
       AND fn_kullanici_proje_yetkili_mi(p_onaylayan_id, v_proje_id, p_firma_id) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu proje icin yetkiniz yok.';
    END IF;

    IF v_eski NOT IN ('BEKLIYOR', 'ITIRAZDA') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Sadece bekleyen veya itirazdaki hakedis reddedilebilir.';
    END IF;

    START TRANSACTION;
        UPDATE hakedis
        SET onay_durumu = 'REDDEDILDI',
            onaylayan_id = p_onaylayan_id,
            onay_tarihi = NOW(),
            red_gerekce = p_red_gerekce
        WHERE hakedis_id = p_hakedis_id
          AND firma_id = p_firma_id;

        UPDATE is_emri
        SET durum = 'TAMAMLANDI'
        WHERE is_emri_id = v_is_emri_id
          AND firma_id = p_firma_id;

        INSERT INTO is_emri_durum_log (
            firma_id, is_emri_id, yapan_id, eski_durum, yeni_durum, aciklama
        ) VALUES (
            p_firma_id, v_is_emri_id, p_onaylayan_id,
            'HAKEDISTE', 'TAMAMLANDI', 'Hakedis reddedildi, is emri tekrar acildi'
        );

        INSERT INTO hakedis_log (
            firma_id, hakedis_id, islem, yapan_id, eski_durum, yeni_durum, aciklama
        ) VALUES (
            p_firma_id, p_hakedis_id, 'REDDEDILDI',
            p_onaylayan_id, v_eski, 'REDDEDILDI', p_red_gerekce
        );
    COMMIT;

    SELECT 1 AS etkilenen_satir;
END //

CREATE PROCEDURE sp_hakedis_sil(
    IN p_hakedis_id INT,
    IN p_firma_id INT,
    IN p_kullanici_id INT,
    IN p_rol VARCHAR(50)
)
BEGIN
    DECLARE v_durum VARCHAR(50);
    DECLARE v_is_emri_id INT;
    DECLARE v_rol VARCHAR(50);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    SET v_rol = UPPER(TRIM(p_rol));

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    SELECT onay_durumu, is_emri_id
    INTO v_durum, v_is_emri_id
    FROM hakedis
    WHERE hakedis_id = p_hakedis_id
      AND firma_id = p_firma_id;

    IF v_durum IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hakedis bulunamadi.';
    END IF;

    IF v_durum = 'ONAYLANDI' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Onaylanmis hakedis silinemez.';
    END IF;

    START TRANSACTION;
        IF v_durum IN ('BEKLIYOR', 'ITIRAZDA') THEN
            UPDATE is_emri
            SET durum = 'TAMAMLANDI'
            WHERE is_emri_id = v_is_emri_id
              AND firma_id = p_firma_id;
        END IF;

        DELETE FROM hakedis
        WHERE hakedis_id = p_hakedis_id
          AND firma_id = p_firma_id;
    COMMIT;

    SELECT 1 AS etkilenen_satir;
END //

DELIMITER ;
