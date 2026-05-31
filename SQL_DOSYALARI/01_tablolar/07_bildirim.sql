USE santiyeos;

CREATE TABLE bildirim (
    bildirim_id         INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    kullanici_id        INT             NOT NULL,
    baslik              VARCHAR(200)    NOT NULL,
    mesaj               TEXT            NOT NULL,
    tip                 ENUM('IS_EMRI','HAKEDIS','ODEME',
                             'BUTCE','SISTEM')  NOT NULL,
    referans_tablo      VARCHAR(50)     NULL,
    referans_id         INT             NULL,
    okundu              TINYINT(1)      NOT NULL DEFAULT 0,
    okundu_tarihi       DATETIME        NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_bildirim      PRIMARY KEY (bildirim_id),
    CONSTRAINT fk_bil_firma     FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_bil_kullanici FOREIGN KEY (kullanici_id)
        REFERENCES kullanici(kullanici_id) ON DELETE CASCADE,

    INDEX idx_bil_firma_id    (firma_id),
    INDEX idx_bil_kullanici   (firma_id, kullanici_id),
    INDEX idx_bil_okundu      (firma_id, kullanici_id, okundu),
    INDEX idx_bil_created_at  (firma_id, created_at)
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 0;
