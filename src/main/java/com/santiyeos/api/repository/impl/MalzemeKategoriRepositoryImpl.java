package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.MalzemeKategori;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.MalzemeKategoriRepository;
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
public class MalzemeKategoriRepositoryImpl implements MalzemeKategoriRepository {

    private final SimpleJdbcCall kategoriListeleCall;
    private final SimpleJdbcCall kategoriGetirCall;
    private final SimpleJdbcCall kategoriEkleCall;
    private final SimpleJdbcCall kategoriGuncelleCall;
    private final SimpleJdbcCall kategoriSilCall;

    public MalzemeKategoriRepositoryImpl(DataSource dataSource) {
        RowMapper<MalzemeKategori> rowMapper = this::mapRowToKategori;

        this.kategoriListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_malzeme_kategori_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);

        this.kategoriGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_malzeme_kategori_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_kategori_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);

        this.kategoriEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_malzeme_kategori_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR),
                        new SqlParameter("p_ad", Types.VARCHAR),
                        new SqlParameter("p_aciklama", Types.VARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("kategori_id"));

        this.kategoriGuncelleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_malzeme_kategori_guncelle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_kategori_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR),
                        new SqlParameter("p_ad", Types.VARCHAR),
                        new SqlParameter("p_aciklama", Types.VARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.kategoriSilCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_malzeme_kategori_sil")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_kategori_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }

    @Override
    public PageResult<MalzemeKategori> listele(Integer firmaId, int limit, int offset) {
        Map<String, Object> result = kategoriListeleCall.execute(firmaId, limit, offset);
        return new PageResult<>(getItems(result), (Integer) result.get("p_toplam"), limit, offset);
    }

    @Override
    public MalzemeKategori getir(Integer firmaId, Integer kategoriId) {
        Map<String, Object> result = kategoriGetirCall.execute(kategoriId, firmaId);
        List<MalzemeKategori> items = getItems(result);
        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, String rol, MalzemeKategori kategori) {
        Map<String, Object> result = kategoriEkleCall.execute(firmaId, rol, kategori.getAd(), kategori.getAciklama());
        List<Integer> items = getItems(result);
        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer guncelle(Integer firmaId, Integer kategoriId, String rol, MalzemeKategori kategori) {
        Map<String, Object> result = kategoriGuncelleCall.execute(
                kategoriId,
                firmaId,
                rol,
                kategori.getAd(),
                kategori.getAciklama()
        );
        List<Integer> items = getItems(result);
        return items.isEmpty() ? 0 : items.get(0);
    }

    @Override
    public Integer sil(Integer firmaId, Integer kategoriId, Integer kullaniciId, String rol) {
        Map<String, Object> result = kategoriSilCall.execute(kategoriId, firmaId, kullaniciId, rol);
        List<Integer> items = getItems(result);
        return items.isEmpty() ? 0 : items.get(0);
    }

    private MalzemeKategori mapRowToKategori(ResultSet rs, int rowNum) throws SQLException {
        return MalzemeKategori.builder()
                .kategoriId(rs.getInt("kategori_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .ad(rs.getString("ad"))
                .aciklama(rs.getString("aciklama"))
                .aktif(rs.getBoolean("aktif"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .malzemeSayisi(getInteger(rs, "malzeme_sayisi"))
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
