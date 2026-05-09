package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.Odeme;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.OdemeRepository;
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
public class OdemeRepositoryImpl implements OdemeRepository {

    private final SimpleJdbcCall odemeListeleCall;
    private final SimpleJdbcCall odemeGetirCall;
    private final SimpleJdbcCall odemeEkleCall;
    private final SimpleJdbcCall odemeSilCall;

    public OdemeRepositoryImpl(DataSource dataSource) {
        RowMapper<Odeme> odemeRowMapper = this::mapRowToOdeme;

        this.odemeListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_odeme_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_hakedis_id", Types.INTEGER),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", odemeRowMapper);

        this.odemeGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_odeme_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_odeme_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", odemeRowMapper);

        this.odemeEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_odeme_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_hakedis_id", Types.INTEGER),
                        new SqlParameter("p_kaydeden_id", Types.INTEGER),
                        new SqlParameter("p_tutar", Types.DECIMAL),
                        new SqlParameter("p_tarih", Types.DATE),
                        new SqlParameter("p_yontem", Types.VARCHAR),
                        new SqlParameter("p_aciklama", Types.LONGVARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("odeme_id"));

        this.odemeSilCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_odeme_sil")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_odeme_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }

    @Override
    public PageResult<Odeme> listele(Integer firmaId, Integer hakedisId, int limit, int offset) {
        Map<String, Object> result = odemeListeleCall.execute(firmaId, hakedisId, limit, offset);
        List<Odeme> items = getItems(result);
        Integer total = (Integer) result.get("p_toplam");

        return new PageResult<>(items, total, limit, offset);
    }

    @Override
    public Odeme getir(Integer firmaId, Integer odemeId) {
        Map<String, Object> result = odemeGetirCall.execute(odemeId, firmaId);
        List<Odeme> items = getItems(result);

        if (items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, Integer kaydedenId, Odeme odeme) {
        Map<String, Object> result = odemeEkleCall.execute(
                firmaId,
                odeme.getHakedisId(),
                kaydedenId,
                odeme.getTutar(),
                toSqlDate(odeme.getOdemeTarihi()),
                odeme.getOdemeYontemi(),
                odeme.getAciklama()
        );

        List<Integer> items = getItems(result);

        if (items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }

    @Override
    public Integer sil(Integer firmaId, Integer odemeId, Integer kullaniciId) {
        Map<String, Object> result = odemeSilCall.execute(odemeId, firmaId, kullaniciId);
        List<Integer> items = getItems(result);
        return items.isEmpty() ? 0 : items.get(0);
    }

    // Ödeme result set'ini Java modeline çevirir.
    private Odeme mapRowToOdeme(ResultSet rs, int rowNum) throws SQLException {
        return Odeme.builder()
                .odemeId(rs.getInt("odeme_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .hakedisId(getInteger(rs, "hakedis_id"))
                .kaydedenId(getInteger(rs, "kaydeden_id"))
                .tutar(rs.getBigDecimal("tutar"))
                .odemeTarihi(getLocalDate(rs, "odeme_tarihi"))
                .odemeYontemi(rs.getString("odeme_yontemi"))
                .aciklama(rs.getString("aciklama"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .hakedisTutari(rs.getBigDecimal("hakedis_tutari"))
                .hakedisOnayDurumu(rs.getString("hakedis_onay_durumu"))
                .isEmriId(getInteger(rs, "is_emri_id"))
                .isEmriBaslik(rs.getString("is_emri_baslik"))
                .projeId(getInteger(rs, "proje_id"))
                .projeAd(rs.getString("proje_ad"))
                .taseronId(getInteger(rs, "taseron_id"))
                .taseronAd(rs.getString("taseron_ad"))
                .kaydeden(rs.getString("kaydeden"))
                .toplamOdenen(rs.getBigDecimal("toplam_odenen"))
                .kalanTutar(rs.getBigDecimal("kalan_tutar"))
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
