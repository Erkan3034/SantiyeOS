package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.Abonelik;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.AbonelikRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class AbonelikRepositoryImpl implements AbonelikRepository {

    private final SimpleJdbcCall abonelikAktifGetirCall;
    private final SimpleJdbcCall abonelikListeleCall;
    private final SimpleJdbcCall abonelikBaslatCall;
    private final SimpleJdbcCall abonelikIptalCall;

    public AbonelikRepositoryImpl(DataSource dataSource) {
        RowMapper<Abonelik> abonelikRowMapper = this::mapRowToAbonelik;

        this.abonelikAktifGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_abonelik_aktif_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", abonelikRowMapper);

        this.abonelikListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_abonelik_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", abonelikRowMapper);

        this.abonelikBaslatCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_abonelik_baslat")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_plan_id", Types.INTEGER),
                        new SqlParameter("p_baslangic_tarihi", Types.DATE),
                        new SqlParameter("p_bitis_tarihi", Types.DATE),
                        new SqlParameter("p_deneme", Types.TINYINT)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("abonelik_id"));

        this.abonelikIptalCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_abonelik_iptal")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_abonelik_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }

    @Override
    public Abonelik aktifGetir(Integer firmaId) {
        Map<String, Object> result = abonelikAktifGetirCall.execute(firmaId);
        List<Abonelik> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public PageResult<Abonelik> listele(Integer firmaId, int limit, int offset) {
        Map<String, Object> result = abonelikListeleCall.execute(firmaId, limit, offset);

        List<Abonelik> items = getItems(result);
        Integer total = (Integer) result.get("p_toplam");

        return new PageResult<>(items, total, limit, offset);
    }

    @Override
    public Integer baslat(Integer firmaId, Integer planId, LocalDate baslangicTarihi, LocalDate bitisTarihi, Boolean deneme) {
        Map<String, Object> result = abonelikBaslatCall.execute(
                firmaId,
                planId,
                toSqlDate(baslangicTarihi),
                toSqlDate(bitisTarihi),
                toTinyInt(deneme)
        );

        List<Integer> items = getItems(result);
        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer iptal(Integer abonelikId, Integer firmaId) {
        Map<String, Object> result = abonelikIptalCall.execute(abonelikId, firmaId);
        List<Integer> items = getItems(result);

        return items.isEmpty() ? 0 : items.get(0);
    }

    private Abonelik mapRowToAbonelik(ResultSet rs, int rowNum) throws SQLException {
        return Abonelik.builder()
                .abonelikId(rs.getInt("abonelik_id"))
                .firmaId(rs.getInt("firma_id"))
                .firmaAd(rs.getString("firma_ad"))
                .planId(rs.getInt("plan_id"))
                .planAd(rs.getString("plan_ad"))
                .maxProje(rs.getInt("max_proje"))
                .maxKullanici(rs.getInt("max_kullanici"))
                .maxTaseron(rs.getInt("max_taseron"))
                .aylikUcret(rs.getBigDecimal("aylik_ucret"))
                .baslangicTarihi(getLocalDate(rs, "baslangic_tarihi"))
                .bitisTarihi(getLocalDate(rs, "bitis_tarihi"))
                .durum(rs.getString("durum"))
                .deneme(rs.getBoolean("deneme"))
                .createdAt(getLocalDateTime(rs, "created_at"))
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

    private Date toSqlDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    private LocalDate getLocalDate(ResultSet rs, String columnName) throws SQLException {
        Date value = rs.getDate(columnName);
        return value == null ? null : value.toLocalDate();
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp value = rs.getTimestamp(columnName);
        return value == null ? null : value.toLocalDateTime();
    }
}