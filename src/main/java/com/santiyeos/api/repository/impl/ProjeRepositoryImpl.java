package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.Proje;
import com.santiyeos.api.repository.ProjeRepository;
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
public class ProjeRepositoryImpl implements ProjeRepository {

    // Repository sadece stored procedure cagirir; is kurali Service katmaninda kalir.
    private final SimpleJdbcCall projeListeleCall;
    private final SimpleJdbcCall projeGetirCall;
    private final SimpleJdbcCall projeEkleCall;
    private final SimpleJdbcCall projeGuncelleCall;
    private final SimpleJdbcCall projeSilCall;

    public ProjeRepositoryImpl(DataSource dataSource) {
        RowMapper<Proje> projeRowMapper = this::mapRowToProje;

        this.projeListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_proje_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_durum", Types.VARCHAR),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", projeRowMapper);

        this.projeGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_proje_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_proje_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", projeRowMapper);

        this.projeEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_proje_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_ad", Types.VARCHAR),
                        new SqlParameter("p_aciklama", Types.LONGVARCHAR),
                        new SqlParameter("p_konum", Types.VARCHAR),
                        new SqlParameter("p_butce", Types.DECIMAL),
                        new SqlParameter("p_baslangic", Types.DATE),
                        new SqlParameter("p_bitis", Types.DATE),
                        new SqlParameter("p_uyari_yuzde", Types.TINYINT)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("proje_id"));

        this.projeGuncelleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_proje_guncelle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_proje_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_ad", Types.VARCHAR),
                        new SqlParameter("p_aciklama", Types.LONGVARCHAR),
                        new SqlParameter("p_konum", Types.VARCHAR),
                        new SqlParameter("p_butce", Types.DECIMAL),
                        new SqlParameter("p_baslangic", Types.DATE),
                        new SqlParameter("p_bitis", Types.DATE),
                        new SqlParameter("p_durum", Types.VARCHAR),
                        new SqlParameter("p_uyari_yuzde", Types.TINYINT)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        // Silme fiziksel delete degil; DB tarafinda durum = IPTAL yapilir.
        this.projeSilCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_proje_sil")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_proje_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }

    @Override
    public PageResult<Proje> listele(Integer firmaId, String durum, int limit, int offset) {
        Map<String, Object> result = projeListeleCall.execute(firmaId, durum, limit, offset);

        List<Proje> items = getItems(result);
        Integer total = (Integer) result.get("p_toplam");

        return new PageResult<>(items, total, limit, offset);
    }

    @Override
    public Proje getir(Integer firmaId, Integer projeId) {
        Map<String, Object> result = projeGetirCall.execute(projeId, firmaId);

        List<Proje> items = getItems(result);

        if (items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, Proje proje) {
        Map<String, Object> result = projeEkleCall.execute(
                firmaId,
                proje.getAd(),
                proje.getAciklama(),
                proje.getKonum(),
                proje.getButce(),
                toSqlDate(proje.getBaslangicTarihi()),
                toSqlDate(proje.getBitisTarihi()),
                proje.getButceUyariYuzde()
        );

        List<Integer> items = getItems(result);

        if (items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }

    @Override
    public Integer guncelle(Integer firmaId, Integer projeId, Proje proje) {
        Map<String, Object> result = projeGuncelleCall.execute(
                projeId,
                firmaId,
                proje.getAd(),
                proje.getAciklama(),
                proje.getKonum(),
                proje.getButce(),
                toSqlDate(proje.getBaslangicTarihi()),
                toSqlDate(proje.getBitisTarihi()),
                proje.getDurum(),
                proje.getButceUyariYuzde()
        );

        List<Integer> items = getItems(result);

        if (items.isEmpty()) {
            return 0;
        }

        return items.get(0);
    }

    @Override
    public Integer sil(Integer firmaId, Integer projeId, Integer kullaniciId) {
        Map<String, Object> result = projeSilCall.execute(projeId, firmaId, kullaniciId);

        List<Integer> items = getItems(result);

        if (items.isEmpty()) {
            return 0;
        }

        return items.get(0);
    }

    // DB snake_case kolonlarini Java camelCase field'lara cevirir.
    private Proje mapRowToProje(ResultSet rs, int rowNum) throws SQLException {
        return Proje.builder()
                .projeId(rs.getInt("proje_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .ad(rs.getString("ad"))
                .aciklama(rs.getString("aciklama"))
                .konum(rs.getString("konum"))
                .butce(rs.getBigDecimal("butce"))
                .baslangicTarihi(getLocalDate(rs, "baslangic_tarihi"))
                .bitisTarihi(getLocalDate(rs, "bitis_tarihi"))
                .durum(rs.getString("durum"))
                .butceUyariYuzde(getInteger(rs, "butce_uyari_yuzde"))
                .toplamIsEmri(getInteger(rs, "toplam_is_emri"))
                .tamamlananIsEmri(getInteger(rs, "tamamlanan_is_emri"))
                .taseronSayisi(getInteger(rs, "taseron_sayisi"))
                .toplamOdeme(rs.getBigDecimal("toplam_odeme"))
                .butceKullanimYuzdesi(rs.getBigDecimal("butce_kullanim_yuzdesi"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .updatedAt(getLocalDateTime(rs, "updated_at"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getItems(Map<String, Object> result) {
        Object items = result.get("items");

        if (items == null) {
            return List.of();
        }

        return (List<T>) items;
    }

    private Date toSqlDate(LocalDate value) {
        if (value == null) {
            return null;
        }

        return Date.valueOf(value);
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
