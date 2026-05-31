USE santiyeos;

DROP PROCEDURE IF EXISTS sp_lookup_projeler;
DROP PROCEDURE IF EXISTS sp_lookup_taseronlar;
DROP PROCEDURE IF EXISTS sp_lookup_kullanicilar;
DROP PROCEDURE IF EXISTS sp_lookup_malzemeler;
DROP PROCEDURE IF EXISTS sp_lookup_abonelik_planlari;

DELIMITER //

CREATE PROCEDURE sp_lookup_projeler(
    IN p_firma_id INT,
    IN p_kullanici_id INT,
    IN p_rol VARCHAR(50)
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    SET v_rol = UPPER(TRIM(p_rol));

    SELECT
        p.proje_id,
        p.ad,
        p.durum
    FROM proje p
    WHERE p.firma_id = p_firma_id
      AND p.durum IN ('PLANLANDI', 'DEVAM_EDIYOR')
      AND (
          v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
          OR (
              v_rol = 'PROJE_YONETICISI'
              AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, p.proje_id, p.firma_id) = 1
          )
      )
    ORDER BY p.ad ASC, p.proje_id ASC;
END //

CREATE PROCEDURE sp_lookup_taseronlar(
    IN p_firma_id INT
)
BEGIN
    SELECT
        taseron_id,
        ad,
        uzmanlik
    FROM taseron
    WHERE firma_id = p_firma_id
      AND aktif = 1
    ORDER BY ad ASC, taseron_id ASC;
END //

CREATE PROCEDURE sp_lookup_kullanicilar(
    IN p_firma_id INT,
    IN p_rol VARCHAR(50)
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    SET v_rol = UPPER(NULLIF(TRIM(p_rol), ''));

    SELECT
        k.kullanici_id,
        k.taseron_id,
        k.ad,
        k.soyad,
        k.email,
        k.rol,
        t.ad AS taseron_ad
    FROM kullanici k
    LEFT JOIN taseron t ON t.taseron_id = k.taseron_id AND t.firma_id = k.firma_id
    WHERE k.firma_id = p_firma_id
      AND k.aktif = 1
      AND k.rol <> 'SUPER_ADMIN'
      AND (v_rol IS NULL OR k.rol = v_rol)
    ORDER BY k.ad ASC, k.soyad ASC, k.kullanici_id ASC;
END //

CREATE PROCEDURE sp_lookup_malzemeler(
    IN p_firma_id INT
)
BEGIN
    SELECT
        m.malzeme_id,
        m.kategori_id,
        m.ad,
        m.birim,
        mk.ad AS kategori_ad,
        m.stok_miktari
    FROM malzeme m
    LEFT JOIN malzeme_kategori mk ON mk.kategori_id = m.kategori_id AND mk.firma_id = m.firma_id
    WHERE m.firma_id = p_firma_id
      AND m.aktif = 1
    ORDER BY m.ad ASC, m.malzeme_id ASC;
END //

CREATE PROCEDURE sp_lookup_abonelik_planlari()
BEGIN
    SELECT
        plan_id,
        ad,
        max_proje,
        max_kullanici,
        max_taseron,
        aylik_ucret
    FROM abonelik_plan
    WHERE aktif = 1
    ORDER BY aylik_ucret ASC, plan_id ASC;
END //

DELIMITER ;
