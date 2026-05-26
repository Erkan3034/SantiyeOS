package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.ProjeKullanici;
import com.santiyeos.api.repository.ProjeKullaniciRepository;
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
public class ProjeKullaniciRepositoryImpl implements ProjeKullaniciRepository {

    private final SimpleJdbcCall projeKullaniciAtaCall;
    private final SimpleJdbcCall projeKullaniciKaldirCall;
    private final SimpleJdbcCall projeKullaniciListeleCall;

    public ProjeKullaniciRepositoryImpl(DataSource dataSource) {
        RowMapper<ProjeKullanici> rowMapper = this::mapRow;

        this.projeKullaniciAtaCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_proje_kullanici_ata")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_proje_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                );

        this.projeKullaniciKaldirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_proje_kullanici_kaldir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_proje_id", Types.INTEGER),
                        new SqlParameter("p_kullanici_id", Types.INTEGER)
                );

        this.projeKullaniciListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_proje_kullanici_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_proje_id", Types.INTEGER),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", rowMapper);
    }

    @Override
    public void ata(Integer firmaId, Integer projeId, Integer kullaniciId) {
        projeKullaniciAtaCall.execute(firmaId, projeId, kullaniciId);
    }

    @Override
    public void kaldir(Integer firmaId, Integer projeId, Integer kullaniciId) {
        projeKullaniciKaldirCall.execute(firmaId, projeId, kullaniciId);
    }

    @Override
    public PageResult<ProjeKullanici> listele(Integer firmaId, Integer projeId, int limit, int offset) {
        Map<String, Object> result = projeKullaniciListeleCall.execute(firmaId, projeId, limit, offset);
        Integer total = (Integer) result.get("p_toplam");
        return new PageResult<>(getItems(result), total, limit, offset);
    }

    private ProjeKullanici mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ProjeKullanici.builder()
                .projeId(rs.getInt("proje_id"))
                .kullaniciId(rs.getInt("kullanici_id"))
                .firmaId(rs.getInt("firma_id"))
                .ad(rs.getString("ad"))
                .soyad(rs.getString("soyad"))
                .rol(rs.getString("rol"))
                .email(rs.getString("email"))
                .atanmaTarihi(getLocalDateTime(rs, "atanma_tarihi"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<ProjeKullanici> getItems(Map<String, Object> result) {
        Object items = result.get("items");
        return items == null ? List.of() : (List<ProjeKullanici>) items;
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp value = rs.getTimestamp(columnName);
        return value == null ? null : value.toLocalDateTime();
    }
}
