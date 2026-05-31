USE santiyeos;

DELIMITER //

DROP PROCEDURE IF EXISTS sp_abonelik_plan_listele //
CREATE PROCEDURE sp_abonelik_plan_listele(
    IN p_aktif TINYINT
)
BEGIN
    SELECT
        plan_id,
        ad,
        max_proje,
        max_kullanici,
        max_taseron,
        aylik_ucret,
        aktif
    FROM abonelik_plan
    WHERE p_aktif IS NULL OR aktif = p_aktif
    ORDER BY aylik_ucret ASC, plan_id ASC;
END //

DROP PROCEDURE IF EXISTS sp_abonelik_plan_getir //
CREATE PROCEDURE sp_abonelik_plan_getir(
    IN p_plan_id INT
)
BEGIN
    SELECT
        plan_id,
        ad,
        max_proje,
        max_kullanici,
        max_taseron,
        aylik_ucret,
        aktif
    FROM abonelik_plan
    WHERE plan_id = p_plan_id;
END //

DELIMITER ;


-- //////////////////////////////////////
DELIMITER //

DROP PROCEDURE IF EXISTS sp_abonelik_aktif_getir //
CREATE PROCEDURE sp_abonelik_aktif_getir(
    IN p_firma_id INT
)
BEGIN
    SELECT
        a.abonelik_id,
        a.firma_id,
        f.ad AS firma_ad,
        a.plan_id,
        p.ad AS plan_ad,
        p.max_proje,
        p.max_kullanici,
        p.max_taseron,
        p.aylik_ucret,
        a.baslangic_tarihi,
        a.bitis_tarihi,
        a.durum,
        a.deneme,
        a.created_at
    FROM abonelik a
    INNER JOIN firma f ON f.firma_id = a.firma_id
    INNER JOIN abonelik_plan p ON p.plan_id = a.plan_id
    WHERE a.firma_id = p_firma_id
      AND a.durum = 'AKTIF'
    ORDER BY a.bitis_tarihi DESC, a.abonelik_id DESC
    LIMIT 1;
END //

DROP PROCEDURE IF EXISTS sp_abonelik_listele //
CREATE PROCEDURE sp_abonelik_listele(
    IN p_firma_id INT,
    IN p_limit INT,
    IN p_offset INT,
    OUT p_toplam INT
)
BEGIN
    SELECT COUNT(*) INTO p_toplam
    FROM abonelik
    WHERE p_firma_id IS NULL OR firma_id = p_firma_id;

    SELECT
        a.abonelik_id,
        a.firma_id,
        f.ad AS firma_ad,
        a.plan_id,
        p.ad AS plan_ad,
        p.max_proje,
        p.max_kullanici,
        p.max_taseron,
        p.aylik_ucret,
        a.baslangic_tarihi,
        a.bitis_tarihi,
        a.durum,
        a.deneme,
        a.created_at
    FROM abonelik a
    INNER JOIN firma f ON f.firma_id = a.firma_id
    INNER JOIN abonelik_plan p ON p.plan_id = a.plan_id
    WHERE p_firma_id IS NULL OR a.firma_id = p_firma_id
    ORDER BY a.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END //

DROP PROCEDURE IF EXISTS sp_abonelik_baslat //
CREATE PROCEDURE sp_abonelik_baslat(
    IN p_firma_id INT,
    IN p_plan_id INT,
    IN p_baslangic_tarihi DATE,
    IN p_bitis_tarihi DATE,
    IN p_deneme TINYINT
)
BEGIN
    DECLARE v_abonelik_id INT;

    IF NOT EXISTS (
        SELECT 1 FROM firma
        WHERE firma_id = p_firma_id
          AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Firma bulunamadi veya pasif.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM abonelik_plan
        WHERE plan_id = p_plan_id
          AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Abonelik plani bulunamadi veya pasif.';
    END IF;

    IF p_bitis_tarihi <= p_baslangic_tarihi THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Abonelik bitis tarihi baslangic tarihinden sonra olmalidir.';
    END IF;

    UPDATE abonelik
    SET durum = 'IPTAL'
    WHERE firma_id = p_firma_id
      AND durum = 'AKTIF';

    INSERT INTO abonelik (
        firma_id,
        plan_id,
        baslangic_tarihi,
        bitis_tarihi,
        durum,
        deneme
    ) VALUES (
        p_firma_id,
        p_plan_id,
        p_baslangic_tarihi,
        p_bitis_tarihi,
        'AKTIF',
        COALESCE(p_deneme, 0)
    );

    SET v_abonelik_id = LAST_INSERT_ID();

    SELECT v_abonelik_id AS abonelik_id;
END //

DROP PROCEDURE IF EXISTS sp_abonelik_iptal //
CREATE PROCEDURE sp_abonelik_iptal(
    IN p_abonelik_id INT,
    IN p_firma_id INT
)
BEGIN
    UPDATE abonelik
    SET durum = 'IPTAL'
    WHERE abonelik_id = p_abonelik_id
      AND (p_firma_id IS NULL OR firma_id = p_firma_id)
      AND durum = 'AKTIF';

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

DELIMITER ;
