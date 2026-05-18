package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.Kullanici;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.KullaniciRepository;
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
public class KullaniciRepositoryImpl implements KullaniciRepository {

    private final SimpleJdbcCall kullaniciListeleCall;
    private final SimpleJdbcCall kullaniciGetirCall;
    private final SimpleJdbcCall kullaniciEkleCall;
    private final SimpleJdbcCall kullaniciGuncelleCall;
    private final SimpleJdbcCall kullaniciSilCall;
    private final SimpleJdbcCall kullaniciSifreGuncelleCall;

    public KullaniciRepositoryImpl(DataSource dataSource) {
        RowMapper<Kullanici> kullaniciRowMapper = this::mapRowToKullanici;

        this.kullaniciListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_kullanici_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_rol", Types.VARCHAR),
                        new SqlParameter("p_aktif", Types.TINYINT),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", kullaniciRowMapper);

        this.kullaniciGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_kullanici_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", kullaniciRowMapper);

        this.kullaniciEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_kullanici_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_taseron_id", Types.INTEGER),
                        new SqlParameter("p_ad", Types.VARCHAR),
                        new SqlParameter("p_soyad", Types.VARCHAR),
                        new SqlParameter("p_email", Types.VARCHAR),
                        new SqlParameter("p_sifre_hash", Types.VARCHAR),
                        new SqlParameter("p_rol", Types.VARCHAR),
                        new SqlParameter("p_telefon", Types.VARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("kullanici_id"));

        this.kullaniciGuncelleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_kullanici_guncelle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_taseron_id", Types.INTEGER),
                        new SqlParameter("p_ad", Types.VARCHAR),
                        new SqlParameter("p_soyad", Types.VARCHAR),
                        new SqlParameter("p_telefon", Types.VARCHAR),
                        new SqlParameter("p_rol", Types.VARCHAR),
                        new SqlParameter("p_aktif", Types.TINYINT)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.kullaniciSilCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_kullanici_sil")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.kullaniciSifreGuncelleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_kullanici_sifre_guncelle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_kullanici_id", Types.INTEGER),
                        new SqlParameter("p_sifre_hash", Types.VARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }

    @Override
    public PageResult<Kullanici> listele(Integer firmaId, String rol, Boolean aktif, int limit, int offset) {
        Map<String, Object> result = kullaniciListeleCall.execute(
                firmaId,
                rol,
                toTinyInt(aktif),
                limit,
                offset
        );

        List<Kullanici> items = getItems(result);
        Integer total = (Integer) result.get("p_toplam");

        return new PageResult<>(items, total, limit, offset);
    }

    @Override
    public Kullanici getir(Integer firmaId, Integer kullaniciId) {
        Map<String, Object> result = kullaniciGetirCall.execute(kullaniciId, firmaId);
        List<Kullanici> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, Kullanici kullanici) {
        Map<String, Object> result = kullaniciEkleCall.execute(
                firmaId,
                kullanici.getTaseronId(),
                kullanici.getAd(),
                kullanici.getSoyad(),
                kullanici.getEmail(),
                kullanici.getSifreHash(),
                kullanici.getRol(),
                kullanici.getTelefon()
        );

        List<Integer> items = getItems(result);
        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer guncelle(Integer firmaId, Integer kullaniciId, Kullanici kullanici) {
        Map<String, Object> result = kullaniciGuncelleCall.execute(
                kullaniciId,
                firmaId,
                kullanici.getTaseronId(),
                kullanici.getAd(),
                kullanici.getSoyad(),
                kullanici.getTelefon(),
                kullanici.getRol(),
                toTinyInt(kullanici.getAktif())
        );

        List<Integer> items = getItems(result);
        return items.isEmpty() ? 0 : items.get(0);
    }

    @Override
    public Integer sil(Integer firmaId, Integer kullaniciId) {
        Map<String, Object> result = kullaniciSilCall.execute(kullaniciId, firmaId);
        List<Integer> items = getItems(result);

        return items.isEmpty() ? 0 : items.get(0);
    }

    @Override
    public Integer sifreGuncelle(Integer kullaniciId, String sifreHash) {
        Map<String, Object> result = kullaniciSifreGuncelleCall.execute(kullaniciId, sifreHash);
        List<Integer> items = getItems(result);

        return items.isEmpty() ? 0 : items.get(0);
    }

    private Kullanici mapRowToKullanici(ResultSet rs, int rowNum) throws SQLException {
        return Kullanici.builder()
                .kullaniciId(rs.getInt("kullanici_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .taseronId(getInteger(rs, "taseron_id"))
                .ad(rs.getString("ad"))
                .soyad(rs.getString("soyad"))
                .email(rs.getString("email"))
                .rol(rs.getString("rol"))
                .telefon(rs.getString("telefon"))
                .aktif(rs.getBoolean("aktif"))
                .sonGiris(getLocalDateTime(rs, "son_giris"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .updatedAt(getLocalDateTime(rs, "updated_at"))
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