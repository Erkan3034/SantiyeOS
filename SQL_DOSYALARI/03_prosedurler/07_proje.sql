USE santiyeos;

DELIMITER //

DROP PROCEDURE IF EXISTS sp_proje_ekle //
CREATE PROCEDURE sp_proje_ekle(
    IN p_firma_id       INT,
    IN p_ad             VARCHAR(200),
    IN p_aciklama       TEXT,
    IN p_konum          VARCHAR(300),
    IN p_butce          DECIMAL(15,2),
    IN p_baslangic      DATE,
    IN p_bitis          DATE,
    IN p_uyari_yuzde    TINYINT
)
BEGIN
    DECLARE v_proje_id INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    START TRANSACTION;
        INSERT INTO proje (
            firma_id, ad, aciklama, konum, butce,
            baslangic_tarihi, bitis_tarihi, butce_uyari_yuzde
        ) VALUES (
            p_firma_id, p_ad, p_aciklama, p_konum, p_butce,
            p_baslangic, p_bitis, COALESCE(p_uyari_yuzde, 85)
        );

        SET v_proje_id = LAST_INSERT_ID();
    COMMIT;

    SELECT v_proje_id AS proje_id;
END //

DROP PROCEDURE IF EXISTS sp_proje_guncelle //
CREATE PROCEDURE sp_proje_guncelle(
    IN p_proje_id       INT,
    IN p_firma_id       INT,
    IN p_ad             VARCHAR(200),
    IN p_aciklama       TEXT,
    IN p_konum          VARCHAR(300),
    IN p_butce          DECIMAL(15,2),
    IN p_baslangic      DATE,
    IN p_bitis          DATE,
    IN p_durum          VARCHAR(50),
    IN p_uyari_yuzde    TINYINT
)
BEGIN
    UPDATE proje
    SET ad = p_ad,
        aciklama = p_aciklama,
        konum = p_konum,
        butce = p_butce,
        baslangic_tarihi = p_baslangic,
        bitis_tarihi = p_bitis,
        durum = p_durum,
        butce_uyari_yuzde = p_uyari_yuzde
    WHERE proje_id = p_proje_id
      AND firma_id = p_firma_id;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

DROP PROCEDURE IF EXISTS sp_proje_sil //
CREATE PROCEDURE sp_proje_sil(
    IN p_proje_id       INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT
)
BEGIN
    IF NOT fn_kullanici_yetki_kontrol(p_kullanici_id, 'SUPER_ADMIN,FIRMA_ADMIN') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Yetkisiz islem. Proje silmek icin yonetici yetkisi gerekir.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM is_emri
        WHERE proje_id = p_proje_id
          AND firma_id = p_firma_id
          AND durum NOT IN ('TAMAMLANDI', 'IPTAL')
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Aktif is emirleri olan proje silinemez.';
    END IF;

    UPDATE proje
    SET durum = 'IPTAL'
    WHERE proje_id = p_proje_id
      AND firma_id = p_firma_id;

    SELECT ROW_COUNT() AS etkilenen_satir;
END //

DROP PROCEDURE IF EXISTS sp_proje_listele //
CREATE PROCEDURE sp_proje_listele(
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50),
    IN p_durum          VARCHAR(50),
    IN p_limit          INT,
    IN p_offset         INT,
    OUT p_toplam        INT
)
BEGIN
    SELECT COUNT(*) INTO p_toplam
    FROM proje p
    WHERE (p_firma_id IS NULL OR p.firma_id = p_firma_id)
      AND (p_durum IS NULL OR p.durum = p_durum)
      AND (
            p_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
            OR (
                p_rol = 'PROJE_YONETICISI'
                AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, p.proje_id, p.firma_id) = 1
            )
      );

    SELECT
        p.proje_id,
        p.firma_id,
        p.ad,
        p.aciklama,
        p.konum,
        p.butce,
        p.durum,
        p.baslangic_tarihi,
        p.bitis_tarihi,
        p.butce_uyari_yuzde,
        p.created_at,
        p.updated_at,
        COUNT(DISTINCT ie.is_emri_id) AS toplam_is_emri,
        COUNT(DISTINCT CASE WHEN ie.durum = 'TAMAMLANDI' THEN ie.is_emri_id END) AS tamamlanan_is_emri,
        COUNT(DISTINCT ie.taseron_id) AS taseron_sayisi,
        COALESCE(SUM(o.tutar), 0) AS toplam_odeme,
        fn_proje_butce_kullanim(p.proje_id, p.firma_id) AS butce_kullanim_yuzdesi
    FROM proje p
    LEFT JOIN is_emri ie ON ie.proje_id = p.proje_id AND ie.firma_id = p.firma_id
    LEFT JOIN hakedis h  ON h.is_emri_id = ie.is_emri_id AND h.firma_id = p.firma_id
    LEFT JOIN odeme o    ON o.hakedis_id = h.hakedis_id AND o.firma_id = p.firma_id
    WHERE (p_firma_id IS NULL OR p.firma_id = p_firma_id)
      AND (p_durum IS NULL OR p.durum = p_durum)
      AND (
            p_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
            OR (
                p_rol = 'PROJE_YONETICISI'
                AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, p.proje_id, p.firma_id) = 1
            )
      )
    GROUP BY p.proje_id
    ORDER BY p.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END //

DROP PROCEDURE IF EXISTS sp_proje_getir //
CREATE PROCEDURE sp_proje_getir(
    IN p_proje_id       INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50)
)
BEGIN
    SELECT
        p.*,
        COALESCE(SUM(o.tutar), 0) AS toplam_odeme,
        COUNT(DISTINCT ie.is_emri_id) AS toplam_is_emri,
        COUNT(DISTINCT CASE WHEN ie.durum = 'TAMAMLANDI' THEN ie.is_emri_id END) AS tamamlanan_is_emri,
        COUNT(DISTINCT ie.taseron_id) AS taseron_sayisi,
        fn_proje_butce_kullanim(p.proje_id, p.firma_id) AS butce_kullanim_yuzdesi
    FROM proje p
    LEFT JOIN is_emri ie ON ie.proje_id = p.proje_id AND ie.firma_id = p.firma_id
    LEFT JOIN hakedis h  ON h.is_emri_id = ie.is_emri_id AND h.firma_id = p.firma_id
    LEFT JOIN odeme o    ON o.hakedis_id = h.hakedis_id AND o.firma_id = p.firma_id
    WHERE p.proje_id = p_proje_id
      AND (p_firma_id IS NULL OR p.firma_id = p_firma_id)
      AND (
            p_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
            OR (
                p_rol = 'PROJE_YONETICISI'
                AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, p.proje_id, p.firma_id) = 1
            )
      )
    GROUP BY p.proje_id;
END //

DELIMITER ;
