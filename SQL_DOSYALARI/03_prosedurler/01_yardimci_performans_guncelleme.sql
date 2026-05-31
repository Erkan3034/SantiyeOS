
USE santiyeos;

DROP PROCEDURE IF EXISTS sp_taseron_performans_guncelle;

DELIMITER //

CREATE PROCEDURE sp_taseron_performans_guncelle(
    IN p_taseron_id INT,
    IN p_firma_id   INT
)
BEGIN
    DECLARE v_toplam        INT DEFAULT 0;
    DECLARE v_zamaninda     INT DEFAULT 0;
    DECLARE v_yeni_skor     DECIMAL(5,2);

    SELECT
        COUNT(*),
        SUM(CASE WHEN tamamlanma_tarihi <= bitis_tarihi
                  AND bitis_tarihi IS NOT NULL THEN 1 ELSE 0 END)
    INTO v_toplam, v_zamaninda
    FROM is_emri
    WHERE taseron_id = p_taseron_id
      AND firma_id   = p_firma_id
      AND durum      = 'TAMAMLANDI';

    IF v_toplam > 0 THEN
        SET v_yeni_skor = ROUND((v_zamaninda / v_toplam) * 100, 2);
        UPDATE taseron
        SET performans_skoru = v_yeni_skor
        WHERE taseron_id = p_taseron_id AND firma_id = p_firma_id;
    END IF;
END //

DELIMITER ;
