USE santiyeos;

DROP PROCEDURE IF EXISTS sp_is_emri_listele;
DROP PROCEDURE IF EXISTS sp_is_emri_getir;
DROP PROCEDURE IF EXISTS sp_is_emri_ekle;
DROP PROCEDURE IF EXISTS sp_is_emri_guncelle;
DROP PROCEDURE IF EXISTS sp_is_emri_durum_guncelle;
DROP PROCEDURE IF EXISTS sp_is_emri_sil;

DELIMITER //

CREATE PROCEDURE sp_is_emri_listele(
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50),
    IN p_proje_id       INT,
    IN p_taseron_id     INT,
    IN p_durum          VARCHAR(50),
    IN p_limit          INT,
    IN p_offset         INT,
    OUT p_toplam        INT
)
BEGIN
    DECLARE v_durum VARCHAR(50);
    DECLARE v_rol VARCHAR(50);

    SET v_durum = UPPER(NULLIF(TRIM(p_durum), ''));
    SET v_rol = UPPER(NULLIF(TRIM(p_rol), ''));
    SET p_limit = COALESCE(NULLIF(p_limit, 0), 20);
    SET p_offset = COALESCE(p_offset, 0);

    IF p_limit > 100 THEN
        SET p_limit = 100;
    END IF;

    IF p_offset < 0 THEN
        SET p_offset = 0;
    END IF;

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI', 'SAHA_PERSONELI', 'TASERON_TEMSILCI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF v_durum IS NOT NULL
       AND v_durum NOT IN ('BEKLIYOR', 'BASLADI', 'DEVAM_EDIYOR', 'TAMAMLANDI', 'IPTAL', 'HAKEDISTE') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecersiz is emri durumu.';
    END IF;

    SELECT COUNT(DISTINCT ie.is_emri_id) INTO p_toplam
    FROM is_emri ie
    INNER JOIN proje p ON p.proje_id = ie.proje_id AND p.firma_id = ie.firma_id
    INNER JOIN taseron t ON t.taseron_id = ie.taseron_id AND t.firma_id = ie.firma_id
    WHERE ie.firma_id = p_firma_id
      AND (p_proje_id IS NULL OR ie.proje_id = p_proje_id)
      AND (p_taseron_id IS NULL OR ie.taseron_id = p_taseron_id)
      AND (v_durum IS NULL OR ie.durum = v_durum)
      AND (
            v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
            OR (
                v_rol = 'PROJE_YONETICISI'
                AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, ie.proje_id, ie.firma_id) = 1
            )
            OR (
                v_rol = 'SAHA_PERSONELI'
                AND ie.atanan_kullanici_id = p_kullanici_id
            )
            OR (
                v_rol = 'TASERON_TEMSILCI'
                AND p_taseron_id IS NOT NULL
                AND ie.taseron_id = p_taseron_id
            )
      );

    SELECT
        ie.is_emri_id,
        ie.firma_id,
        ie.proje_id,
        ie.taseron_id,
        ie.atanan_kullanici_id,
        ie.olusturan_id,
        ie.baslik,
        ie.aciklama,
        ie.oncelik,
        ie.durum,
        ie.baslangic_tarihi,
        ie.bitis_tarihi,
        ie.tamamlanma_tarihi,
        ie.created_at,
        ie.updated_at,
        p.ad AS proje_ad,
        t.ad AS taseron_ad,
        t.uzmanlik AS taseron_uzmanlik,
        CONCAT(k.ad, ' ', k.soyad) AS atanan_kullanici,
        CONCAT(o.ad, ' ', o.soyad) AS olusturan,
        COUNT(DISTINCT n.not_id) AS not_sayisi,
        COUNT(DISTINCT r.rapor_id) AS rapor_sayisi,
        DATEDIFF(ie.bitis_tarihi, CURDATE()) AS kalan_gun
    FROM is_emri ie
    INNER JOIN proje p ON p.proje_id = ie.proje_id AND p.firma_id = ie.firma_id
    INNER JOIN taseron t ON t.taseron_id = ie.taseron_id AND t.firma_id = ie.firma_id
    LEFT JOIN kullanici k ON k.kullanici_id = ie.atanan_kullanici_id
    LEFT JOIN kullanici o ON o.kullanici_id = ie.olusturan_id
    LEFT JOIN is_emri_not n ON n.is_emri_id = ie.is_emri_id AND n.firma_id = ie.firma_id
    LEFT JOIN is_emri_rapor r ON r.is_emri_id = ie.is_emri_id AND r.firma_id = ie.firma_id
    WHERE ie.firma_id = p_firma_id
      AND (p_proje_id IS NULL OR ie.proje_id = p_proje_id)
      AND (p_taseron_id IS NULL OR ie.taseron_id = p_taseron_id)
      AND (v_durum IS NULL OR ie.durum = v_durum)
      AND (
            v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
            OR (
                v_rol = 'PROJE_YONETICISI'
                AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, ie.proje_id, ie.firma_id) = 1
            )
            OR (
                v_rol = 'SAHA_PERSONELI'
                AND ie.atanan_kullanici_id = p_kullanici_id
            )
            OR (
                v_rol = 'TASERON_TEMSILCI'
                AND p_taseron_id IS NOT NULL
                AND ie.taseron_id = p_taseron_id
            )
      )
    GROUP BY
        ie.is_emri_id, ie.firma_id, ie.proje_id, ie.taseron_id,
        ie.atanan_kullanici_id, ie.olusturan_id, ie.baslik, ie.aciklama,
        ie.oncelik, ie.durum, ie.baslangic_tarihi, ie.bitis_tarihi,
        ie.tamamlanma_tarihi, ie.created_at, ie.updated_at,
        p.ad, t.ad, t.uzmanlik, k.ad, k.soyad, o.ad, o.soyad
    ORDER BY
        FIELD(ie.oncelik, 'KRITIK', 'YUKSEK', 'NORMAL', 'DUSUK'),
        ie.bitis_tarihi ASC
    LIMIT p_limit OFFSET p_offset;
