USE santiyeos;


DELIMITER //
CREATE PROCEDURE sp_sozlesme_ekle(
    IN p_firma_id       INT,
    IN p_proje_id       INT,
    IN p_taseron_id     INT,
    IN p_sozlesme_no    VARCHAR(50),
    IN p_tutar          DECIMAL(15,2),
    IN p_baslangic      DATE,
    IN p_bitis          DATE,
    IN p_aciklama       TEXT
)
BEGIN
    DECLARE v_id INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    START TRANSACTION;
        INSERT INTO sozlesme (
            firma_id, proje_id, taseron_id, sozlesme_no,
            tutar, baslangic_tarihi, bitis_tarihi, aciklama
        ) VALUES (
            p_firma_id, p_proje_id, p_taseron_id, p_sozlesme_no,
            p_tutar, p_baslangic, p_bitis, p_aciklama
        );
        SET v_id = LAST_INSERT_ID();
    COMMIT;
    SELECT v_id AS sozlesme_id;
END //

CREATE PROCEDURE sp_sozlesme_guncelle(
    IN p_sozlesme_id    INT,
    IN p_firma_id       INT,
    IN p_tutar          DECIMAL(15,2),
    IN p_baslangic      DATE,
    IN p_bitis          DATE,
    IN p_aciklama       TEXT,
    IN p_durum          VARCHAR(50)
)
BEGIN
    UPDATE sozlesme
    SET tutar = p_tutar, baslangic_tarihi = p_baslangic,
        bitis_tarihi = p_bitis, aciklama = p_aciklama, durum = p_durum
    WHERE sozlesme_id = p_sozlesme_id AND firma_id = p_firma_id;
    SELECT ROW_COUNT() AS etkilenen_satir;
END //

CREATE PROCEDURE sp_sozlesme_sil(
    IN p_sozlesme_id    INT,
    IN p_firma_id       INT,
    IN p_kullanici_id   INT
)
BEGIN
    IF NOT fn_kullanici_yetki_kontrol(p_kullanici_id, 'SUPER_ADMIN,FIRMA_ADMIN,PROJE_YONETICISI') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Yetkisiz islem.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM is_emri ie
        INNER JOIN sozlesme s ON s.proje_id = ie.proje_id AND s.taseron_id = ie.taseron_id
        WHERE s.sozlesme_id = p_sozlesme_id AND s.firma_id = p_firma_id
          AND ie.durum NOT IN ('TAMAMLANDI','IPTAL')
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Aktif is emirleri olan sozlesme iptal edilemez.';
    END IF;

    UPDATE sozlesme SET durum = 'IPTAL'
    WHERE sozlesme_id = p_sozlesme_id AND firma_id = p_firma_id;
END //

CREATE PROCEDURE sp_sozlesme_listele(
    IN p_firma_id   INT,
    IN p_proje_id   INT,
    IN p_limit      INT,
    IN p_offset     INT,
    OUT p_toplam    INT
)
BEGIN
    SELECT COUNT(*) INTO p_toplam
    FROM sozlesme
    WHERE (p_firma_id IS NULL OR firma_id = p_firma_id)
      AND (p_proje_id IS NULL OR proje_id = p_proje_id);

    SELECT
        s.sozlesme_id, s.sozlesme_no, s.tutar,
        s.baslangic_tarihi, s.bitis_tarihi, s.durum,
        p.ad AS proje_ad, t.ad AS taseron_ad, t.uzmanlik
    FROM sozlesme s
    INNER JOIN proje p ON p.proje_id = s.proje_id
    INNER JOIN taseron t ON t.taseron_id = s.taseron_id
    WHERE (p_firma_id IS NULL OR s.firma_id = p_firma_id)
      AND (p_proje_id IS NULL OR s.proje_id = p_proje_id)
    ORDER BY s.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END //

CREATE PROCEDURE sp_sozlesme_getir(
    IN p_sozlesme_id    INT,
    IN p_firma_id       INT
)
BEGIN
    SELECT s.*, p.ad AS proje_ad, t.ad AS taseron_ad
    FROM sozlesme s
    INNER JOIN proje p ON p.proje_id = s.proje_id
    INNER JOIN taseron t ON t.taseron_id = s.taseron_id
    WHERE s.sozlesme_id = p_sozlesme_id
      AND (p_firma_id IS NULL OR s.firma_id = p_firma_id);
END //

DELIMITER ;
