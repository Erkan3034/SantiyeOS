USE santiyeos;

CREATE TABLE proje (
    proje_id            INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    ad                  VARCHAR(200)    NOT NULL,
    aciklama            TEXT            NULL,
    konum               VARCHAR(300)    NULL,
    butce               DECIMAL(15,2)   NOT NULL DEFAULT 0.00
                            CHECK (butce >= 0),
    baslangic_tarihi    DATE            NULL,
    bitis_tarihi        DATE            NULL,
    durum               ENUM('PLANLANDI','DEVAM_EDIYOR',
                             'TAMAMLANDI','IPTAL')
                            NOT NULL DEFAULT 'PLANLANDI',
    butce_uyari_yuzde   TINYINT         NOT NULL DEFAULT 85
                            CHECK (butce_uyari_yuzde BETWEEN 1 AND 99),
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_proje      PRIMARY KEY (proje_id),
    CONSTRAINT fk_proje_firma FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT chk_proje_tarih
        CHECK (bitis_tarihi IS NULL
            OR baslangic_tarihi IS NULL
            OR bitis_tarihi >= baslangic_tarihi),

    INDEX idx_prj_firma_id   (firma_id),
    INDEX idx_prj_durum      (firma_id, durum),
    INDEX idx_prj_created_at (firma_id, created_at)
) ENGINE=InnoDB;


CREATE TABLE proje_kullanici (
    proje_id            INT             NOT NULL,
    kullanici_id        INT             NOT NULL,
    firma_id            INT             NOT NULL,
    atanma_tarihi       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_proje_kullanici PRIMARY KEY (proje_id, kullanici_id),
    CONSTRAINT fk_prjk_proje      FOREIGN KEY (proje_id)
        REFERENCES proje(proje_id) ON DELETE CASCADE,
    CONSTRAINT fk_prjk_kullanici  FOREIGN KEY (kullanici_id)
        REFERENCES kullanici(kullanici_id) ON DELETE CASCADE,
    CONSTRAINT fk_prjk_firma      FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,

    INDEX idx_prjk_kullanici_id (kullanici_id),
    INDEX idx_prjk_firma_id     (firma_id)
) ENGINE=InnoDB;

CREATE TABLE sozlesme (
    sozlesme_id         INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    proje_id            INT             NOT NULL,
    taseron_id          INT             NOT NULL,
    sozlesme_no         VARCHAR(50)     NULL,
    tutar               DECIMAL(15,2)   NOT NULL DEFAULT 0.00
                            CHECK (tutar >= 0),
    baslangic_tarihi    DATE            NULL,
    bitis_tarihi        DATE            NULL,
    aciklama            TEXT            NULL,
    durum               ENUM('AKTIF','TAMAMLANDI','IPTAL')
                            NOT NULL DEFAULT 'AKTIF',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_sozlesme       PRIMARY KEY (sozlesme_id),
    CONSTRAINT fk_soz_firma      FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_soz_proje      FOREIGN KEY (proje_id)
        REFERENCES proje(proje_id) ON DELETE CASCADE,
    CONSTRAINT fk_soz_taseron    FOREIGN KEY (taseron_id)
        REFERENCES taseron(taseron_id) ON DELETE CASCADE,
    CONSTRAINT chk_soz_tarih
        CHECK (bitis_tarihi IS NULL
            OR baslangic_tarihi IS NULL
            OR bitis_tarihi >= baslangic_tarihi),

    INDEX idx_soz_firma_id   (firma_id),
    INDEX idx_soz_proje_id   (firma_id, proje_id),
    INDEX idx_soz_taseron_id (firma_id, taseron_id),
    INDEX idx_soz_durum      (firma_id, durum)
) ENGINE=InnoDB;

