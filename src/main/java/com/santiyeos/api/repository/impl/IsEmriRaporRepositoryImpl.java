package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.IsEmriRapor;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.IsEmriRaporRepository;
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
public class IsEmriRaporRepositoryImpl implements IsEmriRaporRepository {

    private final SimpleJdbcCall raporListeleCall;
    private final SimpleJdbcCall raporGetirCall;
    private final SimpleJdbcCall raporEkleCall;
    private final SimpleJdbcCall raporGuncelleCall;
    private final SimpleJdbcCall raporSilCall;

    public IsEmriRaporRepositoryImpl(DataSource dataSource) {
        RowMapper<IsEmriRapor> rowMapper = this::mapRowToIsEmriRapor;

        this.raporListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_rapor_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_is_emri_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);

        this.raporGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_rapor_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_rapor_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);

        this.raporEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_rapor_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_is_emri_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_baslik", Types.VARCHAR),
                        new SqlParameter("p_icerik", Types.LONGVARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("rapor_id"));

        this.raporGuncelleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_rapor_guncelle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_rapor_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_baslik", Types.VARCHAR),
                        new SqlParameter("p_icerik", Types.LONGVARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.raporSilCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_rapor_sil")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_rapor_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }

    @Override
    public PageResult<IsEmriRapor> listele(Integer firmaId, Integer kullaniciId, Integer isEmriId, int limit, int offset) {
        Map<String, Object> result = raporListeleCall.execute(isEmriId, firmaId, kullaniciId, limit, offset);
        List<IsEmriRapor> items = getItems(result);
        Integer total = (Integer) result.get("p_toplam");

        return new PageResult<>(items, total, limit, offset);
    }

    @Override
    public IsEmriRapor getir(Integer firmaId, Integer kullaniciId, Integer raporId) {
        Map<String, Object> result = raporGetirCall.execute(raporId, firmaId, kullaniciId);
        List<IsEmriRapor> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, Integer kullaniciId, Integer isEmriId, IsEmriRapor isEmriRapor) {
        Map<String, Object> result = raporEkleCall.execute(
                firmaId,
                isEmriId,
                kullaniciId,
                isEmriRapor.getBaslik(),
                isEmriRapor.getIcerik()
        );
        List<Integer> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer guncelle(Integer firmaId, Integer kullaniciId, Integer raporId, IsEmriRapor isEmriRapor) {
        Map<String, Object> result = raporGuncelleCall.execute(
                raporId,
                firmaId,
                kullaniciId,
                isEmriRapor.getBaslik(),
                isEmriRapor.getIcerik()
        );
        List<Integer> items = getItems(result);

        return items.isEmpty() ? 0 : items.get(0);
    }

    @Override
    public Integer sil(Integer firmaId, Integer kullaniciId, Integer raporId) {
        Map<String, Object> result = raporSilCall.execute(raporId, firmaId, kullaniciId);
        List<Integer> items = getItems(result);

        return items.isEmpty() ? 0 : items.get(0);
    }

    private IsEmriRapor mapRowToIsEmriRapor(ResultSet rs, int rowNum) throws SQLException {
        return IsEmriRapor.builder()
                .raporId(rs.getInt("rapor_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .isEmriId(getInteger(rs, "is_emri_id"))
                .kullaniciId(getInteger(rs, "kullanici_id"))
                .baslik(rs.getString("baslik"))
                .icerik(rs.getString("icerik"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .yazan(rs.getString("yazan"))
                .rol(rs.getString("rol"))
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