END //

CREATE PROCEDURE sp_is_emri_getir(
    IN p_is_emri_id     INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50),
    IN p_taseron_id     INT
)
BEGIN
    DECLARE v_rol VARCHAR(50);

    SET v_rol = UPPER(NULLIF(TRIM(p_rol), ''));

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI', 'SAHA_PERSONELI', 'TASERON_TEMSILCI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    SELECT
        ie.is_emri_id,
        ie.firma_id,
        ie.proje_id,
        ie.taseron_id,
        ie.atanan_kullanici_id,
        ie.olusturan_id,
        ie.baslik,
        ie.aciklama,
        ie.oncelik,
        ie.durum,
        ie.baslangic_tarihi,
        ie.bitis_tarihi,
        ie.tamamlanma_tarihi,
        ie.created_at,
        ie.updated_at,
        p.ad AS proje_ad,
        t.ad AS taseron_ad,
        t.uzmanlik AS taseron_uzmanlik,
        CONCAT(k.ad, ' ', k.soyad) AS atanan_kullanici,
        CONCAT(o.ad, ' ', o.soyad) AS olusturan,
        COUNT(DISTINCT n.not_id) AS not_sayisi,
        COUNT(DISTINCT r.rapor_id) AS rapor_sayisi,
        DATEDIFF(ie.bitis_tarihi, CURDATE()) AS kalan_gun
    FROM is_emri ie
    INNER JOIN proje p ON p.proje_id = ie.proje_id AND p.firma_id = ie.firma_id
    INNER JOIN taseron t ON t.taseron_id = ie.taseron_id AND t.firma_id = ie.firma_id
    LEFT JOIN kullanici k ON k.kullanici_id = ie.atanan_kullanici_id
    LEFT JOIN kullanici o ON o.kullanici_id = ie.olusturan_id
    LEFT JOIN is_emri_not n ON n.is_emri_id = ie.is_emri_id AND n.firma_id = ie.firma_id
    LEFT JOIN is_emri_rapor r ON r.is_emri_id = ie.is_emri_id AND r.firma_id = ie.firma_id
    WHERE ie.is_emri_id = p_is_emri_id
      AND ie.firma_id = p_firma_id
      AND (p_taseron_id IS NULL OR ie.taseron_id = p_taseron_id)
      AND (
            v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
            OR (
                v_rol = 'PROJE_YONETICISI'
                AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, ie.proje_id, ie.firma_id) = 1
            )
            OR (
                v_rol = 'SAHA_PERSONELI'
                AND ie.atanan_kullanici_id = p_kullanici_id
            )
            OR (
                v_rol = 'TASERON_TEMSILCI'
                AND p_taseron_id IS NOT NULL
                AND ie.taseron_id = p_taseron_id
            )
      )
    GROUP BY
        ie.is_emri_id, ie.firma_id, ie.proje_id, ie.taseron_id,
        ie.atanan_kullanici_id, ie.olusturan_id, ie.baslik, ie.aciklama,
        ie.oncelik, ie.durum, ie.baslangic_tarihi, ie.bitis_tarihi,
        ie.tamamlanma_tarihi, ie.created_at, ie.updated_at,
        p.ad, t.ad, t.uzmanlik, k.ad, k.soyad, o.ad, o.soyad;
