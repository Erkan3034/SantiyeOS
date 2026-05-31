USE santiyeos;

DROP FUNCTION IF EXISTS fn_taseron_bakiye;
DROP FUNCTION IF EXISTS fn_proje_butce_kullanim;
DROP FUNCTION IF EXISTS fn_kullanici_yetki_kontrol;
DROP FUNCTION IF EXISTS fn_kullanici_yetki_kontrol_firma;

DELIMITER //

CREATE FUNCTION fn_taseron_bakiye(
    p_taseron_id INT,
    p_firma_id INT
)
RETURNS DECIMAL(15,2)
READS SQL DATA
NOT DETERMINISTIC
BEGIN
    DECLARE v_hakedis DECIMAL(15,2) DEFAULT 0;
    DECLARE v_odeme DECIMAL(15,2) DEFAULT 0;

    SELECT COALESCE(SUM(h.tutar), 0) INTO v_hakedis
    FROM hakedis h
    INNER JOIN is_emri ie ON ie.is_emri_id = h.is_emri_id
    WHERE ie.taseron_id = p_taseron_id
      AND h.firma_id = p_firma_id
      AND h.onay_durumu = 'ONAYLANDI';

    SELECT COALESCE(SUM(o.tutar), 0) INTO v_odeme
    FROM odeme o
    INNER JOIN hakedis h ON h.hakedis_id = o.hakedis_id
    INNER JOIN is_emri ie ON ie.is_emri_id = h.is_emri_id
    WHERE ie.taseron_id = p_taseron_id
      AND o.firma_id = p_firma_id;

    RETURN v_hakedis - v_odeme;
END //



CREATE FUNCTION fn_proje_butce_kullanim(
    p_proje_id INT,
    p_firma_id INT
)
RETURNS DECIMAL(15,2)
READS SQL DATA
NOT DETERMINISTIC
BEGIN
    DECLARE v_butce DECIMAL(15,2) DEFAULT 0;
    DECLARE v_harcanan DECIMAL(15,2) DEFAULT 0;

    SELECT COALESCE(butce, 0) INTO v_butce
    FROM proje
    WHERE proje_id = p_proje_id
      AND firma_id = p_firma_id;

    SELECT COALESCE(SUM(o.tutar), 0) INTO v_harcanan
    FROM odeme o
    INNER JOIN hakedis h ON h.hakedis_id = o.hakedis_id
    INNER JOIN is_emri ie ON ie.is_emri_id = h.is_emri_id
    WHERE ie.proje_id = p_proje_id
      AND o.firma_id = p_firma_id;

    IF v_butce = 0 THEN
        RETURN 0;
    END IF;

    RETURN ROUND((v_harcanan / v_butce) * 100, 2);
END //




CREATE FUNCTION fn_kullanici_yetki_kontrol(
    p_kullanici_id INT,
    p_izinli_roller VARCHAR(255)
)
RETURNS TINYINT(1)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_rol VARCHAR(50);

    SELECT rol INTO v_rol
    FROM kullanici
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1;

    RETURN IFNULL(FIND_IN_SET(v_rol, p_izinli_roller), 0) > 0;
END //





CREATE FUNCTION fn_kullanici_yetki_kontrol_firma(
    p_kullanici_id INT,
    p_firma_id INT,
    p_izinli_roller VARCHAR(255)
)
RETURNS TINYINT(1)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_rol VARCHAR(50);
    DECLARE v_firma_id INT;

    SELECT COUNT(*) INTO v_count
    FROM kullanici
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1;

    IF v_count = 0 THEN
        RETURN 0;
    END IF;

    SELECT rol, firma_id INTO v_rol, v_firma_id
    FROM kullanici
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1
    LIMIT 1;

    IF IFNULL(FIND_IN_SET(v_rol, p_izinli_roller), 0) = 0 THEN
        RETURN 0;
    END IF;

    IF v_rol = 'SUPER_ADMIN' THEN
        RETURN 1;
    END IF;

    RETURN IFNULL(v_firma_id = p_firma_id, 0);
END //

DELIMITER ;
