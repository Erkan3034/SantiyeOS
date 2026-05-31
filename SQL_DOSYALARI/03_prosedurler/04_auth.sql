USE santiyeos;

-- SantiyeOS Auth Module Migration
-- Login icin kullanici okuma prosedurleri. Tablo verisini degistirmez.

DROP PROCEDURE IF EXISTS sp_auth_kullanici_email_getir;
DROP PROCEDURE IF EXISTS sp_auth_kullanici_id_getir;
DROP PROCEDURE IF EXISTS sp_kullanici_sifre_guncelle;

DELIMITER //

CREATE PROCEDURE sp_auth_kullanici_email_getir(
    IN p_email VARCHAR(150)
)
BEGIN
    SELECT
        k.kullanici_id,
        k.firma_id,
        k.taseron_id,
        k.ad,
        k.soyad,
        k.email,
        k.sifre_hash,
        k.rol,
        k.telefon,
        k.aktif,
        k.sifre_degistirmeli,
        k.son_giris,
        k.created_at,
        k.updated_at
    FROM kullanici k
    LEFT JOIN firma f ON f.firma_id = k.firma_id
    WHERE LOWER(k.email) = LOWER(TRIM(p_email))
      AND k.aktif = 1
      AND (k.firma_id IS NULL OR f.aktif = 1)
    LIMIT 1;
END //

CREATE PROCEDURE sp_auth_kullanici_id_getir(
    IN p_kullanici_id INT
)
BEGIN
    SELECT
        k.kullanici_id,
        k.firma_id,
        k.taseron_id,
        k.ad,
        k.soyad,
        k.email,
        k.sifre_hash,
        k.rol,
        k.telefon,
        k.aktif,
        k.sifre_degistirmeli,
        k.son_giris,
        k.created_at,
        k.updated_at
    FROM kullanici k
    LEFT JOIN firma f ON f.firma_id = k.firma_id
    WHERE k.kullanici_id = p_kullanici_id
      AND k.aktif = 1
      AND (k.firma_id IS NULL OR f.aktif = 1)
    LIMIT 1;
END //

CREATE PROCEDURE sp_kullanici_sifre_guncelle(
    IN p_kullanici_id INT,
    IN p_sifre_hash VARCHAR(255),
    IN p_sifre_degistirmeli TINYINT
)
BEGIN
    UPDATE kullanici
    SET sifre_hash = p_sifre_hash,
        sifre_degistirmeli = COALESCE(p_sifre_degistirmeli, 0)
    WHERE kullanici_id = p_kullanici_id
      AND aktif = 1;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

DELIMITER ;

