USE santiyeos;

CREATE TABLE hakedis (
    hakedis_id          INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    is_emri_id          INT             NOT NULL,
    talep_eden_id       INT             NOT NULL,
    onaylayan_id        INT             NULL,
    tutar               DECIMAL(15,2)   NOT NULL
                            CHECK (tutar >= 0),
    onay_durumu         ENUM('BEKLIYOR','ONAYLANDI',
                             'REDDEDILDI','ITIRAZDA')
                            NOT NULL DEFAULT 'BEKLIYOR',
    onay_tarihi         DATETIME        NULL,
    aciklama            TEXT            NULL,
    red_gerekce         TEXT            NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_hakedis       PRIMARY KEY (hakedis_id),
    CONSTRAINT uq_hakedis_emri  UNIQUE (is_emri_id),
    CONSTRAINT fk_hak_firma     FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_hak_is_emri   FOREIGN KEY (is_emri_id)
        REFERENCES is_emri(is_emri_id) ON DELETE CASCADE,
    CONSTRAINT fk_hak_talep     FOREIGN KEY (talep_eden_id)
        REFERENCES kullanici(kullanici_id) ON DELETE CASCADE,
    CONSTRAINT fk_hak_onay      FOREIGN KEY (onaylayan_id)
        REFERENCES kullanici(kullanici_id) ON DELETE SET NULL,

    INDEX idx_hak_firma_id    (firma_id),
    INDEX idx_hak_is_emri_id  (firma_id, is_emri_id),
    INDEX idx_hak_onay_durumu (firma_id, onay_durumu),
    INDEX idx_hak_created_at  (firma_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE hakedis_itiraz (
    itiraz_id           INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    hakedis_id          INT             NOT NULL,
    itiraz_eden_id      INT             NOT NULL,
    yanit_veren_id      INT             NULL,
    itiraz_metni        TEXT            NOT NULL,
    yanit_metni         TEXT            NULL,
    durum               ENUM('ACIK','YANITLANDI','KAPANDI')
                            NOT NULL DEFAULT 'ACIK',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_hakedis_itiraz PRIMARY KEY (itiraz_id),
    CONSTRAINT fk_hit_firma      FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_hit_hakedis    FOREIGN KEY (hakedis_id)
        REFERENCES hakedis(hakedis_id) ON DELETE CASCADE,
    CONSTRAINT fk_hit_itiraz_eden FOREIGN KEY (itiraz_eden_id)
        REFERENCES kullanici(kullanici_id) ON DELETE CASCADE,
    CONSTRAINT fk_hit_yanit_veren FOREIGN KEY (yanit_veren_id)
        REFERENCES kullanici(kullanici_id) ON DELETE SET NULL,

    INDEX idx_hit_firma_id   (firma_id),
    INDEX idx_hit_hakedis_id (firma_id, hakedis_id),
    INDEX idx_hit_durum      (firma_id, durum),
    INDEX idx_hit_created_at (firma_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE hakedis_log (
    log_id              INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    hakedis_id          INT             NOT NULL,
    islem               ENUM('OLUSTURULDU','ONAYLANDI',
                             'REDDEDILDI','ITIRAZ_EDILDI',
                             'ITIRAZ_KAPANDI')   NOT NULL,
    yapan_id            INT             NULL,
    eski_durum          VARCHAR(50)     NULL,
    yeni_durum          VARCHAR(50)     NULL,
    aciklama            TEXT            NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_hakedis_log PRIMARY KEY (log_id),
    CONSTRAINT fk_hl_hakedis  FOREIGN KEY (hakedis_id)
        REFERENCES hakedis(hakedis_id) ON DELETE CASCADE,
    CONSTRAINT fk_hl_yapan    FOREIGN KEY (yapan_id)
        REFERENCES kullanici(kullanici_id) ON DELETE SET NULL,

    INDEX idx_hl_firma_id   (firma_id),
    INDEX idx_hl_hakedis_id (firma_id, hakedis_id),
    INDEX idx_hl_created_at (firma_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE odeme (
    odeme_id            INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    hakedis_id          INT             NOT NULL,
    kaydeden_id         INT             NULL,
    tutar               DECIMAL(15,2)   NOT NULL CHECK (tutar > 0),
    odeme_tarihi        DATE            NOT NULL,
    odeme_yontemi       ENUM('HAVALE','EFT','CEK','NAKIT')
                            NOT NULL DEFAULT 'HAVALE',
    aciklama            TEXT            NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_odeme       PRIMARY KEY (odeme_id),
    CONSTRAINT fk_ode_firma   FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_ode_hakedis FOREIGN KEY (hakedis_id)
        REFERENCES hakedis(hakedis_id) ON DELETE CASCADE,
    CONSTRAINT fk_ode_kaydeden FOREIGN KEY (kaydeden_id)
        REFERENCES kullanici(kullanici_id) ON DELETE SET NULL,

    INDEX idx_ode_firma_id    (firma_id),
    INDEX idx_ode_hakedis_id  (firma_id, hakedis_id),
    INDEX idx_ode_odeme_tarihi (firma_id, odeme_tarihi),
    INDEX idx_ode_created_at  (firma_id, created_at)
) ENGINE=InnoDB;

