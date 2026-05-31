USE santiyeos;

DROP TRIGGER IF EXISTS trg_bildirim_okundu;

DELIMITER //

CREATE TRIGGER trg_bildirim_okundu
BEFORE UPDATE ON bildirim
FOR EACH ROW
BEGIN
    IF NEW.okundu = 1 AND OLD.okundu = 0 THEN
        SET NEW.okundu_tarihi = NOW();
    END IF;
END //

DELIMITER ;
