USE santiyeos;

DELIMITER //

CREATE PROCEDURE sp_proje_kullanici_ata(
    IN p_firma_id       INT,
    IN p_proje_id       INT,
    IN p_kullanici_id   INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    IF NOT EXISTS (
        SELECT 1 FROM proje WHERE proje_id = p_proje_id AND firma_id = p_firma_id
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Proje bulunamadi.';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM kullanici WHERE kullanici_id = p_kullanici_id
          AND (firma_id = p_firma_id OR firma_id IS NULL)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Kullanici bulunamadi.';
    END IF;

    START TRANSACTION;
        INSERT INTO proje_kullanici (proje_id, kullanici_id, firma_id)
        VALUES (p_proje_id, p_kullanici_id, p_firma_id)
        ON DUPLICATE KEY UPDATE atanma_tarihi = NOW();
    COMMIT;
END //

CREATE PROCEDURE sp_proje_kullanici_kaldir(
    IN p_firma_id       INT,
    IN p_proje_id       INT,
    IN p_kullanici_id   INT
)
BEGIN
    DELETE FROM proje_kullanici
    WHERE proje_id = p_proje_id AND kullanici_id = p_kullanici_id AND firma_id = p_firma_id;
END //

CREATE PROCEDURE sp_proje_kullanici_listele(
    IN p_firma_id       INT,
    IN p_proje_id       INT,
    IN p_limit          INT,
    IN p_offset         INT,
    OUT p_toplam        INT
)
BEGIN
    SELECT COUNT(*) INTO p_toplam
    FROM proje_kullanici
    WHERE firma_id = p_firma_id AND proje_id = p_proje_id;

    SELECT pk.*, k.ad, k.soyad, k.rol, k.email
    FROM proje_kullanici pk
    INNER JOIN kullanici k ON k.kullanici_id = pk.kullanici_id
    WHERE pk.firma_id = p_firma_id AND pk.proje_id = p_proje_id
    LIMIT p_limit OFFSET p_offset;
END //

DELIMITER ;

