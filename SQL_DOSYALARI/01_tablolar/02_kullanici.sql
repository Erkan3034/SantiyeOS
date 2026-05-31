USE santiyeos;


CREATE TABLE kullanici (
    kullanici_id        INT             NOT NULL AUTO_INCREMENT,
    firma_id            INT             NULL,
    taseron_id          INT             NULL,
    ad                  VARCHAR(100)    NOT NULL,
    soyad               VARCHAR(100)    NOT NULL,
    email               VARCHAR(150)    NOT NULL,
    sifre_hash          VARCHAR(255)    NOT NULL,
    rol                 ENUM('SUPER_ADMIN','FIRMA_ADMIN','PROJE_YONETICISI',
                             'SAHA_PERSONELI','TASERON_TEMSILCI') NOT NULL,
    telefon             VARCHAR(20)     NULL,
    aktif               TINYINT(1)      NOT NULL DEFAULT 1,
    sifre_degistirmeli  TINYINT(1)      NOT NULL DEFAULT 0,
    son_giris           DATETIME        NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_kullanici    PRIMARY KEY (kullanici_id),
    CONSTRAINT uq_kullanici_email UNIQUE (email),
    CONSTRAINT fk_kullanici_firma FOREIGN KEY (firma_id)
        REFERENCES firma(firma_id) ON DELETE CASCADE,

    INDEX idx_kul_firma_id (firma_id),
    INDEX idx_kul_rol      (rol),
    INDEX idx_kul_aktif    (firma_id, aktif)
) ENGINE=InnoDB;

CREATE TABLE refresh_token (
    token_id            INT             NOT NULL AUTO_INCREMENT,
    kullanici_id        INT             NOT NULL,
    token_hash          VARCHAR(255)    NOT NULL,
    son_kullanim_tarihi DATETIME        NOT NULL,
    ip_adresi           VARCHAR(45)     NULL,
    user_agent          VARCHAR(500)    NULL,
    iptal_edildi        TINYINT(1)      NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_refresh_token PRIMARY KEY (token_id),
    CONSTRAINT uq_refresh_token UNIQUE (token_hash),
    CONSTRAINT fk_rt_kullanici  FOREIGN KEY (kullanici_id)
        REFERENCES kullanici(kullanici_id) ON DELETE CASCADE,

    INDEX idx_rt_kullanici_id       (kullanici_id),
    INDEX idx_rt_token_hash         (token_hash),
    INDEX idx_rt_son_kullanim       (son_kullanim_tarihi),
    INDEX idx_rt_iptal              (iptal_edildi)
) ENGINE=InnoDB;


