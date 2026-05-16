package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.IsEmri;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.IsEmriRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class IsEmriRepositoryImpl implements IsEmriRepository {

    private final SimpleJdbcCall isEmriListeleCall;
    private final SimpleJdbcCall isEmriGetirCall;
    private final SimpleJdbcCall isEmriEkleCall;
    private final SimpleJdbcCall isEmriGuncelleCall;
    private final SimpleJdbcCall isEmriDurumGuncelleCall;
    private final SimpleJdbcCall isEmriSilCall;

    public IsEmriRepositoryImpl(DataSource dataSource) {
        RowMapper<IsEmri> isEmriRowMapper = this::mapRowToIsEmri;

        this.isEmriListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_proje_id", Types.INTEGER),
                        new SqlParameter("p_taseron_id", Types.INTEGER),
                        new SqlParameter("p_durum", Types.VARCHAR),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", isEmriRowMapper);

        this.isEmriGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_is_emri_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_taseron_id", Types.INTEGER)
                )
                .returningResultSet("items", isEmriRowMapper);

        this.isEmriEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_proje_id", Types.INTEGER),
                        new SqlParameter("p_taseron_id", Types.INTEGER),
                        new SqlParameter("p_atanan_id", Types.INTEGER),
                        new SqlParameter("p_olusturan_id", Types.INTEGER),
                        new SqlParameter("p_baslik", Types.VARCHAR),
                        new SqlParameter("p_aciklama", Types.LONGVARCHAR),
                        new SqlParameter("p_oncelik", Types.VARCHAR),
                        new SqlParameter("p_baslangic", Types.DATE),
                        new SqlParameter("p_bitis", Types.DATE)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("is_emri_id"));

        this.isEmriGuncelleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_guncelle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_is_emri_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_baslik", Types.VARCHAR),
                        new SqlParameter("p_aciklama", Types.LONGVARCHAR),
                        new SqlParameter("p_oncelik", Types.VARCHAR),
                        new SqlParameter("p_atanan_id", Types.INTEGER),
                        new SqlParameter("p_baslangic", Types.DATE),
                        new SqlParameter("p_bitis", Types.DATE)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.isEmriDurumGuncelleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_durum_guncelle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_is_emri_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_yeni_durum", Types.VARCHAR),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_aciklama", Types.VARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.isEmriSilCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_sil")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_is_emri_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }


    @Override
    public PageResult<IsEmri> listele(
            Integer firmaId,
            Integer projeId,
            Integer taseronId,
            String durum,
            int limit,
            int offset
    ) {
        Map<String, Object> result = isEmriListeleCall.execute(
                firmaId,
                projeId,
                taseronId,
                durum,
                limit,
                offset
        );

        List<IsEmri> items = getItems(result);
        Integer total = (Integer) result.get("p_toplam");

        return new PageResult<>(items, total, limit, offset);
    }

    @Override
    public IsEmri getir(Integer firmaId, Integer taseronId, Integer isEmriId) {
        Map<String, Object> result = isEmriGetirCall.execute(isEmriId, firmaId, taseronId);
        List<IsEmri> items = getItems(result);

        if (items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, Integer olusturanId, IsEmri isEmri) {
        Map<String, Object> result = isEmriEkleCall.execute(
                firmaId,
                isEmri.getProjeId(),
                isEmri.getTaseronId(),
                isEmri.getAtananKullaniciId(),
                olusturanId,
                isEmri.getBaslik(),
                isEmri.getAciklama(),
                isEmri.getOncelik(),
                toSqlDate(isEmri.getBaslangicTarihi()),
                toSqlDate(isEmri.getBitisTarihi())
        );

        List<Integer> items = getItems(result);

        if (items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }

    @Override
    public Integer guncelle(Integer firmaId, Integer isEmriId, Integer kullaniciId, IsEmri isEmri) {
        Map<String, Object> result = isEmriGuncelleCall.execute(
                isEmriId,
                firmaId,
                kullaniciId,
                isEmri.getBaslik(),
                isEmri.getAciklama(),
                isEmri.getOncelik(),
                isEmri.getAtananKullaniciId(),
                toSqlDate(isEmri.getBaslangicTarihi()),
                toSqlDate(isEmri.getBitisTarihi())
        );

        List<Integer> items = getItems(result);

        if (items.isEmpty()) {
            return 0;
        }

        return items.get(0);
    }

    @Override
    public Integer durumGuncelle(Integer firmaId, Integer isEmriId, Integer kullaniciId, String yeniDurum, String aciklama) {
        Map<String, Object> result = isEmriDurumGuncelleCall.execute(
                isEmriId,
                firmaId,
                yeniDurum,
                kullaniciId,
                aciklama
        );

        List<Integer> items = getItems(result);

        if (items.isEmpty()) {
            return 0;
        }

        return items.get(0);
    }

    @Override
    public Integer sil(Integer firmaId, Integer isEmriId, Integer kullaniciId) {
        Map<String, Object> result = isEmriSilCall.execute(isEmriId, firmaId, kullaniciId);
        List<Integer> items = getItems(result);

        if (items.isEmpty()) {
            return 0;
        }

        return items.get(0);
    }

    // DB kolonlarını Java nesnesine çeviren tek yer burası.
    private IsEmri mapRowToIsEmri(ResultSet rs, int rowNum) throws SQLException {
        return IsEmri.builder()
                .isEmriId(rs.getInt("is_emri_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .projeId(getInteger(rs, "proje_id"))
                .taseronId(getInteger(rs, "taseron_id"))
                .atananKullaniciId(getInteger(rs, "atanan_kullanici_id"))
                .olusturanId(getInteger(rs, "olusturan_id"))
                .baslik(rs.getString("baslik"))
                .aciklama(rs.getString("aciklama"))
                .oncelik(rs.getString("oncelik"))
                .durum(rs.getString("durum"))
                .baslangicTarihi(getLocalDate(rs, "baslangic_tarihi"))
                .bitisTarihi(getLocalDate(rs, "bitis_tarihi"))
                .tamamlanmaTarihi(getLocalDateTime(rs, "tamamlanma_tarihi"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .updatedAt(getLocalDateTime(rs, "updated_at"))
                .projeAd(rs.getString("proje_ad"))
                .taseronAd(rs.getString("taseron_ad"))
                .taseronUzmanlik(rs.getString("taseron_uzmanlik"))
                .atananKullanici(rs.getString("atanan_kullanici"))
                .olusturan(rs.getString("olusturan"))
                .notSayisi(getInteger(rs, "not_sayisi"))
                .raporSayisi(getInteger(rs, "rapor_sayisi"))
                .kalanGun(getInteger(rs, "kalan_gun"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getItems(Map<String, Object> result) {
        Object items = result.get("items");
        return items == null ? List.of() : (List<T>) items;
    }

    private Date toSqlDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    private Integer getInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private LocalDate getLocalDate(ResultSet rs, String columnName) throws SQLException {
        Date value = rs.getDate(columnName);
        return value == null ? null : value.toLocalDate();
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp value = rs.getTimestamp(columnName);
        return value == null ? null : value.toLocalDateTime();
    }
}