CREATE TABLE is_emri (
    is_emri_id          INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    proje_id            INT             NOT NULL,
    taseron_id          INT             NOT NULL,
    atanan_kullanici_id INT             NULL,
    olusturan_id        INT             NOT NULL,
    baslik              VARCHAR(300)    NOT NULL,
    aciklama            TEXT            NULL,
    oncelik             ENUM('DUSUK','NORMAL','YUKSEK','KRITIK')
                            NOT NULL DEFAULT 'NORMAL',
    durum               ENUM('BEKLIYOR','BASLADI','DEVAM_EDIYOR',
                             'TAMAMLANDI','IPTAL','HAKEDISTE')
                            NOT NULL DEFAULT 'BEKLIYOR',
    baslangic_tarihi    DATE            NULL,
    bitis_tarihi        DATE            NULL,
    tamamlanma_tarihi   DATETIME        NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_is_emri      PRIMARY KEY (is_emri_id),
    CONSTRAINT fk_ie_firma     FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_ie_proje     FOREIGN KEY (proje_id)
        REFERENCES proje(proje_id) ON DELETE CASCADE,
    CONSTRAINT fk_ie_taseron   FOREIGN KEY (taseron_id)
        REFERENCES taseron(taseron_id) ON DELETE CASCADE,
    CONSTRAINT fk_ie_atanan    FOREIGN KEY (atanan_kullanici_id)
        REFERENCES kullanici(kullanici_id) ON DELETE SET NULL,
    CONSTRAINT fk_ie_olusturan FOREIGN KEY (olusturan_id)
        REFERENCES kullanici(kullanici_id) ON DELETE CASCADE,

    INDEX idx_ie_firma_id    (firma_id),
    INDEX idx_ie_proje_id    (firma_id, proje_id),
    INDEX idx_ie_taseron_id  (firma_id, taseron_id),
    INDEX idx_ie_atanan      (firma_id, atanan_kullanici_id),
    INDEX idx_ie_durum       (firma_id, durum),
    INDEX idx_ie_oncelik     (firma_id, oncelik),
    INDEX idx_ie_created_at  (firma_id, created_at)
) ENGINE=InnoDB;


CREATE TABLE is_emri_durum_log (
    log_id              INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    is_emri_id          INT             NOT NULL,
    yapan_id            INT             NULL,
    eski_durum          VARCHAR(50)     NULL,
    yeni_durum          VARCHAR(50)     NOT NULL,
    aciklama            VARCHAR(500)    NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ie_durum_log  PRIMARY KEY (log_id),
    CONSTRAINT fk_iedl_firma    FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_iedl_is_emri  FOREIGN KEY (is_emri_id)
        REFERENCES is_emri(is_emri_id) ON DELETE CASCADE,
    CONSTRAINT fk_iedl_yapan    FOREIGN KEY (yapan_id)
        REFERENCES kullanici(kullanici_id) ON DELETE SET NULL,

    INDEX idx_iedl_firma_id   (firma_id),
    INDEX idx_iedl_is_emri_id (firma_id, is_emri_id),
    INDEX idx_iedl_created_at (firma_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE is_emri_not (
    not_id              INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    is_emri_id          INT             NOT NULL,
    kullanici_id        INT             NULL,
    icerik              TEXT            NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ie_not       PRIMARY KEY (not_id),
    CONSTRAINT fk_ien_firma    FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_ien_is_emri  FOREIGN KEY (is_emri_id)
        REFERENCES is_emri(is_emri_id) ON DELETE CASCADE,
    CONSTRAINT fk_ien_kul      FOREIGN KEY (kullanici_id)
        REFERENCES kullanici(kullanici_id) ON DELETE SET NULL,

    INDEX idx_ien_firma_id   (firma_id),
    INDEX idx_ien_is_emri_id (firma_id, is_emri_id),
    INDEX idx_ien_created_at (firma_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE is_emri_rapor (
    rapor_id            INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NOT NULL,
    is_emri_id          INT             NOT NULL,
    kullanici_id        INT             NULL,
    baslik              VARCHAR(200)    NOT NULL,
    icerik              TEXT            NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ie_rapor     PRIMARY KEY (rapor_id),
    CONSTRAINT fk_ier_firma    FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_ier_is_emri  FOREIGN KEY (is_emri_id)
        REFERENCES is_emri(is_emri_id) ON DELETE CASCADE,
    CONSTRAINT fk_ier_kul      FOREIGN KEY (kullanici_id)
        REFERENCES kullanici(kullanici_id) ON DELETE SET NULL,

    INDEX idx_ier_firma_id   (firma_id),
    INDEX idx_ier_is_emri_id (firma_id, is_emri_id),
    INDEX idx_ier_created_at (firma_id, created_at)
) ENGINE=InnoDB;



