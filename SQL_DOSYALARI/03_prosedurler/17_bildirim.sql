USE santiyeos;

DROP PROCEDURE IF EXISTS sp_bildirim_ekle;
DROP PROCEDURE IF EXISTS sp_bildirim_getir;
DROP PROCEDURE IF EXISTS sp_bildirim_okundu_isaretle;
DROP PROCEDURE IF EXISTS sp_bildirim_tumunu_okundu_isaretle;
DROP PROCEDURE IF EXISTS sp_bildirim_sil;
DROP PROCEDURE IF EXISTS sp_bildirim_listele;

DELIMITER //

CREATE PROCEDURE sp_bildirim_ekle(
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_olusturan_id   INT,
    IN p_baslik         VARCHAR(200),
    IN p_mesaj          TEXT,
    IN p_tip            VARCHAR(50),
    IN p_referans_tablo VARCHAR(50),
    IN p_referans_id    INT
)
BEGIN
    DECLARE v_id INT;
    DECLARE v_tip VARCHAR(50);

    SET v_tip = UPPER(NULLIF(TRIM(p_tip), ''));

    IF NOT fn_kullanici_yetki_kontrol_firma(
        p_olusturan_id,
        p_firma_id,
        'SUPER_ADMIN,FIRMA_ADMIN'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM kullanici
        WHERE kullanici_id = p_kullanici_id
          AND firma_id = p_firma_id
          AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hedef kullanici bulunamadi veya pasif.';
    END IF;

    IF v_tip NOT IN ('IS_EMRI', 'HAKEDIS', 'ODEME', 'BUTCE', 'SISTEM') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecersiz bildirim tipi.';
    END IF;

    INSERT INTO bildirim (
        firma_id, kullanici_id, baslik, mesaj, tip, referans_tablo, referans_id
    ) VALUES (
        p_firma_id, p_kullanici_id, p_baslik, p_mesaj, v_tip, p_referans_tablo, p_referans_id
    );

    SET v_id = LAST_INSERT_ID();
    SELECT v_id AS bildirim_id;
END //

CREATE PROCEDURE sp_bildirim_getir(
    IN p_bildirim_id INT,
    IN p_firma_id    INT,
    IN p_kullanici_id INT
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    SELECT rol INTO v_rol FROM kullanici WHERE kullanici_id = p_kullanici_id AND aktif = 1;

    SELECT
        bildirim_id, firma_id, kullanici_id, baslik, mesaj, tip,
        referans_tablo, referans_id, okundu, okundu_tarihi, created_at
    FROM bildirim
    WHERE bildirim_id = p_bildirim_id
      AND firma_id = p_firma_id
      AND (v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN') OR kullanici_id = p_kullanici_id);
END //

CREATE PROCEDURE sp_bildirim_okundu_isaretle(
    IN p_bildirim_id INT,
    IN p_firma_id    INT,
    IN p_kullanici_id INT
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    SELECT rol INTO v_rol FROM kullanici WHERE kullanici_id = p_kullanici_id AND aktif = 1;

    IF EXISTS (
        SELECT 1 FROM bildirim
        WHERE bildirim_id = p_bildirim_id
          AND firma_id = p_firma_id
          AND (v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN') OR kullanici_id = p_kullanici_id)
    ) THEN
        UPDATE bildirim
        SET okundu = 1,
            okundu_tarihi = COALESCE(okundu_tarihi, NOW())
        WHERE bildirim_id = p_bildirim_id
          AND firma_id = p_firma_id
          AND (v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN') OR kullanici_id = p_kullanici_id);

        SELECT 1 AS etkilenen_satir;
    ELSE
        SELECT 0 AS etkilenen_satir;
    END IF;
END //

CREATE PROCEDURE sp_bildirim_tumunu_okundu_isaretle(
    IN p_firma_id     INT,
    IN p_kullanici_id INT
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    SELECT rol INTO v_rol FROM kullanici WHERE kullanici_id = p_kullanici_id AND aktif = 1;

    UPDATE bildirim
    SET okundu = 1,
        okundu_tarihi = COALESCE(okundu_tarihi, NOW())
    WHERE firma_id = p_firma_id
      AND (v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN') OR kullanici_id = p_kullanici_id)
      AND okundu = 0;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_bildirim_sil(
    IN p_bildirim_id INT,
    IN p_firma_id    INT,
    IN p_kullanici_id INT
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    SELECT rol INTO v_rol FROM kullanici WHERE kullanici_id = p_kullanici_id AND aktif = 1;

    DELETE FROM bildirim
    WHERE bildirim_id = p_bildirim_id
      AND firma_id = p_firma_id
      AND (v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN') OR kullanici_id = p_kullanici_id);

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_bildirim_listele(
    IN p_firma_id           INT,
    IN p_kullanici_id       INT,
    IN p_sadece_okunmamis   TINYINT(1),
    IN p_limit              INT,
    IN p_offset             INT,
    OUT p_toplam            INT
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    SELECT rol INTO v_rol FROM kullanici WHERE kullanici_id = p_kullanici_id AND aktif = 1;

    SET p_limit = COALESCE(NULLIF(p_limit, 0), 20);
    SET p_offset = COALESCE(p_offset, 0);

    IF p_limit > 100 THEN
        SET p_limit = 100;
    END IF;

    IF p_offset < 0 THEN
        SET p_offset = 0;
    END IF;

    SELECT COUNT(*) INTO p_toplam
    FROM bildirim
    WHERE firma_id = p_firma_id
      AND (v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN') OR kullanici_id = p_kullanici_id)
      AND (COALESCE(p_sadece_okunmamis, 0) = 0 OR okundu = 0);

    SELECT
        bildirim_id, firma_id, kullanici_id, baslik, mesaj, tip,
        referans_tablo, referans_id, okundu, okundu_tarihi, created_at
    FROM bildirim
    WHERE firma_id = p_firma_id
      AND (v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN') OR kullanici_id = p_kullanici_id)
      AND (COALESCE(p_sadece_okunmamis, 0) = 0 OR okundu = 0)
    ORDER BY created_at DESC
    LIMIT p_limit OFFSET p_offset;
END //

DELIMITER ;