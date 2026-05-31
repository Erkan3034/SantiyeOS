USE santiyeos;


CREATE TABLE firma (
    firma_id        INT          NOT NULL AUTO_INCREMENT,
    ad              VARCHAR(200) NOT NULL,
    vergi_no        VARCHAR(20)  NOT NULL,
    telefon         VARCHAR(20)  NULL,
    email           VARCHAR(150) NOT NULL,
    adres           TEXT         NULL,
    aktif           TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_firma PRIMARY KEY (firma_id),
    CONSTRAINT uq_firma_vergi_no UNIQUE (vergi_no),
    CONSTRAINT uq_firma_email UNIQUE (email),

    INDEX idx_firma_aktif (aktif)
) ENGINE=InnoDB;

-- 2. ABONELIK PLANLARI
-- Planlar ayri tablo oldugu icin yeni paket eklemek icin schema degisikligi gerekmez.
CREATE TABLE abonelik_plan (
    plan_id        INT           NOT NULL AUTO_INCREMENT,
    ad             VARCHAR(100)  NOT NULL,
    max_proje      SMALLINT      NOT NULL DEFAULT 1,
    max_kullanici  SMALLINT      NOT NULL DEFAULT 2,
    max_taseron    SMALLINT      NOT NULL DEFAULT 5,
    aylik_ucret    DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (aylik_ucret >= 0),
    aktif          TINYINT(1)    NOT NULL DEFAULT 1,

    CONSTRAINT pk_abonelik_plan PRIMARY KEY (plan_id)
) ENGINE=InnoDB;

INSERT INTO abonelik_plan (ad, max_proje, max_kullanici, max_taseron, aylik_ucret)
VALUES
    ('Başlangıç', 1, 2, 5, 499.00),
    ('Profesyonel', 10, 10, 999, 3999.00),
    ('Kurumsal', 999, 999, 999, 9999.00);

-- 3. ABONELIK (Firma - Plan iliskisi, gecmis dahil)
-- Her abonelik degisikligi yeni kayit acar; gecmis abonelikler korunur.
CREATE TABLE abonelik (
    abonelik_id       INT       NOT NULL AUTO_INCREMENT,
    firma_id          INT       NOT NULL,
    plan_id           INT       NOT NULL,
    baslangic_tarihi  DATE      NOT NULL,
    bitis_tarihi      DATE      NOT NULL,
    durum             ENUM('AKTIF','SURESI_DOLDU','IPTAL')
                                      NOT NULL DEFAULT 'AKTIF',
    deneme            TINYINT(1) NOT NULL DEFAULT 0,
    created_at        DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_abonelik PRIMARY KEY (abonelik_id),
    CONSTRAINT fk_abonelik_firma FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,
    CONSTRAINT fk_abonelik_plan FOREIGN KEY (plan_id)
        REFERENCES abonelik_plan(plan_id),
    CONSTRAINT chk_abonelik_tarih CHECK (bitis_tarihi > baslangic_tarihi),

    INDEX idx_abonelik_firma_id (firma_id),
    INDEX idx_abonelik_durum (firma_id, durum),
    INDEX idx_abonelik_bitis (bitis_tarihi)
) ENGINE=InnoDB;

-- 4. ABONELIK ODEME (abonelik odemeleri - audit trail)
CREATE TABLE abonelik_odeme (
    odeme_id       INT           NOT NULL AUTO_INCREMENT,
    abonelik_id    INT           NOT NULL,
    firma_id       INT           NOT NULL,
    tutar          DECIMAL(10,2) NOT NULL CHECK (tutar > 0),
    odeme_tarihi   DATETIME      NOT NULL,
    odeme_yontemi  ENUM('KREDI_KARTI','HAVALE','EFT') NOT NULL,
    referans_no    VARCHAR(100)  NULL,
    durum          ENUM('BEKLIYOR','TAMAMLANDI','BASARISIZ','IADE')
                                 NOT NULL DEFAULT 'BEKLIYOR',
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_abonelik_odeme PRIMARY KEY (odeme_id),
    CONSTRAINT fk_abo_abonelik FOREIGN KEY (abonelik_id)
        REFERENCES abonelik(abonelik_id),
    CONSTRAINT fk_abo_firma FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,

    INDEX idx_abo_firma_id (firma_id),
    INDEX idx_abo_abonelik_id (abonelik_id),
    INDEX idx_abo_tarih (odeme_tarihi)
) ENGINE=InnoDB;
