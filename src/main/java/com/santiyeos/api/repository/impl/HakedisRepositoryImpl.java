package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.Hakedis;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.HakedisRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class HakedisRepositoryImpl implements HakedisRepository {

    private final SimpleJdbcCall hakedisListeleCall;
    private final SimpleJdbcCall hakedisGetirCall;
    private final SimpleJdbcCall hakedisEkleCall;
    private final SimpleJdbcCall hakedisOnaylaCall;
    private final SimpleJdbcCall hakedisReddetCall;
    private final SimpleJdbcCall hakedisSilCall;

    public HakedisRepositoryImpl(DataSource dataSource) {
        RowMapper<Hakedis> hakedisRowMapper = this::mapRowToHakedis;

        this.hakedisListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_hakedis_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_taseron_id", Types.INTEGER),
                        new SqlParameter("p_onay_durumu", Types.VARCHAR),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", hakedisRowMapper);

        this.hakedisGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_hakedis_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_hakedis_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_taseron_id", Types.INTEGER)
                )
                .returningResultSet("items", hakedisRowMapper);

        this.hakedisEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_hakedis_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_is_emri_id", Types.INTEGER),
                        new SqlParameter("p_talep_eden_id", Types.INTEGER),
                        new SqlParameter("p_tutar", Types.DECIMAL),
                        new SqlParameter("p_aciklama", Types.LONGVARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("hakedis_id"));

        this.hakedisOnaylaCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_hakedis_onayla")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_hakedis_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_onaylayan_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.hakedisReddetCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_hakedis_reddet")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_hakedis_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_onaylayan_id", Types.INTEGER),
                        new SqlParameter("p_red_gerekce", Types.LONGVARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.hakedisSilCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_hakedis_sil")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_hakedis_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }

    @Override
    public PageResult<Hakedis> listele(Integer firmaId, Integer taseronId, String onayDurumu, int limit, int offset) {
        Map<String, Object> result = hakedisListeleCall.execute(firmaId, taseronId, onayDurumu, limit, offset);
        List<Hakedis> items = getItems(result);
        Integer total = (Integer) result.get("p_toplam");

        return new PageResult<>(items, total, limit, offset);
    }

    @Override
    public Hakedis getir(Integer firmaId,Integer taseronId, Integer hakedisId) {
        Map<String, Object> result = hakedisGetirCall.execute(hakedisId, firmaId,taseronId);
        List<Hakedis> items = getItems(result);

        if (items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, Integer talepEdenId, Hakedis hakedis) {
        Map<String, Object> result = hakedisEkleCall.execute(
                firmaId,
                hakedis.getIsEmriId(),
                talepEdenId,
                hakedis.getTutar(),
                hakedis.getAciklama()
        );

        List<Integer> items = getItems(result);

        if (items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }

    @Override
    public Integer onayla(Integer firmaId, Integer hakedisId, Integer onaylayanId) {
        Map<String, Object> result = hakedisOnaylaCall.execute(hakedisId, firmaId, onaylayanId);
        List<Integer> items = getItems(result);
        return items.isEmpty() ? 0 : items.get(0);
    }

    @Override
    public Integer reddet(Integer firmaId, Integer hakedisId, Integer onaylayanId, String redGerekce) {
        Map<String, Object> result = hakedisReddetCall.execute(hakedisId, firmaId, onaylayanId, redGerekce);
        List<Integer> items = getItems(result);
        return items.isEmpty() ? 0 : items.get(0);
    }

    @Override
    public Integer sil(Integer firmaId, Integer hakedisId, Integer kullaniciId) {
        Map<String, Object> result = hakedisSilCall.execute(hakedisId, firmaId, kullaniciId);
        List<Integer> items = getItems(result);
        return items.isEmpty() ? 0 : items.get(0);
    }

    // Stored procedure result set'ini Hakedis modeline map ediyoruz.
    private Hakedis mapRowToHakedis(ResultSet rs, int rowNum) throws SQLException {
        return Hakedis.builder()
                .hakedisId(rs.getInt("hakedis_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .isEmriId(getInteger(rs, "is_emri_id"))
                .talepEdenId(getInteger(rs, "talep_eden_id"))
                .onaylayanId(getInteger(rs, "onaylayan_id"))
                .tutar(rs.getBigDecimal("tutar"))
                .onayDurumu(rs.getString("onay_durumu"))
                .onayTarihi(getLocalDateTime(rs, "onay_tarihi"))
                .aciklama(rs.getString("aciklama"))
                .redGerekce(rs.getString("red_gerekce"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .updatedAt(getLocalDateTime(rs, "updated_at"))
                .projeId(getInteger(rs, "proje_id"))
                .projeAd(rs.getString("proje_ad"))
                .taseronId(getInteger(rs, "taseron_id"))
                .taseronAd(rs.getString("taseron_ad"))
                .isEmriBaslik(rs.getString("is_emri_baslik"))
                .tamamlanmaTarihi(getLocalDateTime(rs, "tamamlanma_tarihi"))
                .talepEden(rs.getString("talep_eden"))
                .onaylayan(rs.getString("onaylayan"))
                .odenenTutar(rs.getBigDecimal("odenen_tutar"))
                .kalanTutar(rs.getBigDecimal("kalan_tutar"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getItems(Map<String, Object> result) {
        Object items = result.get("items");
        return items == null ? List.of() : (List<T>) items;
    }

    private Integer getInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp value = rs.getTimestamp(columnName);
        return value == null ? null : value.toLocalDateTime();
    }
}
