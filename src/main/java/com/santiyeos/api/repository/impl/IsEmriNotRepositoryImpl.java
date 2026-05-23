package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.IsEmriNot;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.IsEmriNotRepository;
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
public class IsEmriNotRepositoryImpl implements IsEmriNotRepository {

    private final SimpleJdbcCall notListeleCall;
    private final SimpleJdbcCall notGetirCall;
    private final SimpleJdbcCall notEkleCall;
    private final SimpleJdbcCall notGuncelleCall;
    private final SimpleJdbcCall notSilCall;

    public IsEmriNotRepositoryImpl(DataSource dataSource) {
        RowMapper<IsEmriNot> rowMapper = this::mapRowToIsEmriNot;

        this.notListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_not_listele")
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

        this.notGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_not_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_not_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);

        this.notEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_not_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_is_emri_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_icerik", Types.LONGVARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("not_id"));

        this.notGuncelleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_not_guncelle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_not_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_icerik", Types.LONGVARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.notSilCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_is_emri_not_sil")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_not_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }

    @Override
    public PageResult<IsEmriNot> listele(Integer firmaId, Integer kullaniciId, Integer isEmriId, int limit, int offset) {
        Map<String, Object> result = notListeleCall.execute(isEmriId, firmaId, kullaniciId, limit, offset);
        List<IsEmriNot> items = getItems(result);
        Integer total = (Integer) result.get("p_toplam");

        return new PageResult<>(items, total, limit, offset);
    }

    @Override
    public IsEmriNot getir(Integer firmaId, Integer kullaniciId, Integer notId) {
        Map<String, Object> result = notGetirCall.execute(notId, firmaId, kullaniciId);
        List<IsEmriNot> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, Integer kullaniciId, Integer isEmriId, IsEmriNot isEmriNot) {
        Map<String, Object> result = notEkleCall.execute(
                firmaId,
                isEmriId,
                kullaniciId,
                isEmriNot.getIcerik()
        );
        List<Integer> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer guncelle(Integer firmaId, Integer kullaniciId, Integer notId, IsEmriNot isEmriNot) {
        Map<String, Object> result = notGuncelleCall.execute(notId, firmaId, kullaniciId, isEmriNot.getIcerik());
        List<Integer> items = getItems(result);

        return items.isEmpty() ? 0 : items.get(0);
    }

    @Override
    public Integer sil(Integer firmaId, Integer kullaniciId, Integer notId) {
        Map<String, Object> result = notSilCall.execute(notId, firmaId, kullaniciId);
        List<Integer> items = getItems(result);

        return items.isEmpty() ? 0 : items.get(0);
    }

    private IsEmriNot mapRowToIsEmriNot(ResultSet rs, int rowNum) throws SQLException {
        return IsEmriNot.builder()
                .notId(rs.getInt("not_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .isEmriId(getInteger(rs, "is_emri_id"))
                .kullaniciId(getInteger(rs, "kullanici_id"))
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
