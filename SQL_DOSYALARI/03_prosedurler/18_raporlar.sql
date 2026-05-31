USE santiyeos;

DROP PROCEDURE IF EXISTS sp_dashboard_ozet;
DROP PROCEDURE IF EXISTS sp_genel_ozet_raporu;
DROP PROCEDURE IF EXISTS sp_proje_maliyet_raporu;
DROP PROCEDURE IF EXISTS sp_taseron_performans_raporu;

DELIMITER //

CREATE PROCEDURE sp_dashboard_ozet(
    IN p_firma_id INT
)
BEGIN
    SELECT
        (SELECT COUNT(*) FROM proje WHERE firma_id = p_firma_id AND durum = 'DEVAM_EDIYOR') AS aktif_proje,
        (SELECT COUNT(*) FROM is_emri WHERE firma_id = p_firma_id AND durum NOT IN ('TAMAMLANDI','IPTAL')) AS aktif_is_emri,
        (SELECT COUNT(*) FROM is_emri WHERE firma_id = p_firma_id AND durum NOT IN ('TAMAMLANDI','IPTAL') AND bitis_tarihi < CURDATE()) AS geciken_is_emri,
        (SELECT COUNT(*) FROM hakedis WHERE firma_id = p_firma_id AND onay_durumu = 'BEKLIYOR') AS bekleyen_hakedis,
        (SELECT COUNT(*) FROM malzeme WHERE firma_id = p_firma_id AND aktif = 1 AND stok_miktari <= min_stok) AS kritik_stok_sayisi;

    SELECT d.durum, COALESCE(ie.toplam, 0) AS toplam
    FROM (
        SELECT 'BEKLIYOR' AS durum, 1 AS sira
        UNION ALL SELECT 'BASLADI', 2
        UNION ALL SELECT 'DEVAM_EDIYOR', 3
        UNION ALL SELECT 'HAKEDISTE', 4
        UNION ALL SELECT 'TAMAMLANDI', 5
        UNION ALL SELECT 'IPTAL', 6
    ) d
    LEFT JOIN (
        SELECT durum, COUNT(*) AS toplam
        FROM is_emri
        WHERE firma_id = p_firma_id
        GROUP BY durum
    ) ie ON ie.durum = d.durum
    ORDER BY d.sira;

    SELECT d.durum, COALESCE(h.toplam, 0) AS toplam
    FROM (
        SELECT 'BEKLIYOR' AS durum, 1 AS sira
        UNION ALL SELECT 'ONAYLANDI', 2
        UNION ALL SELECT 'REDDEDILDI', 3
        UNION ALL SELECT 'ITIRAZDA', 4
    ) d
    LEFT JOIN (
        SELECT onay_durumu AS durum, COUNT(*) AS toplam
        FROM hakedis
        WHERE firma_id = p_firma_id
        GROUP BY onay_durumu
    ) h ON h.durum = d.durum
    ORDER BY d.sira;

    SELECT
        COALESCE(SUM(CASE WHEN h.onay_durumu = 'ONAYLANDI' THEN h.tutar ELSE 0 END), 0) AS toplam_onaylanan_hakedis,
        COALESCE(SUM(CASE WHEN h.onay_durumu = 'ONAYLANDI' THEN COALESCE(o.odenen_tutar, 0) ELSE 0 END), 0) AS toplam_odeme,
        COALESCE(SUM(CASE WHEN h.onay_durumu = 'ONAYLANDI' THEN h.tutar - COALESCE(o.odenen_tutar, 0) ELSE 0 END), 0) AS toplam_odenmemis,
        COALESCE(SUM(CASE WHEN h.onay_durumu = 'BEKLIYOR' THEN 1 ELSE 0 END), 0) AS bekleyen_hakedis,
        COALESCE(SUM(CASE WHEN h.onay_durumu = 'ONAYLANDI' THEN 1 ELSE 0 END), 0) AS onaylanan_hakedis
    FROM hakedis h
    LEFT JOIN (
        SELECT firma_id, hakedis_id, SUM(tutar) AS odenen_tutar
        FROM odeme
        WHERE firma_id = p_firma_id
        GROUP BY firma_id, hakedis_id
    ) o ON o.hakedis_id = h.hakedis_id AND o.firma_id = h.firma_id
    WHERE h.firma_id = p_firma_id;

    SELECT
        m.malzeme_id,
        m.ad,
        mk.ad AS kategori_ad,
        m.birim,
        m.stok_miktari,
        m.min_stok
    FROM malzeme m
    LEFT JOIN malzeme_kategori mk ON mk.kategori_id = m.kategori_id AND mk.firma_id = m.firma_id
    WHERE m.firma_id = p_firma_id
      AND m.aktif = 1
      AND m.stok_miktari <= m.min_stok
    ORDER BY (m.min_stok - m.stok_miktari) DESC, m.ad ASC
    LIMIT 5;

    SELECT
        p.proje_id,
        p.ad,
        p.bitis_tarihi,
        p.durum,
        DATEDIFF(p.bitis_tarihi, CURDATE()) AS kalan_gun
    FROM proje p
    WHERE p.firma_id = p_firma_id
      AND p.durum IN ('PLANLANDI', 'DEVAM_EDIYOR')
      AND p.bitis_tarihi IS NOT NULL
      AND p.bitis_tarihi BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)
    ORDER BY p.bitis_tarihi ASC, p.proje_id ASC
    LIMIT 5;
