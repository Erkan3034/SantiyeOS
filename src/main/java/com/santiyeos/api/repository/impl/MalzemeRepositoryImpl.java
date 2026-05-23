package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.Malzeme;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.MalzemeRepository;
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
public class MalzemeRepositoryImpl implements MalzemeRepository {

    private final SimpleJdbcCall malzemeListeleCall;
    private final SimpleJdbcCall malzemeGetirCall;
    private final SimpleJdbcCall malzemeEkleCall;
    private final SimpleJdbcCall malzemeGuncelleCall;
    private final SimpleJdbcCall malzemeSilCall;

    public MalzemeRepositoryImpl(DataSource dataSource) {
        RowMapper<Malzeme> rowMapper = this::mapRowToMalzeme;

        this.malzemeListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_malzeme_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kategori_id", Types.INTEGER),
                        new SqlParameter("p_kritik_stok", Types.TINYINT),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);

        this.malzemeGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_malzeme_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_malzeme_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);

        this.malzemeEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_malzeme_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR),
                        new SqlParameter("p_kategori_id", Types.INTEGER),
                        new SqlParameter("p_ad", Types.VARCHAR),
                        new SqlParameter("p_birim", Types.VARCHAR),
                        new SqlParameter("p_birim_fiyat", Types.DECIMAL),
                        new SqlParameter("p_min_stok", Types.DECIMAL)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("malzeme_id"));

        this.malzemeGuncelleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_malzeme_guncelle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_malzeme_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR),
                        new SqlParameter("p_kategori_id", Types.INTEGER),
                        new SqlParameter("p_ad", Types.VARCHAR),
                        new SqlParameter("p_birim", Types.VARCHAR),
                        new SqlParameter("p_birim_fiyat", Types.DECIMAL),
                        new SqlParameter("p_min_stok", Types.DECIMAL)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.malzemeSilCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_malzeme_sil")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_malzeme_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }

    @Override
    public PageResult<Malzeme> listele(Integer firmaId, Integer kategoriId, Boolean kritikStok, int limit, int offset) {
        Map<String, Object> result = malzemeListeleCall.execute(
                firmaId,
                kategoriId,
                toTinyInt(kritikStok),
                limit,
                offset
        );
        return new PageResult<>(getItems(result), (Integer) result.get("p_toplam"), limit, offset);
    }

    @Override
    public Malzeme getir(Integer firmaId, Integer malzemeId) {
        Map<String, Object> result = malzemeGetirCall.execute(malzemeId, firmaId);
        List<Malzeme> items = getItems(result);
        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, String rol, Malzeme malzeme) {
        Map<String, Object> result = malzemeEkleCall.execute(
                firmaId,
                rol,
                malzeme.getKategoriId(),
                malzeme.getAd(),
                malzeme.getBirim(),
                malzeme.getBirimFiyat(),
                malzeme.getMinStok()
        );
        List<Integer> items = getItems(result);
        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer guncelle(Integer firmaId, Integer malzemeId, String rol, Malzeme malzeme) {
        Map<String, Object> result = malzemeGuncelleCall.execute(
                malzemeId,
                firmaId,
                rol,
                malzeme.getKategoriId(),
                malzeme.getAd(),
                malzeme.getBirim(),
                malzeme.getBirimFiyat(),
                malzeme.getMinStok()
        );
        List<Integer> items = getItems(result);
        return items.isEmpty() ? 0 : items.get(0);
    }

    @Override
    public Integer sil(Integer firmaId, Integer malzemeId, Integer kullaniciId, String rol) {
        Map<String, Object> result = malzemeSilCall.execute(malzemeId, firmaId, kullaniciId, rol);
        List<Integer> items = getItems(result);
        return items.isEmpty() ? 0 : items.get(0);
    }

    private Malzeme mapRowToMalzeme(ResultSet rs, int rowNum) throws SQLException {
        return Malzeme.builder()
                .malzemeId(rs.getInt("malzeme_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .kategoriId(getInteger(rs, "kategori_id"))
                .ad(rs.getString("ad"))
                .birim(rs.getString("birim"))
                .birimFiyat(rs.getBigDecimal("birim_fiyat"))
                .stokMiktari(rs.getBigDecimal("stok_miktari"))
                .minStok(rs.getBigDecimal("min_stok"))
                .kritikStok(rs.getBoolean("kritik_stok"))
                .aktif(rs.getBoolean("aktif"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .updatedAt(getLocalDateTime(rs, "updated_at"))
                .kategoriAd(rs.getString("kategori_ad"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getItems(Map<String, Object> result) {
        Object items = result.get("items");
        return items == null ? List.of() : (List<T>) items;
    }

    private Integer toTinyInt(Boolean value) {
        if (value == null) {
            return null;
        }

        return value ? 1 : 0;
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
