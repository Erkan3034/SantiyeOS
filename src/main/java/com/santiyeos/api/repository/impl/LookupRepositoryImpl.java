package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.LookupItem;
import com.santiyeos.api.repository.LookupRepository;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Repository
public class LookupRepositoryImpl implements LookupRepository {

    private final SimpleJdbcCall projelerCall;
    private final SimpleJdbcCall taseronlarCall;
    private final SimpleJdbcCall kullanicilarCall;
    private final SimpleJdbcCall malzemelerCall;
    private final SimpleJdbcCall abonelikPlanlariCall;

    public LookupRepositoryImpl(DataSource dataSource) {
        this.projelerCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_lookup_projeler")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR)
                )
                .returningResultSet("items", this::mapRowToProje);

        this.taseronlarCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_lookup_taseronlar")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", this::mapRowToTaseron);

        this.kullanicilarCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_lookup_kullanicilar")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR)
                )
                .returningResultSet("items", this::mapRowToKullanici);

        this.malzemelerCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_lookup_malzemeler")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", this::mapRowToMalzeme);

        this.abonelikPlanlariCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_lookup_abonelik_planlari")
                .withoutProcedureColumnMetaDataAccess()
                .returningResultSet("items", this::mapRowToAbonelikPlan);
    }

    @Override
    public List<LookupItem.Proje> projeler(Integer firmaId, Integer kullaniciId, String rol) {
        Map<String, Object> result = projelerCall.execute(firmaId, kullaniciId, rol);
        return getItems(result);
    }

    @Override
    public List<LookupItem.Taseron> taseronlar(Integer firmaId) {
        Map<String, Object> result = taseronlarCall.execute(firmaId);
        return getItems(result);
    }

    @Override
    public List<LookupItem.Kullanici> kullanicilar(Integer firmaId, String rol) {
        Map<String, Object> result = kullanicilarCall.execute(firmaId, rol);
        return getItems(result);
    }

    @Override
    public List<LookupItem.Malzeme> malzemeler(Integer firmaId) {
        Map<String, Object> result = malzemelerCall.execute(firmaId);
        return getItems(result);
    }

    @Override
    public List<LookupItem.AbonelikPlan> abonelikPlanlari() {
        Map<String, Object> result = abonelikPlanlariCall.execute();
        return getItems(result);
    }

    private LookupItem.Proje mapRowToProje(ResultSet rs, int rowNum) throws SQLException {
        return LookupItem.Proje.builder()
                .projeId(rs.getInt("proje_id"))
                .ad(rs.getString("ad"))
                .durum(rs.getString("durum"))
                .build();
    }

    private LookupItem.Taseron mapRowToTaseron(ResultSet rs, int rowNum) throws SQLException {
        return LookupItem.Taseron.builder()
                .taseronId(rs.getInt("taseron_id"))
                .ad(rs.getString("ad"))
                .uzmanlik(rs.getString("uzmanlik"))
                .build();
    }

    private LookupItem.Kullanici mapRowToKullanici(ResultSet rs, int rowNum) throws SQLException {
        return LookupItem.Kullanici.builder()
                .kullaniciId(rs.getInt("kullanici_id"))
                .taseronId(getInteger(rs, "taseron_id"))
                .ad(rs.getString("ad"))
                .soyad(rs.getString("soyad"))
                .email(rs.getString("email"))
                .rol(rs.getString("rol"))
                .taseronAd(rs.getString("taseron_ad"))
                .build();
    }

    private LookupItem.Malzeme mapRowToMalzeme(ResultSet rs, int rowNum) throws SQLException {
        return LookupItem.Malzeme.builder()
                .malzemeId(rs.getInt("malzeme_id"))
                .kategoriId(getInteger(rs, "kategori_id"))
                .ad(rs.getString("ad"))
                .birim(rs.getString("birim"))
                .kategoriAd(rs.getString("kategori_ad"))
                .stokMiktari(rs.getBigDecimal("stok_miktari"))
                .build();
    }

    private LookupItem.AbonelikPlan mapRowToAbonelikPlan(ResultSet rs, int rowNum) throws SQLException {
        return LookupItem.AbonelikPlan.builder()
                .planId(rs.getInt("plan_id"))
                .ad(rs.getString("ad"))
                .maxProje(rs.getInt("max_proje"))
                .maxKullanici(rs.getInt("max_kullanici"))
                .maxTaseron(rs.getInt("max_taseron"))
                .aylikUcret(rs.getBigDecimal("aylik_ucret"))
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
}
