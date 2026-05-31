USE santiyeos;

DROP FUNCTION IF EXISTS fn_kullanici_proje_yetkili_mi;

DELIMITER //

CREATE FUNCTION fn_kullanici_proje_yetkili_mi(
    p_kullanici_id INT,
    p_proje_id INT,
    p_firma_id INT
)
RETURNS TINYINT
READS SQL DATA
BEGIN
    DECLARE v_yetkili TINYINT DEFAULT 0;

    SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END
    INTO v_yetkili
    FROM proje_kullanici pk
    INNER JOIN proje p ON p.proje_id = pk.proje_id
    WHERE pk.kullanici_id = p_kullanici_id
      AND pk.proje_id = p_proje_id
      AND pk.firma_id = p_firma_id
      AND p.firma_id = p_firma_id
      AND p.durum <> 'IPTAL';

    RETURN v_yetkili;
END //

DELIMITER ;