END //

CREATE PROCEDURE sp_is_emri_ekle(
    IN p_firma_id       INT,
    IN p_proje_id       INT,
    IN p_taseron_id     INT,
    IN p_atanan_id      INT,
    IN p_olusturan_id   INT,
    IN p_rol            VARCHAR(50),
    IN p_baslik         VARCHAR(300),
    IN p_aciklama       TEXT,
    IN p_oncelik        VARCHAR(20),
    IN p_baslangic      DATE,
    IN p_bitis          DATE
)
BEGIN
    DECLARE v_id INT;
    DECLARE v_rol VARCHAR(50);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    SET v_rol = UPPER(NULLIF(TRIM(p_rol), ''));

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM proje
        WHERE proje_id = p_proje_id
          AND firma_id = p_firma_id
          AND durum <> 'IPTAL'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Proje bulunamadi.';
    END IF;

    IF v_rol = 'PROJE_YONETICISI'
       AND fn_kullanici_proje_yetkili_mi(p_olusturan_id, p_proje_id, p_firma_id) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu proje icin yetkiniz yok.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM taseron
        WHERE taseron_id = p_taseron_id
          AND firma_id = p_firma_id
          AND aktif = 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Taseron bulunamadi.';
    END IF;

    IF p_atanan_id IS NOT NULL
       AND NOT EXISTS (
            SELECT 1 FROM kullanici
            WHERE kullanici_id = p_atanan_id
              AND firma_id = p_firma_id
              AND aktif = 1
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Atanan kullanici bulunamadi.';
    END IF;

    START TRANSACTION;
        INSERT INTO is_emri (
            firma_id, proje_id, taseron_id, atanan_kullanici_id,
            olusturan_id, baslik, aciklama, oncelik,
            baslangic_tarihi, bitis_tarihi
        ) VALUES (
            p_firma_id, p_proje_id, p_taseron_id, p_atanan_id,
            p_olusturan_id, p_baslik, p_aciklama, COALESCE(p_oncelik, 'NORMAL'),
            p_baslangic, p_bitis
        );

        SET v_id = LAST_INSERT_ID();

        INSERT INTO is_emri_durum_log (
            firma_id, is_emri_id, yapan_id, eski_durum, yeni_durum, aciklama
        ) VALUES (
            p_firma_id, v_id, p_olusturan_id, NULL, 'BEKLIYOR', 'Is emri olusturuldu'
        );
    COMMIT;

    SELECT v_id AS is_emri_id;
END //

CREATE PROCEDURE sp_is_emri_guncelle(
    IN p_is_emri_id     INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50),
    IN p_baslik         VARCHAR(300),
    IN p_aciklama       TEXT,
    IN p_oncelik        VARCHAR(20),
    IN p_atanan_id      INT,
    IN p_baslangic      DATE,
    IN p_bitis          DATE
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    DECLARE v_proje_id INT;
    DECLARE v_etkilenen_satir INT DEFAULT 0;

    SET v_rol = UPPER(NULLIF(TRIM(p_rol), ''));

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    SELECT proje_id INTO v_proje_id
    FROM is_emri
    WHERE is_emri_id = p_is_emri_id
      AND firma_id = p_firma_id
    LIMIT 1;

    IF v_proje_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Is emri bulunamadi.';
    END IF;

    IF v_rol = 'PROJE_YONETICISI'
       AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, v_proje_id, p_firma_id) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu proje icin yetkiniz yok.';
    END IF;

    IF p_atanan_id IS NOT NULL
       AND NOT EXISTS (
            SELECT 1 FROM kullanici
            WHERE kullanici_id = p_atanan_id
              AND firma_id = p_firma_id
              AND aktif = 1
       ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Atanan kullanici bulunamadi.';
    END IF;

    UPDATE is_emri
    SET baslik = p_baslik,
        aciklama = p_aciklama,
        oncelik = p_oncelik,
        atanan_kullanici_id = p_atanan_id,
        baslangic_tarihi = p_baslangic,
        bitis_tarihi = p_bitis
    WHERE is_emri_id = p_is_emri_id
      AND firma_id = p_firma_id;

    SET v_etkilenen_satir = ROW_COUNT();

    SELECT v_etkilenen_satir AS etkilenen_satir;
END //

CREATE PROCEDURE sp_is_emri_durum_guncelle(
    IN p_is_emri_id     INT,
    IN p_firma_id       INT,
    IN p_yeni_durum     VARCHAR(50),
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50),
    IN p_aciklama       VARCHAR(500)
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    DECLARE v_eski_durum VARCHAR(50);
    DECLARE v_proje_id INT;
    DECLARE v_taseron_id INT;
    DECLARE v_atanan_id INT;
    DECLARE v_etkilenen_satir INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    SET v_rol = UPPER(NULLIF(TRIM(p_rol), ''));

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI', 'SAHA_PERSONELI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF p_yeni_durum NOT IN ('BEKLIYOR', 'BASLADI', 'DEVAM_EDIYOR', 'TAMAMLANDI', 'IPTAL') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Gecersiz is emri durumu.';
    END IF;

    SELECT durum, proje_id, taseron_id, atanan_kullanici_id
    INTO v_eski_durum, v_proje_id, v_taseron_id, v_atanan_id
    FROM is_emri
    WHERE is_emri_id = p_is_emri_id
      AND firma_id = p_firma_id
    LIMIT 1;

    IF v_eski_durum IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Is emri bulunamadi.';
    END IF;

    IF v_rol = 'PROJE_YONETICISI'
       AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, v_proje_id, p_firma_id) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu proje icin yetkiniz yok.';
    END IF;

    IF v_rol = 'SAHA_PERSONELI'
       AND (v_atanan_id IS NULL OR v_atanan_id <> p_kullanici_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu is emri icin yetkiniz yok.';
    END IF;

    START TRANSACTION;
        UPDATE is_emri
        SET durum = p_yeni_durum,
            tamamlanma_tarihi = CASE
                WHEN p_yeni_durum = 'TAMAMLANDI' THEN NOW()
                WHEN p_yeni_durum <> 'TAMAMLANDI' THEN NULL
                ELSE tamamlanma_tarihi
            END
        WHERE is_emri_id = p_is_emri_id
          AND firma_id = p_firma_id;

        SET v_etkilenen_satir = ROW_COUNT();

        INSERT INTO is_emri_durum_log (
            firma_id, is_emri_id, yapan_id, eski_durum, yeni_durum, aciklama
        ) VALUES (
            p_firma_id, p_is_emri_id, p_kullanici_id,
            v_eski_durum, p_yeni_durum, p_aciklama
        );

        IF p_yeni_durum = 'TAMAMLANDI' AND v_eski_durum <> 'TAMAMLANDI' THEN
            CALL sp_taseron_performans_guncelle(v_taseron_id, p_firma_id);
        END IF;
    COMMIT;

    SELECT v_etkilenen_satir AS etkilenen_satir;
END //

CREATE PROCEDURE sp_is_emri_sil(
    IN p_is_emri_id     INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT,
    IN p_rol            VARCHAR(50)
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    DECLARE v_eski_durum VARCHAR(50);
    DECLARE v_proje_id INT;
    DECLARE v_etkilenen_satir INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    SET v_rol = UPPER(NULLIF(TRIM(p_rol), ''));

    IF v_rol NOT IN ('SUPER_ADMIN', 'FIRMA_ADMIN', 'PROJE_YONETICISI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    SELECT durum, proje_id
    INTO v_eski_durum, v_proje_id
    FROM is_emri
    WHERE is_emri_id = p_is_emri_id
      AND firma_id = p_firma_id
    LIMIT 1;

    IF v_eski_durum IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Is emri bulunamadi.';
    END IF;

    IF v_rol = 'PROJE_YONETICISI'
       AND fn_kullanici_proje_yetkili_mi(p_kullanici_id, v_proje_id, p_firma_id) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bu proje icin yetkiniz yok.';
    END IF;

    START TRANSACTION;
        UPDATE is_emri
        SET durum = 'IPTAL'
        WHERE is_emri_id = p_is_emri_id
          AND firma_id = p_firma_id;

        SET v_etkilenen_satir = ROW_COUNT();

        INSERT INTO is_emri_durum_log (
            firma_id, is_emri_id, yapan_id, eski_durum, yeni_durum, aciklama
        ) VALUES (
            p_firma_id, p_is_emri_id, p_kullanici_id,
            v_eski_durum, 'IPTAL', 'Is emri iptal edildi'
        );
    COMMIT;

    SELECT v_etkilenen_satir AS etkilenen_satir;
END //

DELIMITER ;