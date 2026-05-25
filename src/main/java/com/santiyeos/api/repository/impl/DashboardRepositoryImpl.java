package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.DashboardOzet;
import com.santiyeos.api.repository.DashboardRepository;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class DashboardRepositoryImpl implements DashboardRepository {

    private final SimpleJdbcCall dashboardOzetCall;

    public DashboardRepositoryImpl(DataSource dataSource) {
        this.dashboardOzetCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_dashboard_ozet")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("genelOzet", this::mapRowToGenelOzet)
                .returningResultSet("isEmriDurumlari", this::mapRowToDurumDagilimi)
                .returningResultSet("hakedisDurumlari", this::mapRowToDurumDagilimi)
                .returningResultSet("finansOzet", this::mapRowToFinansOzet)
                .returningResultSet("kritikStoklar", this::mapRowToKritikStok)
                .returningResultSet("yaklasanProjeler", this::mapRowToYaklasanProje);
    }

    @Override
    public DashboardOzet ozet(Integer firmaId) {
        Map<String, Object> result = dashboardOzetCall.execute(firmaId);

        List<DashboardOzet.GenelOzet> genelOzet = getItems(result, "genelOzet");
        List<DashboardOzet.FinansOzet> finansOzet = getItems(result, "finansOzet");

        return DashboardOzet.builder()
                .genelOzet(genelOzet.isEmpty() ? null : genelOzet.get(0))
                .isEmriDurumlari(getItems(result, "isEmriDurumlari"))
                .hakedisDurumlari(getItems(result, "hakedisDurumlari"))
                .finansOzet(finansOzet.isEmpty() ? null : finansOzet.get(0))
                .kritikStoklar(getItems(result, "kritikStoklar"))
                .yaklasanProjeler(getItems(result, "yaklasanProjeler"))
                .build();
    }

    private DashboardOzet.GenelOzet mapRowToGenelOzet(ResultSet rs, int rowNum) throws SQLException {
        return DashboardOzet.GenelOzet.builder()
                .aktifProje(rs.getInt("aktif_proje"))
                .aktifIsEmri(rs.getInt("aktif_is_emri"))
                .gecikenIsEmri(rs.getInt("geciken_is_emri"))
                .bekleyenHakedis(rs.getInt("bekleyen_hakedis"))
                .kritikStokSayisi(rs.getInt("kritik_stok_sayisi"))
                .build();
    }

    private DashboardOzet.DurumDagilimi mapRowToDurumDagilimi(ResultSet rs, int rowNum) throws SQLException {
        return DashboardOzet.DurumDagilimi.builder()
                .durum(rs.getString("durum"))
                .toplam(rs.getInt("toplam"))
                .build();
    }

    private DashboardOzet.FinansOzet mapRowToFinansOzet(ResultSet rs, int rowNum) throws SQLException {
        return DashboardOzet.FinansOzet.builder()
                .toplamOnaylananHakedis(rs.getBigDecimal("toplam_onaylanan_hakedis"))
                .toplamOdeme(rs.getBigDecimal("toplam_odeme"))
                .toplamOdenmemis(rs.getBigDecimal("toplam_odenmemis"))
                .bekleyenHakedis(rs.getInt("bekleyen_hakedis"))
                .onaylananHakedis(rs.getInt("onaylanan_hakedis"))
                .build();
    }

    private DashboardOzet.KritikStok mapRowToKritikStok(ResultSet rs, int rowNum) throws SQLException {
        return DashboardOzet.KritikStok.builder()
                .malzemeId(rs.getInt("malzeme_id"))
                .ad(rs.getString("ad"))
                .kategoriAd(rs.getString("kategori_ad"))
                .birim(rs.getString("birim"))
                .stokMiktari(rs.getBigDecimal("stok_miktari"))
                .minStok(rs.getBigDecimal("min_stok"))
                .build();
    }

    private DashboardOzet.YaklasanProje mapRowToYaklasanProje(ResultSet rs, int rowNum) throws SQLException {
        return DashboardOzet.YaklasanProje.builder()
                .projeId(rs.getInt("proje_id"))
                .ad(rs.getString("ad"))
                .bitisTarihi(getLocalDate(rs, "bitis_tarihi"))
                .durum(rs.getString("durum"))
                .kalanGun(rs.getInt("kalan_gun"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getItems(Map<String, Object> result, String key) {
        Object items = result.get(key);
        return items == null ? List.of() : (List<T>) items;
    }

    private LocalDate getLocalDate(ResultSet rs, String columnName) throws SQLException {
        Date value = rs.getDate(columnName);
        return value == null ? null : value.toLocalDate();
    }
}
