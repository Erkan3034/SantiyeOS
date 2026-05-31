USE santiyeos;

DROP PROCEDURE IF EXISTS sp_abonelik_limit_kontrol;

DELIMITER //

CREATE PROCEDURE sp_abonelik_limit_kontrol(
    IN p_firma_id INT,
    IN p_kaynak_tipi VARCHAR(30)
)
BEGIN
    DECLARE v_limit INT DEFAULT 0;
    DECLARE v_kullanim INT DEFAULT 0;
    DECLARE v_kaynak_tipi VARCHAR(30);
    DECLARE v_aktif_var TINYINT DEFAULT 0;

    SET v_kaynak_tipi = UPPER(TRIM(p_kaynak_tipi));
    SET v_aktif_var = fn_firma_aktif_abonelik_var_mi(p_firma_id);

    IF v_aktif_var = 1 THEN
        SET v_limit = fn_firma_plan_limit(p_firma_id, v_kaynak_tipi);
        SET v_kullanim = fn_firma_kullanim_sayisi(p_firma_id, v_kaynak_tipi);
    END IF;

    SELECT
        v_aktif_var AS aktif_abonelik_var_mi,
        v_limit AS plan_limit,
        v_kullanim AS kullanim_sayisi;
END //

DELIMITER ;
