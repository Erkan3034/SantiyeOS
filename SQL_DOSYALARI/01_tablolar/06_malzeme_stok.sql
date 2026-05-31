USE santiyeos;

CREATE TABLE malzeme (
    malzeme_id          INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    kategori_id         INT             NULL,
    ad                  VARCHAR(200)    NOT NULL,
    birim               ENUM('ADET','KG','TON','METRE',
                             'M2','M3','LITRE')
                            NOT NULL DEFAULT 'ADET',
    birim_fiyat         DECIMAL(12,2)   NOT NULL DEFAULT 0.00
                            CHECK (birim_fiyat >= 0),
    stok_miktari        DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    min_stok            DECIMAL(12,2)   NOT NULL DEFAULT 0.00
                            CHECK (min_stok >= 0),
    aktif               TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_malzeme     PRIMARY KEY (malzeme_id),
    CONSTRAINT fk_mal_firma   FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_mal_kategori FOREIGN KEY (kategori_id)
        REFERENCES malzeme_kategori(kategori_id) ON DELETE SET NULL,

    INDEX idx_mal_firma_id   (firma_id),
    INDEX idx_mal_kategori   (firma_id, kategori_id),
    INDEX idx_mal_aktif      (firma_id, aktif),
    INDEX idx_mal_stok       (firma_id, stok_miktari)
) ENGINE=InnoDB;

CREATE TABLE stok_hareket (
    hareket_id          INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    malzeme_id          INT             NOT NULL,
    proje_id            INT             NULL,
    is_emri_id          INT             NULL,
    kaydeden_id         INT             NULL,
    hareket_tipi        ENUM('GIRIS','CIKIS') NOT NULL,
    miktar              DECIMAL(12,2)   NOT NULL CHECK (miktar > 0),
    birim_fiyat         DECIMAL(12,2)   NULL,
    aciklama            TEXT            NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_stok_hareket  PRIMARY KEY (hareket_id),
    CONSTRAINT fk_sh_firma      FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_sh_malzeme    FOREIGN KEY (malzeme_id)
        REFERENCES malzeme(malzeme_id) ON DELETE CASCADE,
    CONSTRAINT fk_sh_proje      FOREIGN KEY (proje_id)
        REFERENCES proje(proje_id) ON DELETE SET NULL,
    CONSTRAINT fk_sh_is_emri    FOREIGN KEY (is_emri_id)
        REFERENCES is_emri(is_emri_id) ON DELETE SET NULL,
    CONSTRAINT fk_sh_kaydeden   FOREIGN KEY (kaydeden_id)
        REFERENCES kullanici(kullanici_id) ON DELETE SET NULL,

    INDEX idx_sh_firma_id   (firma_id),
    INDEX idx_sh_malzeme_id (firma_id, malzeme_id),
    INDEX idx_sh_proje_id   (firma_id, proje_id),
    INDEX idx_sh_created_at (firma_id, created_at)
) ENGINE=InnoDB;
