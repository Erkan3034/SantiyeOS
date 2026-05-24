package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.GenelOzetRaporu;
import com.santiyeos.api.model.ProjeMaliyetRaporu;
import com.santiyeos.api.model.TaseronPerformansRaporu;
import com.santiyeos.api.repository.RaporRepository;
import org.springframework.jdbc.core.RowMapper;
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
public class RaporRepositoryImpl implements RaporRepository {

    private final SimpleJdbcCall genelOzetCall;
    private final SimpleJdbcCall projeMaliyetCall;
    private final SimpleJdbcCall taseronPerformansCall;

    public RaporRepositoryImpl(DataSource dataSource) {
        this.genelOzetCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_genel_ozet_raporu")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", this::mapRowToGenelOzet);

        this.projeMaliyetCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_proje_maliyet_raporu")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR),
                        new SqlParameter("p_proje_id", Types.INTEGER)
                )
                .returningResultSet("items", this::mapRowToProjeMaliyet);

        this.taseronPerformansCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_taseron_performans_raporu")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR)
                )
                .returningResultSet("items", this::mapRowToTaseronPerformans);
    }

    @Override
    public GenelOzetRaporu genelOzet(Integer firmaId) {
        Map<String, Object> result = genelOzetCall.execute(firmaId);
        List<GenelOzetRaporu> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public ProjeMaliyetRaporu projeMaliyet(Integer firmaId, Integer kullaniciId, String rol, Integer projeId) {
        Map<String, Object> result = projeMaliyetCall.execute(firmaId, kullaniciId, rol, projeId);
        List<ProjeMaliyetRaporu> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public List<TaseronPerformansRaporu> taseronPerformans(Integer firmaId, Integer kullaniciId, String rol) {
        Map<String, Object> result = taseronPerformansCall.execute(firmaId, kullaniciId, rol);
        return getItems(result);
    }

    private GenelOzetRaporu mapRowToGenelOzet(ResultSet rs, int rowNum) throws SQLException {
        return GenelOzetRaporu.builder()
                .aktifProje(rs.getInt("aktif_proje"))
                .aktifIsEmri(rs.getInt("aktif_is_emri"))
                .gecikenIsEmri(rs.getInt("geciken_is_emri"))
                .bekleyenHakedis(rs.getInt("bekleyen_hakedis"))
                .toplamOdenmemis(rs.getBigDecimal("toplam_odenmemis"))
                .kritikStokSayisi(rs.getInt("kritik_stok_sayisi"))
                .build();
    }

    private ProjeMaliyetRaporu mapRowToProjeMaliyet(ResultSet rs, int rowNum) throws SQLException {
        return ProjeMaliyetRaporu.builder()
                .projeId(rs.getInt("proje_id"))
                .projeAd(rs.getString("proje_ad"))
                .toplamButce(rs.getBigDecimal("toplam_butce"))
                .durum(rs.getString("durum"))
                .isEmriSayisi(rs.getInt("is_emri_sayisi"))
                .taseronSayisi(rs.getInt("taseron_sayisi"))
                .toplamOnaylananHakedis(rs.getBigDecimal("toplam_onaylanan_hakedis"))
                .toplamOdeme(rs.getBigDecimal("toplam_odeme"))
                .kalanButce(rs.getBigDecimal("kalan_butce"))
                .butceKullanimYuzdesi(rs.getBigDecimal("butce_kullanim_yuzdesi"))
                .build();
    }

    private TaseronPerformansRaporu mapRowToTaseronPerformans(ResultSet rs, int rowNum) throws SQLException {
        return TaseronPerformansRaporu.builder()
                .taseronId(rs.getInt("taseron_id"))
                .ad(rs.getString("ad"))
                .uzmanlik(rs.getString("uzmanlik"))
                .performansSkoru(rs.getBigDecimal("performans_skoru"))
                .toplamIs(rs.getInt("toplam_is"))
                .tamamlanan(rs.getInt("tamamlanan"))
                .iptal(rs.getInt("iptal"))
                .geciken(rs.getInt("geciken"))
                .ortGecikmeGun(rs.getBigDecimal("ort_gecikme_gun"))
                .odenmemisBakiye(rs.getBigDecimal("odenmemis_bakiye"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getItems(Map<String, Object> result) {
        Object items = result.get("items");
        return items == null ? List.of() : (List<T>) items;
    }
}
