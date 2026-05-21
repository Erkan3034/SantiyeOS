package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.Firma;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.FirmaRepository;
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
public class FirmaRepositoryImpl implements FirmaRepository {

    private final SimpleJdbcCall firmaListeleCall;
    private final SimpleJdbcCall firmaGetirCall;
    private final SimpleJdbcCall firmaEkleCall;
    private final SimpleJdbcCall firmaGuncelleCall;
    private final SimpleJdbcCall firmaPasiflestirCall;

    public FirmaRepositoryImpl(DataSource dataSource) {
        RowMapper<Firma> firmaRowMapper = this::mapRowToFirma;

        this.firmaListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_firma_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_aktif", Types.TINYINT),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", firmaRowMapper);

        this.firmaGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_firma_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", firmaRowMapper);

        this.firmaEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_firma_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_ad", Types.VARCHAR),
                        new SqlParameter("p_vergi_no", Types.VARCHAR),
                        new SqlParameter("p_telefon", Types.VARCHAR),
                        new SqlParameter("p_email", Types.VARCHAR),
                        new SqlParameter("p_adres", Types.LONGVARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("firma_id"));

        this.firmaGuncelleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_firma_guncelle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_ad", Types.VARCHAR),
                        new SqlParameter("p_vergi_no", Types.VARCHAR),
                        new SqlParameter("p_telefon", Types.VARCHAR),
                        new SqlParameter("p_email", Types.VARCHAR),
                        new SqlParameter("p_adres", Types.LONGVARCHAR),
                        new SqlParameter("p_aktif", Types.TINYINT)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));

        this.firmaPasiflestirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_firma_pasiflestir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("etkilenen_satir"));
    }

    @Override
    public PageResult<Firma> listele(Boolean aktif, int limit, int offset) {
        Map<String, Object> result = firmaListeleCall.execute(
                toTinyInt(aktif),
                limit,
                offset
        );

        List<Firma> items = getItems(result);
        Integer total = (Integer) result.get("p_toplam");

        return new PageResult<>(items, total, limit, offset);
    }

    @Override
    public Firma getir(Integer firmaId) {
        Map<String, Object> result = firmaGetirCall.execute(firmaId);
        List<Firma> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer ekle(Firma firma) {
        Map<String, Object> result = firmaEkleCall.execute(
                firma.getAd(),
                firma.getVergiNo(),
                firma.getTelefon(),
                firma.getEmail(),
                firma.getAdres()
        );

        List<Integer> items = getItems(result);
        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public Integer guncelle(Integer firmaId, Firma firma) {
        Map<String, Object> result = firmaGuncelleCall.execute(
                firmaId,
                firma.getAd(),
                firma.getVergiNo(),
                firma.getTelefon(),
                firma.getEmail(),
                firma.getAdres(),
                toTinyInt(firma.getAktif())
        );

        List<Integer> items = getItems(result);
        return items.isEmpty() ? 0 : items.get(0);
    }

    @Override
    public Integer pasiflestir(Integer firmaId) {
        Map<String, Object> result = firmaPasiflestirCall.execute(firmaId);
        List<Integer> items = getItems(result);

        return items.isEmpty() ? 0 : items.get(0);
    }

    private Firma mapRowToFirma(ResultSet rs, int rowNum) throws SQLException {
        return Firma.builder()
                .firmaId(rs.getInt("firma_id"))
                .ad(rs.getString("ad"))
                .vergiNo(rs.getString("vergi_no"))
                .telefon(rs.getString("telefon"))
                .email(rs.getString("email"))
                .adres(rs.getString("adres"))
                .aktif(rs.getBoolean("aktif"))
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

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp value = rs.getTimestamp(columnName);
        return value == null ? null : value.toLocalDateTime();
    }
}