package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.Kullanici;
import com.santiyeos.api.repository.AuthRepository;
import org.springframework.jdbc.core.RowMapper;
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
public class AuthRepositoryImpl implements AuthRepository {

    private final SimpleJdbcCall kullaniciEmailGetirCall;
    private final SimpleJdbcCall kullaniciIdGetirCall;
    private final SimpleJdbcCall sonGirisGuncelleCall;

    public AuthRepositoryImpl(DataSource dataSource) {
        RowMapper<Kullanici> kullaniciRowMapper = this::mapRowToKullanici;

        this.kullaniciEmailGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_auth_kullanici_email_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(new SqlParameter("p_email", Types.VARCHAR))
                .returningResultSet("items", kullaniciRowMapper);

        this.kullaniciIdGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_auth_kullanici_id_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(new SqlParameter("p_kullanici_id", Types.INTEGER))
                .returningResultSet("items", kullaniciRowMapper);

        this.sonGirisGuncelleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_kullanici_son_giris_guncelle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(new SqlParameter("p_kullanici_id", Types.INTEGER));
    }

    @Override
    public Kullanici emailIleGetir(String email) {
        Map<String, Object> result = kullaniciEmailGetirCall.execute(email);
        List<Kullanici> items = getItems(result);
        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Kullanici idIleGetir(Integer kullaniciId) {
        Map<String, Object> result = kullaniciIdGetirCall.execute(kullaniciId);
        List<Kullanici> items = getItems(result);
        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public void sonGirisGuncelle(Integer kullaniciId) {
        sonGirisGuncelleCall.execute(kullaniciId);
    }

    private Kullanici mapRowToKullanici(ResultSet rs, int rowNum) throws SQLException {
        return Kullanici.builder()
                .kullaniciId(rs.getInt("kullanici_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .taseronId(getInteger(rs, "taseron_id"))
                .ad(rs.getString("ad"))
                .soyad(rs.getString("soyad"))
                .email(rs.getString("email"))
                .sifreHash(rs.getString("sifre_hash"))
                .rol(rs.getString("rol"))
                .telefon(rs.getString("telefon"))
                .aktif(getBoolean(rs, "aktif"))
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

    private Integer getInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private Boolean getBoolean(ResultSet rs, String columnName) throws SQLException {
        boolean value = rs.getBoolean(columnName);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp value = rs.getTimestamp(columnName);
        return value == null ? null : value.toLocalDateTime();
    }
}
