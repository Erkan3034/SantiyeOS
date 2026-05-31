USE santiyeos;

DELIMITER //

CREATE PROCEDURE sp_refresh_token_ekle(
    IN p_kullanici_id       INT,
    IN p_token_hash         VARCHAR(255),
    IN p_son_kullanim       DATETIME,
    IN p_ip_adresi          VARCHAR(45),
    IN p_user_agent         VARCHAR(500)
)
BEGIN
    DECLARE v_id INT;
    INSERT INTO refresh_token (kullanici_id, token_hash, son_kullanim_tarihi, ip_adresi, user_agent)
    VALUES (p_kullanici_id, p_token_hash, p_son_kullanim, p_ip_adresi, p_user_agent);
    SET v_id = LAST_INSERT_ID();
    SELECT v_id AS token_id;
END //

CREATE PROCEDURE sp_refresh_token_iptal(IN p_token_hash VARCHAR(255))
BEGIN
    UPDATE refresh_token SET iptal_edildi = 1 WHERE token_hash = p_token_hash;
END //

CREATE PROCEDURE sp_refresh_token_temizle(IN p_kullanici_id INT)
BEGIN
    DELETE FROM refresh_token
    WHERE kullanici_id = p_kullanici_id
      AND (son_kullanim_tarihi < NOW() OR iptal_edildi = 1);
END //

DELIMITER ;
