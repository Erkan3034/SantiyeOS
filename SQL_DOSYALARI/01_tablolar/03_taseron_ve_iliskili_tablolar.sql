USE santiyeos;

CREATE TABLE malzeme_kategori (
    kategori_id         INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    ad                  VARCHAR(100)    NOT NULL,
    aciklama            VARCHAR(300)    NULL,
    aktif               TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_malzeme_kategori PRIMARY KEY (kategori_id),
    CONSTRAINT fk_mk_firma FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT uq_mk_firma_ad UNIQUE (firma_id, ad),

    INDEX idx_mk_firma_id (firma_id),
    INDEX idx_mk_aktif    (firma_id, aktif)
) ENGINE=InnoDB;


-- taseron 
CREATE TABLE taseron (
    taseron_id          INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    ad                  VARCHAR(200)    NOT NULL,
    vergi_no            VARCHAR(20)     NULL,
    yetkili_ad          VARCHAR(200)    NULL,
    telefon             VARCHAR(20)     NULL,
    email               VARCHAR(150)    NULL,
    uzmanlik            VARCHAR(200)    NULL,
    performans_skoru    DECIMAL(5,2)    NOT NULL DEFAULT 100.00
                            CHECK (performans_skoru >= 0
                               AND performans_skoru <= 100),
    aktif               TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_taseron       PRIMARY KEY (taseron_id),
    CONSTRAINT uq_taseron_vergi UNIQUE (firma_id, vergi_no),
    CONSTRAINT fk_taseron_firma FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,

    INDEX idx_tas_firma_id    (firma_id),
    INDEX idx_tas_aktif       (firma_id, aktif),
    INDEX idx_tas_performans  (firma_id, performans_skoru)
) ENGINE=InnoDB;

ALTER TABLE kullanici
    ADD CONSTRAINT fk_kullanici_taseron
    FOREIGN KEY (taseron_id)
    REFERENCES taseron(taseron_id) ON DELETE SET NULL;
