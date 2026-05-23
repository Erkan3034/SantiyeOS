package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.StokHareket;
import com.santiyeos.api.repository.StokHareketRepository;
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
public class StokHareketRepositoryImpl implements StokHareketRepository {

    private final SimpleJdbcCall stokHareketListeleCall;
    private final SimpleJdbcCall stokHareketGetirCall;
    private final SimpleJdbcCall stokHareketEkleCall;

    public StokHareketRepositoryImpl(DataSource dataSource) {
        RowMapper<StokHareket> rowMapper = this::mapRowToStokHareket;

        this.stokHareketListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_stok_hareket_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_malzeme_id", Types.INTEGER),
                        new SqlParameter("p_proje_id", Types.INTEGER),
                        new SqlParameter("p_is_emri_id", Types.INTEGER),
                        new SqlParameter("p_hareket_tipi", Types.VARCHAR),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);

        this.stokHareketGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_stok_hareket_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_hareket_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);

        this.stokHareketEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_stok_hareket_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_malzeme_id", Types.INTEGER),
                        new SqlParameter("p_proje_id", Types.INTEGER),
                        new SqlParameter("p_is_emri_id", Types.INTEGER),
                        new SqlParameter("p_kaydeden_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR),
                        new SqlParameter("p_hareket_tipi", Types.VARCHAR),
                        new SqlParameter("p_miktar", Types.DECIMAL),
                        new SqlParameter("p_birim_fiyat", Types.DECIMAL),
                        new SqlParameter("p_aciklama", Types.LONGVARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("hareket_id"));
    }

    @Override
    public PageResult<StokHareket> listele(
            Integer firmaId,
            Integer malzemeId,
            Integer projeId,
            Integer isEmriId,
            String hareketTipi,
            int limit,
            int offset
    ) {
        Map<String, Object> result = stokHareketListeleCall.execute(
                firmaId,
                malzemeId,
                projeId,
                isEmriId,
                hareketTipi,
                limit,
                offset
        );
        return new PageResult<>(getItems(result), (Integer) result.get("p_toplam"), limit, offset);
    }

    @Override
    public StokHareket getir(Integer firmaId, Integer hareketId) {
        Map<String, Object> result = stokHareketGetirCall.execute(hareketId, firmaId);
        List<StokHareket> items = getItems(result);
        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, Integer kaydedenId, String rol, StokHareket hareket) {
        Map<String, Object> result = stokHareketEkleCall.execute(
                firmaId,
                hareket.getMalzemeId(),
                hareket.getProjeId(),
                hareket.getIsEmriId(),
                kaydedenId,
                rol,
                hareket.getHareketTipi(),
                hareket.getMiktar(),
                hareket.getBirimFiyat(),
                hareket.getAciklama()
        );
        List<Integer> items = getItems(result);
        return items.isEmpty() ? null : items.get(0);
    }

    private StokHareket mapRowToStokHareket(ResultSet rs, int rowNum) throws SQLException {
        return StokHareket.builder()
                .hareketId(rs.getInt("hareket_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .malzemeId(getInteger(rs, "malzeme_id"))
                .projeId(getInteger(rs, "proje_id"))
                .isEmriId(getInteger(rs, "is_emri_id"))
                .kaydedenId(getInteger(rs, "kaydeden_id"))
                .hareketTipi(rs.getString("hareket_tipi"))
                .miktar(rs.getBigDecimal("miktar"))
                .birimFiyat(rs.getBigDecimal("birim_fiyat"))
                .aciklama(rs.getString("aciklama"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .malzemeAd(rs.getString("malzeme_ad"))
                .birim(rs.getString("birim"))
                .projeAd(rs.getString("proje_ad"))
                .isEmriBaslik(rs.getString("is_emri_baslik"))
                .kaydeden(rs.getString("kaydeden"))
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
