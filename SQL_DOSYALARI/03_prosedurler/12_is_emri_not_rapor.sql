USE santiyeos;

DROP PROCEDURE IF EXISTS sp_is_emri_not_ekle;
DROP PROCEDURE IF EXISTS sp_is_emri_not_guncelle;
DROP PROCEDURE IF EXISTS sp_is_emri_not_sil;
DROP PROCEDURE IF EXISTS sp_is_emri_not_listele;
DROP PROCEDURE IF EXISTS sp_is_emri_not_getir;

DROP PROCEDURE IF EXISTS sp_is_emri_rapor_ekle;
DROP PROCEDURE IF EXISTS sp_is_emri_rapor_guncelle;
DROP PROCEDURE IF EXISTS sp_is_emri_rapor_sil;
DROP PROCEDURE IF EXISTS sp_is_emri_rapor_listele;
DROP PROCEDURE IF EXISTS sp_is_emri_rapor_getir;

DELIMITER //

CREATE PROCEDURE sp_is_emri_not_ekle(
    IN p_firma_id       INT,
    IN p_is_emri_id     INT,
    IN p_kullanici_id   INT,
    IN p_icerik         TEXT
)
BEGIN
    DECLARE v_id INT;
    DECLARE v_proje_id INT;
    DECLARE v_taseron_id INT;
    DECLARE v_rol VARCHAR(50);

    SELECT rol INTO v_rol
    FROM kullanici
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1
    LIMIT 1;

    IF v_rol IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Kullanici bulunamadi veya pasif.';
    END IF;

    IF p_icerik IS NULL OR TRIM(p_icerik) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Not icerigi zorunludur.';
    END IF;

    SELECT proje_id, taseron_id INTO v_proje_id, v_taseron_id
    FROM is_emri
    WHERE is_emri_id = p_is_emri_id
      AND firma_id = p_firma_id
      AND durum <> 'IPTAL'
    LIMIT 1;

    IF v_proje_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Is emri bulunamadi.';
    END IF;

    IF v_rol <> 'SUPER_ADMIN'
       AND NOT fn_kullanici_yetki_kontrol_firma(
           p_kullanici_id,
           p_firma_id,
           'FIRMA_ADMIN,PROJE_YONETICISI,SAHA_PERSONELI,TASERON_TEMSILCI'
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF v_rol IN ('PROJE_YONETICISI', 'SAHA_PERSONELI')
       AND NOT EXISTS (
           SELECT 1
           FROM proje_kullanici
           WHERE proje_id = v_proje_id
             AND kullanici_id = p_kullanici_id
             AND firma_id = p_firma_id
       )
       AND NOT EXISTS (
           SELECT 1
           FROM is_emri
           WHERE is_emri_id = p_is_emri_id
             AND firma_id = p_firma_id
             AND atanan_kullanici_id = p_kullanici_id
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu is emrine erisim yetkiniz yok.';
    END IF;

    IF v_rol = 'TASERON_TEMSILCI'
       AND NOT EXISTS (
           SELECT 1
           FROM kullanici
           WHERE kullanici_id = p_kullanici_id
             AND taseron_id = v_taseron_id
             AND firma_id = p_firma_id
             AND aktif = 1
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu is emri bu taseron kullanicisina ait degil.';
    END IF;

    INSERT INTO is_emri_not (firma_id, is_emri_id, kullanici_id, icerik)
    VALUES (p_firma_id, p_is_emri_id, p_kullanici_id, TRIM(p_icerik));

    SET v_id = LAST_INSERT_ID();

    SELECT v_id AS not_id;
END //

CREATE PROCEDURE sp_is_emri_not_guncelle(
    IN p_not_id         INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_icerik         TEXT
)
BEGIN
    DECLARE v_yazan_id INT;
    DECLARE v_rol VARCHAR(50);

    SELECT rol INTO v_rol
    FROM kullanici
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1
    LIMIT 1;

    SELECT kullanici_id INTO v_yazan_id
    FROM is_emri_not
    WHERE not_id = p_not_id
      AND firma_id = p_firma_id
    LIMIT 1;

    IF v_yazan_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Not bulunamadi.';
    END IF;

    IF p_icerik IS NULL OR TRIM(p_icerik) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Not icerigi zorunludur.';
    END IF;

    IF v_yazan_id <> p_kullanici_id
       AND IFNULL(FIND_IN_SET(v_rol, 'SUPER_ADMIN,FIRMA_ADMIN'), 0) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Sadece notu yazan kisi veya admin guncelleyebilir.';
    END IF;

    UPDATE is_emri_not
    SET icerik = TRIM(p_icerik)
    WHERE not_id = p_not_id
      AND firma_id = p_firma_id;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_is_emri_not_sil(
    IN p_not_id         INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT
)
BEGIN
    DECLARE v_yazan_id INT;
    DECLARE v_rol VARCHAR(50);

    SELECT rol INTO v_rol
    FROM kullanici
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1
    LIMIT 1;

    SELECT kullanici_id INTO v_yazan_id
    FROM is_emri_not
    WHERE not_id = p_not_id
      AND firma_id = p_firma_id
    LIMIT 1;

    IF v_yazan_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Not bulunamadi.';
    END IF;

    IF v_yazan_id <> p_kullanici_id
       AND IFNULL(FIND_IN_SET(v_rol, 'SUPER_ADMIN,FIRMA_ADMIN'), 0) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Sadece notu yazan kisi veya admin silebilir.';
    END IF;

    DELETE FROM is_emri_not
    WHERE not_id = p_not_id
      AND firma_id = p_firma_id;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_is_emri_rapor_ekle(
    IN p_firma_id       INT,
    IN p_is_emri_id     INT,
    IN p_kullanici_id   INT,
    IN p_baslik         VARCHAR(200),
    IN p_icerik         TEXT
)
BEGIN
    DECLARE v_id INT;
    DECLARE v_proje_id INT;
    DECLARE v_taseron_id INT;
    DECLARE v_rol VARCHAR(50);

    SELECT rol INTO v_rol
    FROM kullanici
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1
    LIMIT 1;

    IF v_rol IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Kullanici bulunamadi veya pasif.';
    END IF;

    IF p_baslik IS NULL OR TRIM(p_baslik) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Rapor basligi zorunludur.';
    END IF;

    IF p_icerik IS NULL OR TRIM(p_icerik) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Rapor icerigi zorunludur.';
    END IF;

    SELECT proje_id, taseron_id INTO v_proje_id, v_taseron_id
    FROM is_emri
    WHERE is_emri_id = p_is_emri_id
      AND firma_id = p_firma_id
      AND durum <> 'IPTAL'
    LIMIT 1;

    IF v_proje_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Is emri bulunamadi.';
    END IF;

    IF v_rol <> 'SUPER_ADMIN'
       AND NOT fn_kullanici_yetki_kontrol_firma(
           p_kullanici_id,
           p_firma_id,
           'FIRMA_ADMIN,PROJE_YONETICISI,SAHA_PERSONELI,TASERON_TEMSILCI'
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF v_rol IN ('PROJE_YONETICISI', 'SAHA_PERSONELI')
       AND NOT EXISTS (
           SELECT 1
           FROM proje_kullanici
           WHERE proje_id = v_proje_id
             AND kullanici_id = p_kullanici_id
             AND firma_id = p_firma_id
       )
       AND NOT EXISTS (
           SELECT 1
           FROM is_emri
           WHERE is_emri_id = p_is_emri_id
             AND firma_id = p_firma_id
             AND atanan_kullanici_id = p_kullanici_id
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu is emrine erisim yetkiniz yok.';
    END IF;

    IF v_rol = 'TASERON_TEMSILCI'
       AND NOT EXISTS (
           SELECT 1
           FROM kullanici
           WHERE kullanici_id = p_kullanici_id
             AND taseron_id = v_taseron_id
             AND firma_id = p_firma_id
             AND aktif = 1
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu is emri bu taseron kullanicisina ait degil.';
    END IF;

    INSERT INTO is_emri_rapor (firma_id, is_emri_id, kullanici_id, baslik, icerik)
    VALUES (p_firma_id, p_is_emri_id, p_kullanici_id, TRIM(p_baslik), TRIM(p_icerik));

    SET v_id = LAST_INSERT_ID();

    SELECT v_id AS rapor_id;
END //

CREATE PROCEDURE sp_is_emri_rapor_guncelle(
    IN p_rapor_id       INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_baslik         VARCHAR(200),
    IN p_icerik         TEXT
)
BEGIN
    DECLARE v_yazan_id INT;
    DECLARE v_rol VARCHAR(50);

    SELECT rol INTO v_rol
    FROM kullanici
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1
    LIMIT 1;

    SELECT kullanici_id INTO v_yazan_id
    FROM is_emri_rapor
    WHERE rapor_id = p_rapor_id
      AND firma_id = p_firma_id
    LIMIT 1;

    IF v_yazan_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Rapor bulunamadi.';
    END IF;

    IF p_baslik IS NULL OR TRIM(p_baslik) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Rapor basligi zorunludur.';
    END IF;

    IF p_icerik IS NULL OR TRIM(p_icerik) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Rapor icerigi zorunludur.';
    END IF;

    IF v_yazan_id <> p_kullanici_id
       AND IFNULL(FIND_IN_SET(v_rol, 'SUPER_ADMIN,FIRMA_ADMIN'), 0) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Sadece raporu yazan kisi veya admin guncelleyebilir.';
    END IF;

    UPDATE is_emri_rapor
    SET baslik = TRIM(p_baslik),
        icerik = TRIM(p_icerik)
    WHERE rapor_id = p_rapor_id
      AND firma_id = p_firma_id;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_is_emri_rapor_sil(
    IN p_rapor_id       INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT
)
BEGIN
    DECLARE v_yazan_id INT;
    DECLARE v_rol VARCHAR(50);

    SELECT rol INTO v_rol
    FROM kullanici
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1
    LIMIT 1;

    SELECT kullanici_id INTO v_yazan_id
    FROM is_emri_rapor
    WHERE rapor_id = p_rapor_id
      AND firma_id = p_firma_id
    LIMIT 1;

    IF v_yazan_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Rapor bulunamadi.';
    END IF;

    IF v_yazan_id <> p_kullanici_id
       AND IFNULL(FIND_IN_SET(v_rol, 'SUPER_ADMIN,FIRMA_ADMIN'), 0) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Sadece raporu yazan kisi veya admin silebilir.';
    END IF;

    DELETE FROM is_emri_rapor
    WHERE rapor_id = p_rapor_id
      AND firma_id = p_firma_id;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

DELIMITER ;

-- ================================================================================================

DROP PROCEDURE IF EXISTS sp_is_emri_not_listele;
DROP PROCEDURE IF EXISTS sp_is_emri_not_getir;
DROP PROCEDURE IF EXISTS sp_is_emri_rapor_listele;
DROP PROCEDURE IF EXISTS sp_is_emri_rapor_getir;

DELIMITER //

CREATE PROCEDURE sp_is_emri_not_listele(
    IN p_is_emri_id     INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_limit          INT,
    IN p_offset         INT,
    OUT p_toplam        INT
)
BEGIN
    DECLARE v_proje_id INT;
    DECLARE v_taseron_id INT;
    DECLARE v_rol VARCHAR(50);

    SET p_limit = COALESCE(NULLIF(p_limit, 0), 20);
    SET p_offset = COALESCE(p_offset, 0);

    IF p_limit > 100 THEN
        SET p_limit = 100;
    END IF;

    IF p_offset < 0 THEN
        SET p_offset = 0;
    END IF;

    SELECT rol INTO v_rol
    FROM kullanici
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1
    LIMIT 1;

    IF v_rol IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Kullanici bulunamadi veya pasif.';
    END IF;

    SELECT proje_id, taseron_id INTO v_proje_id, v_taseron_id
    FROM is_emri
    WHERE is_emri_id = p_is_emri_id
      AND firma_id = p_firma_id
    LIMIT 1;

    IF v_proje_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Is emri bulunamadi.';
    END IF;

    IF v_rol <> 'SUPER_ADMIN'
       AND NOT fn_kullanici_yetki_kontrol_firma(
           p_kullanici_id,
           p_firma_id,
           'FIRMA_ADMIN,PROJE_YONETICISI,SAHA_PERSONELI,TASERON_TEMSILCI'
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF v_rol IN ('PROJE_YONETICISI', 'SAHA_PERSONELI')
       AND NOT EXISTS (
           SELECT 1 FROM proje_kullanici
           WHERE proje_id = v_proje_id
             AND kullanici_id = p_kullanici_id
             AND firma_id = p_firma_id
       )
       AND NOT EXISTS (
           SELECT 1 FROM is_emri
           WHERE is_emri_id = p_is_emri_id
             AND firma_id = p_firma_id
             AND atanan_kullanici_id = p_kullanici_id
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu is emrine erisim yetkiniz yok.';
    END IF;

    IF v_rol = 'TASERON_TEMSILCI'
       AND NOT EXISTS (
           SELECT 1 FROM kullanici
           WHERE kullanici_id = p_kullanici_id
             AND taseron_id = v_taseron_id
             AND firma_id = p_firma_id
             AND aktif = 1
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu is emri bu taseron kullanicisina ait degil.';
    END IF;

    SELECT COUNT(*) INTO p_toplam
    FROM is_emri_not
    WHERE is_emri_id = p_is_emri_id
      AND firma_id = p_firma_id;

    SELECT
        n.not_id,
        n.firma_id,
        n.is_emri_id,
        n.kullanici_id,
        n.icerik,
        n.created_at,
        CONCAT(k.ad, ' ', k.soyad) AS yazan,
        k.rol
    FROM is_emri_not n
    INNER JOIN kullanici k ON k.kullanici_id = n.kullanici_id
    WHERE n.is_emri_id = p_is_emri_id
      AND n.firma_id = p_firma_id
    ORDER BY n.created_at ASC
    LIMIT p_limit OFFSET p_offset;
END //

CREATE PROCEDURE sp_is_emri_not_getir(
    IN p_not_id         INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT
)
BEGIN
    DECLARE v_is_emri_id INT;

    SELECT is_emri_id INTO v_is_emri_id
    FROM is_emri_not
    WHERE not_id = p_not_id
      AND firma_id = p_firma_id
    LIMIT 1;

    IF v_is_emri_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Not bulunamadi.';
    END IF;

    CALL sp_is_emri_not_listele(v_is_emri_id, p_firma_id, p_kullanici_id, 100, 0, @not_toplam);

    SELECT
        n.not_id,
        n.firma_id,
        n.is_emri_id,
        n.kullanici_id,
        n.icerik,
        n.created_at,
        CONCAT(k.ad, ' ', k.soyad) AS yazan,
        k.rol
    FROM is_emri_not n
    INNER JOIN kullanici k ON k.kullanici_id = n.kullanici_id
    WHERE n.not_id = p_not_id
      AND n.firma_id = p_firma_id;
END //

CREATE PROCEDURE sp_is_emri_rapor_listele(
    IN p_is_emri_id     INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_limit          INT,
    IN p_offset         INT,
    OUT p_toplam        INT
)
BEGIN
    DECLARE v_proje_id INT;
    DECLARE v_taseron_id INT;
    DECLARE v_rol VARCHAR(50);

    SET p_limit = COALESCE(NULLIF(p_limit, 0), 20);
    SET p_offset = COALESCE(p_offset, 0);

    IF p_limit > 100 THEN
        SET p_limit = 100;
    END IF;

    IF p_offset < 0 THEN
        SET p_offset = 0;
    END IF;

    SELECT rol INTO v_rol
    FROM kullanici
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1
    LIMIT 1;

    IF v_rol IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Kullanici bulunamadi veya pasif.';
    END IF;

    SELECT proje_id, taseron_id INTO v_proje_id, v_taseron_id
    FROM is_emri
    WHERE is_emri_id = p_is_emri_id
      AND firma_id = p_firma_id
    LIMIT 1;

    IF v_proje_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Is emri bulunamadi.';
    END IF;

    IF v_rol <> 'SUPER_ADMIN'
       AND NOT fn_kullanici_yetki_kontrol_firma(
           p_kullanici_id,
           p_firma_id,
           'FIRMA_ADMIN,PROJE_YONETICISI,SAHA_PERSONELI,TASERON_TEMSILCI'
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF v_rol IN ('PROJE_YONETICISI', 'SAHA_PERSONELI')
       AND NOT EXISTS (
           SELECT 1 FROM proje_kullanici
           WHERE proje_id = v_proje_id
             AND kullanici_id = p_kullanici_id
             AND firma_id = p_firma_id
       )
       AND NOT EXISTS (
           SELECT 1 FROM is_emri
           WHERE is_emri_id = p_is_emri_id
             AND firma_id = p_firma_id
             AND atanan_kullanici_id = p_kullanici_id
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu is emrine erisim yetkiniz yok.';
    END IF;

    IF v_rol = 'TASERON_TEMSILCI'
       AND NOT EXISTS (
           SELECT 1 FROM kullanici
           WHERE kullanici_id = p_kullanici_id
             AND taseron_id = v_taseron_id
             AND firma_id = p_firma_id
             AND aktif = 1
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu is emri bu taseron kullanicisina ait degil.';
    END IF;

    SELECT COUNT(*) INTO p_toplam
    FROM is_emri_rapor
    WHERE is_emri_id = p_is_emri_id
      AND firma_id = p_firma_id;

    SELECT
        r.rapor_id,
        r.firma_id,
        r.is_emri_id,
        r.kullanici_id,
        r.baslik,
        r.icerik,
        r.created_at,
        CONCAT(k.ad, ' ', k.soyad) AS yazan,
        k.rol
    FROM is_emri_rapor r
    INNER JOIN kullanici k ON k.kullanici_id = r.kullanici_id
    WHERE r.is_emri_id = p_is_emri_id
      AND r.firma_id = p_firma_id
    ORDER BY r.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END //

CREATE PROCEDURE sp_is_emri_rapor_getir(
    IN p_rapor_id       INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT
)
BEGIN
    DECLARE v_is_emri_id INT;

    SELECT is_emri_id INTO v_is_emri_id
    FROM is_emri_rapor
    WHERE rapor_id = p_rapor_id
      AND firma_id = p_firma_id
    LIMIT 1;

    IF v_is_emri_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Rapor bulunamadi.';
    END IF;

    CALL sp_is_emri_rapor_listele(v_is_emri_id, p_firma_id, p_kullanici_id, 100, 0, @rapor_toplam);

    SELECT
        r.rapor_id,
        r.firma_id,
        r.is_emri_id,
        r.kullanici_id,
        r.baslik,
        r.icerik,
        r.created_at,
        CONCAT(k.ad, ' ', k.soyad) AS yazan,
        k.rol
    FROM is_emri_rapor r
    INNER JOIN kullanici k ON k.kullanici_id = r.kullanici_id
    WHERE r.rapor_id = p_rapor_id
      AND r.firma_id = p_firma_id;
END //

DELIMITER ;
