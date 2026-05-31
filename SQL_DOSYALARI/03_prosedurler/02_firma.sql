USE santiyeos;

DELIMITER //

DROP PROCEDURE IF EXISTS sp_firma_listele //
CREATE PROCEDURE sp_firma_listele(
    IN p_aktif TINYINT,
    IN p_limit INT,
    IN p_offset INT,
    OUT p_toplam INT
)
BEGIN
    SELECT COUNT(*) INTO p_toplam
    FROM firma
    WHERE p_aktif IS NULL OR aktif = p_aktif;

    SELECT
        firma_id,
        ad,
        vergi_no,
        telefon,
        email,
        adres,
        aktif,
        created_at,
        updated_at
    FROM firma
    WHERE p_aktif IS NULL OR aktif = p_aktif
    ORDER BY created_at DESC
    LIMIT p_limit OFFSET p_offset;
END //

DROP PROCEDURE IF EXISTS sp_firma_getir //
CREATE PROCEDURE sp_firma_getir(
    IN p_firma_id INT
)
BEGIN
    SELECT
        firma_id,
        ad,
        vergi_no,
        telefon,
        email,
        adres,
        aktif,
        created_at,
        updated_at
    FROM firma
    WHERE firma_id = p_firma_id;
END //

DROP PROCEDURE IF EXISTS sp_firma_ekle //
CREATE PROCEDURE sp_firma_ekle(
    IN p_ad VARCHAR(200),
    IN p_vergi_no VARCHAR(20),
    IN p_telefon VARCHAR(20),
    IN p_email VARCHAR(150),
    IN p_adres TEXT
)
BEGIN
    DECLARE v_firma_id INT;

    INSERT INTO firma (ad, vergi_no, telefon, email, adres)
    VALUES (p_ad, p_vergi_no, p_telefon, p_email, p_adres);

    SET v_firma_id = LAST_INSERT_ID();

    SELECT v_firma_id AS firma_id;
END //

DROP PROCEDURE IF EXISTS sp_firma_guncelle //
CREATE PROCEDURE sp_firma_guncelle(
    IN p_firma_id INT,
    IN p_ad VARCHAR(200),
    IN p_vergi_no VARCHAR(20),
    IN p_telefon VARCHAR(20),
    IN p_email VARCHAR(150),
    IN p_adres TEXT,
    IN p_aktif TINYINT
)
BEGIN
    UPDATE firma
    SET ad = p_ad,
        vergi_no = p_vergi_no,
        telefon = p_telefon,
        email = p_email,
        adres = p_adres,
        aktif = p_aktif,
        updated_at = NOW()
    WHERE firma_id = p_firma_id;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

DROP PROCEDURE IF EXISTS sp_firma_pasiflestir //
CREATE PROCEDURE sp_firma_pasiflestir(
    IN p_firma_id INT
)
BEGIN
    UPDATE firma
    SET aktif = 0,
        updated_at = NOW()
    WHERE firma_id = p_firma_id;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

DELIMITER ;