END //

CREATE PROCEDURE sp_genel_ozet_raporu(
    IN p_firma_id INT
)
BEGIN
    SELECT
        (SELECT COUNT(*) FROM proje WHERE firma_id = p_firma_id AND durum = 'DEVAM_EDIYOR') AS aktif_proje,
        (SELECT COUNT(*) FROM is_emri WHERE firma_id = p_firma_id AND durum NOT IN ('TAMAMLANDI','IPTAL')) AS aktif_is_emri,
        (SELECT COUNT(*) FROM is_emri WHERE firma_id = p_firma_id AND durum NOT IN ('TAMAMLANDI','IPTAL') AND bitis_tarihi < CURDATE()) AS geciken_is_emri,
        (SELECT COUNT(*) FROM hakedis WHERE firma_id = p_firma_id AND onay_durumu = 'BEKLIYOR') AS bekleyen_hakedis,
        (SELECT COALESCE(SUM(h.tutar - COALESCE((SELECT SUM(o.tutar) FROM odeme o WHERE o.hakedis_id = h.hakedis_id AND o.firma_id = h.firma_id), 0)), 0)
         FROM hakedis h WHERE h.firma_id = p_firma_id AND h.onay_durumu = 'ONAYLANDI') AS toplam_odenmemis,
        (SELECT COUNT(*) FROM malzeme WHERE firma_id = p_firma_id AND aktif = 1 AND stok_miktari <= min_stok) AS kritik_stok_sayisi;
END //

