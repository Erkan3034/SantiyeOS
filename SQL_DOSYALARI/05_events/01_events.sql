USE santiyeos;


DELIMITER //

CREATE EVENT IF NOT EXISTS ev_abonelik_kontrol
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
    UPDATE abonelik
    SET durum = 'SURESI_DOLDU'
    WHERE bitis_tarihi < CURDATE()
      AND durum = 'AKTIF';
END //

CREATE EVENT IF NOT EXISTS ev_token_temizle
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
    DELETE FROM refresh_token
    WHERE son_kullanim_tarihi < NOW()
       OR iptal_edildi = 1;
END //

DELIMITER ;

SET GLOBAL event_scheduler = ON;

