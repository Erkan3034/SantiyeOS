USE santiyeos;

DROP FUNCTION IF EXISTS fn_firma_aktif_abonelik_var_mi;
DROP FUNCTION IF EXISTS fn_firma_plan_limit;
DROP FUNCTION IF EXISTS fn_firma_kullanim_sayisi;

DELIMITER //

CREATE FUNCTION fn_firma_aktif_abonelik_var_mi(p_firma_id INT)
RETURNS TINYINT
READS SQL DATA
BEGIN
    DECLARE v_var_mi TINYINT DEFAULT 0;

    SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END
    INTO v_var_mi
    FROM firma f
    INNER JOIN abonelik a ON a.firma_id = f.firma_id
    WHERE f.firma_id = p_firma_id
      AND f.aktif = 1
      AND a.durum = 'AKTIF'
      AND CURDATE() BETWEEN a.baslangic_tarihi AND a.bitis_tarihi;

    RETURN v_var_mi;
END //

CREATE FUNCTION fn_firma_plan_limit(
    p_firma_id INT,
    p_limit_tipi VARCHAR(30)
)
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_limit INT DEFAULT 0;
    DECLARE v_limit_tipi VARCHAR(30);

    SET v_limit_tipi = UPPER(TRIM(p_limit_tipi));

    SELECT CASE v_limit_tipi
               WHEN 'PROJE' THEN ap.max_proje
               WHEN 'KULLANICI' THEN ap.max_kullanici
               WHEN 'TASERON' THEN ap.max_taseron
               ELSE 0
           END
    INTO v_limit
    FROM abonelik a
    INNER JOIN abonelik_plan ap ON ap.plan_id = a.plan_id
    INNER JOIN firma f ON f.firma_id = a.firma_id
    WHERE a.firma_id = p_firma_id
      AND f.aktif = 1
      AND a.durum = 'AKTIF'
      AND CURDATE() BETWEEN a.baslangic_tarihi AND a.bitis_tarihi
    ORDER BY a.bitis_tarihi DESC
    LIMIT 1;

    RETURN COALESCE(v_limit, 0);
END //

CREATE FUNCTION fn_firma_kullanim_sayisi(
    p_firma_id INT,
    p_kaynak_tipi VARCHAR(30)
)
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_sayi INT DEFAULT 0;
    DECLARE v_kaynak_tipi VARCHAR(30);

    SET v_kaynak_tipi = UPPER(TRIM(p_kaynak_tipi));

    IF v_kaynak_tipi = 'PROJE' THEN
        SELECT COUNT(*) INTO v_sayi
        FROM proje
        WHERE firma_id = p_firma_id
          AND durum <> 'IPTAL';
    ELSEIF v_kaynak_tipi = 'KULLANICI' THEN
        SELECT COUNT(*) INTO v_sayi
        FROM kullanici
        WHERE firma_id = p_firma_id
          AND aktif = 1;
    ELSEIF v_kaynak_tipi = 'TASERON' THEN
        SELECT COUNT(*) INTO v_sayi
        FROM taseron
        WHERE firma_id = p_firma_id
          AND aktif = 1;
    ELSE
        SET v_sayi = 0;
    END IF;

    RETURN COALESCE(v_sayi, 0);
END //

DELIMITER ;
