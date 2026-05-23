package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.Bildirim;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.BildirimRepository;
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
public class BildirimRepositoryImpl implements BildirimRepository {

    private final SimpleJdbcCall bildirimListeleCall;
    private final SimpleJdbcCall bildirimGetirCall;
    private final SimpleJdbcCall bildirimEkleCall;
    private final SimpleJdbcCall bildirimOkunduCall;
    private final SimpleJdbcCall bildirimTumunuOkunduCall;
    private final SimpleJdbcCall bildirimSilCall;

    public BildirimRepositoryImpl(DataSource dataSource) {
        RowMapper<Bildirim> rowMapper = this::mapRowToBildirim;

        this.bildirimListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_bildirim_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_sadece_okunmamis", Types.TINYINT),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);

        this.bildirimGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_bildirim_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_bildirim_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);

        this.bildirimEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_bildirim_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_olusturan_id", Types.INTEGER),
                        new SqlParameter("p_baslik", Types.VARCHAR),
                        new SqlParameter("p_mesaj", Types.LONGVARCHAR),
                        new SqlParameter("p_tip", Types.VARCHAR),
                        new SqlParameter("p_referans_tablo", Types.VARCHAR),
                        new SqlParameter("p_referans_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("bildirim_id"));

        this.bildirimOkunduCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_bildirim_okundu_isaretle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_bildirim_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.bildirimTumunuOkunduCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_bildirim_tumunu_okundu_isaretle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.bildirimSilCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_bildirim_sil")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_bildirim_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }

    @Override
    public PageResult<Bildirim> listele(Integer firmaId, Integer kullaniciId, Boolean sadeceOkunmamis, int limit, int offset) {
        Map<String, Object> result = bildirimListeleCall.execute(
                firmaId,
                kullaniciId,
                toTinyInt(sadeceOkunmamis),
                limit,
                offset
        );
        List<Bildirim> items = getItems(result);
        Integer total = (Integer) result.get("p_toplam");

        return new PageResult<>(items, total, limit, offset);
    }

    @Override
    public Bildirim getir(Integer firmaId, Integer kullaniciId, Integer bildirimId) {
        Map<String, Object> result = bildirimGetirCall.execute(bildirimId, firmaId, kullaniciId);
        List<Bildirim> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, Integer kullaniciId, Integer olusturanId, Bildirim bildirim) {
        Map<String, Object> result = bildirimEkleCall.execute(
                firmaId,
                kullaniciId,
                olusturanId,
                bildirim.getBaslik(),
                bildirim.getMesaj(),
                bildirim.getTip(),
                bildirim.getReferansTablo(),
                bildirim.getReferansId()
        );
        List<Integer> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer okunduIsaretle(Integer firmaId, Integer kullaniciId, Integer bildirimId) {
        Map<String, Object> result = bildirimOkunduCall.execute(bildirimId, firmaId, kullaniciId);
        List<Integer> items = getItems(result);

        return items.isEmpty() ? 0 : items.get(0);
    }

    @Override
    public Integer tumunuOkunduIsaretle(Integer firmaId, Integer kullaniciId) {
        Map<String, Object> result = bildirimTumunuOkunduCall.execute(firmaId, kullaniciId);
        List<Integer> items = getItems(result);

        return items.isEmpty() ? 0 : items.get(0);
    }

    @Override
    public Integer sil(Integer firmaId, Integer kullaniciId, Integer bildirimId) {
        Map<String, Object> result = bildirimSilCall.execute(bildirimId, firmaId, kullaniciId);
        List<Integer> items = getItems(result);

        return items.isEmpty() ? 0 : items.get(0);
    }

    private Bildirim mapRowToBildirim(ResultSet rs, int rowNum) throws SQLException {
        return Bildirim.builder()
                .bildirimId(rs.getInt("bildirim_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .kullaniciId(getInteger(rs, "kullanici_id"))
                .baslik(rs.getString("baslik"))
                .mesaj(rs.getString("mesaj"))
                .tip(rs.getString("tip"))
                .referansTablo(rs.getString("referans_tablo"))
                .referansId(getInteger(rs, "referans_id"))
                .okundu(rs.getBoolean("okundu"))
                .okunduTarihi(getLocalDateTime(rs, "okundu_tarihi"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getItems(Map<String, Object> result) {
        Object items = result.get("items");
        return items == null ? List.of() : (List<T>) items;
    }

    private Integer toTinyInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
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