CREATE PROCEDURE sp_proje_maliyet_raporu(
    IN p_firma_id INT,
    IN p_kullanici_id INT,
    IN p_rol VARCHAR(50),
    IN p_proje_id INT
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    SET v_rol = UPPER(TRIM(p_rol));

    SELECT
        p.proje_id,
        p.ad AS proje_ad,
        p.butce AS toplam_butce,
        p.durum,
        COALESCE(ie_ozet.is_emri_sayisi, 0) AS is_emri_sayisi,
        COALESCE(ie_ozet.taseron_sayisi, 0) AS taseron_sayisi,
        COALESCE(h_ozet.toplam_onaylanan_hakedis, 0) AS toplam_onaylanan_hakedis,
        COALESCE(o_ozet.toplam_odeme, 0) AS toplam_odeme,
        p.butce - COALESCE(o_ozet.toplam_odeme, 0) AS kalan_butce,
        fn_proje_butce_kullanim(p.proje_id, p.firma_id) AS butce_kullanim_yuzdesi
    FROM proje p
    LEFT JOIN (
        SELECT firma_id, proje_id,
               COUNT(DISTINCT is_emri_id) AS is_emri_sayisi,
               COUNT(DISTINCT taseron_id) AS taseron_sayisi
        FROM is_emri
        GROUP BY firma_id, proje_id
    ) ie_ozet ON ie_ozet.proje_id = p.proje_id AND ie_ozet.firma_id = p.firma_id
    LEFT JOIN (
        SELECT ie.firma_id, ie.proje_id,
               SUM(CASE WHEN h.onay_durumu = 'ONAYLANDI' THEN h.tutar ELSE 0 END) AS toplam_onaylanan_hakedis
        FROM is_emri ie
        INNER JOIN hakedis h ON h.is_emri_id = ie.is_emri_id AND h.firma_id = ie.firma_id
        GROUP BY ie.firma_id, ie.proje_id
    ) h_ozet ON h_ozet.proje_id = p.proje_id AND h_ozet.firma_id = p.firma_id
    LEFT JOIN (
        SELECT ie.firma_id, ie.proje_id, SUM(o.tutar) AS toplam_odeme
        FROM is_emri ie
        INNER JOIN hakedis h ON h.is_emri_id = ie.is_emri_id AND h.firma_id = ie.firma_id
        INNER JOIN odeme o ON o.hakedis_id = h.hakedis_id AND o.firma_id = h.firma_id
        GROUP BY ie.firma_id, ie.proje_id
    ) o_ozet ON o_ozet.proje_id = p.proje_id AND o_ozet.firma_id = p.firma_id
    WHERE p.firma_id = p_firma_id
      AND p.proje_id = p_proje_id
      AND (
          v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
          OR (
              v_rol = 'PROJE_YONETICISI'
              AND EXISTS (
                  SELECT 1
                  FROM proje_kullanici pk
                  WHERE pk.proje_id = p.proje_id
                    AND pk.firma_id = p.firma_id
                    AND pk.kullanici_id = p_kullanici_id
              )
          )
      );
END //

CREATE PROCEDURE sp_taseron_performans_raporu(
    IN p_firma_id INT,
    IN p_kullanici_id INT,
    IN p_rol VARCHAR(50)
)
BEGIN
    DECLARE v_rol VARCHAR(50);
    SET v_rol = UPPER(TRIM(p_rol));

    SELECT
        t.taseron_id,
        t.ad,
        t.uzmanlik,
        t.performans_skoru,
        COUNT(DISTINCT ie.is_emri_id) AS toplam_is,
        COUNT(DISTINCT CASE WHEN ie.durum = 'TAMAMLANDI' THEN ie.is_emri_id END) AS tamamlanan,
        COUNT(DISTINCT CASE WHEN ie.durum = 'IPTAL' THEN ie.is_emri_id END) AS iptal,
        COUNT(DISTINCT CASE WHEN ie.durum = 'TAMAMLANDI' AND ie.tamamlanma_tarihi > ie.bitis_tarihi THEN ie.is_emri_id END) AS geciken,
        ROUND(AVG(CASE WHEN ie.durum = 'TAMAMLANDI' THEN DATEDIFF(ie.tamamlanma_tarihi, ie.bitis_tarihi) END), 1) AS ort_gecikme_gun,
        fn_taseron_bakiye(t.taseron_id, t.firma_id) AS odenmemis_bakiye
    FROM taseron t
    LEFT JOIN is_emri ie ON ie.taseron_id = t.taseron_id AND ie.firma_id = t.firma_id
    WHERE t.firma_id = p_firma_id
      AND t.aktif = 1
      AND (
          v_rol IN ('SUPER_ADMIN', 'FIRMA_ADMIN')
          OR (
              v_rol = 'PROJE_YONETICISI'
              AND EXISTS (
                  SELECT 1
                  FROM proje_kullanici pk
                  WHERE pk.proje_id = ie.proje_id
                    AND pk.firma_id = ie.firma_id
                    AND pk.kullanici_id = p_kullanici_id
              )
          )
      )
    GROUP BY t.taseron_id, t.ad, t.uzmanlik, t.performans_skoru, t.firma_id
    ORDER BY t.performans_skoru DESC;
END //

DELIMITER ;
