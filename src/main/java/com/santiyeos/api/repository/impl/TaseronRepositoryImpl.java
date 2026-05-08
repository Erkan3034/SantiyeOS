package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.Taseron;
import com.santiyeos.api.repository.TaseronRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
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
public class TaseronRepositoryImpl implements TaseronRepository {

    // stored procedure cagrilarini hazir tutuyoruz, boylece her metotta tekrar tekrar call tanimi yazmiyoruz
    private final SimpleJdbcCall taseronListeleCall;
    private final SimpleJdbcCall taseronGetirCall;
    private final SimpleJdbcCall taseronEkleCall;

    public TaseronRepositoryImpl(DataSource dataSource) {
        RowMapper<Taseron> taseronRowMapper = this::mapRowToTaseron;

        // sp_taseron_listele hem kaiyt hem de OUT paramatere oalak toplam sayi doner.
        this.taseronListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_taseron_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_limit", Types.INTEGER),
                        new SqlParameter("p_offset", Types.INTEGER),
                        new SqlOutParameter("p_toplam", Types.INTEGER)
                )
                .returningResultSet("items", taseronRowMapper);


        // tek kayit getiren sp. sonuc yine resultset oalrak geldiği için aynı mmapperi kullaniyruz.
        this.taseronGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_taseron_getir")
                .withoutProcedureColumnMetaDataAccess() //prosedürün meta verilerini sorgulama
                .declareParameters(
                        new SqlParameter("p_taseron_id", Types.INTEGER),
                        new SqlParameter("p_firma_id", Types.INTEGER)
                )
                .returningResultSet("items", taseronRowMapper);

        // ekleme spsi yeni olusan taseron_id degerini select ile dondurur.
        this.taseronEkleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_taseron_ekle")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_ad", Types.VARCHAR),
                        new SqlParameter("p_vergi_no", Types.VARCHAR),
                        new SqlParameter("p_yetkili", Types.VARCHAR),
                        new SqlParameter("p_telefon", Types.VARCHAR),
                        new SqlParameter("p_email", Types.VARCHAR),
                        new SqlParameter("p_uzmanlik", Types.VARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> rs.getInt("taseron_id"));
    }

    @Override
    public PageResult<Taseron> listele(Integer firmaId, int limit, int offset) {

        // firma filtresi ve sayfalama bilgileri spye gonderilir
        Map<String, Object> result = taseronListeleCall.execute(
                firmaId,
                limit,
                offset
        );

        List<Taseron> items = (List<Taseron>) result.get("items");

        // OUT parametre: toplam kayıt sayısı, frontend pagination için
        Integer total = (Integer) result.get("p_toplam");

        return new PageResult<>(items, total, limit, offset);
    }

    @Override
    public Taseron getir(Integer firmaId, Integer taseronId) {
        Map<String, Object> result = taseronGetirCall.execute(
                taseronId,
                firmaId
        );

        List<Taseron> items = (List<Taseron>) result.get("items");

        if (items == null || items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }

    @Override
    public Integer ekle(Integer firmaId, Taseron taseron) {
        Map<String, Object> result = taseronEkleCall.execute(
                firmaId,
                taseron.getAd(),
                taseron.getVergiNo(),
                taseron.getYetkiliAd(),
                taseron.getTelefon(),
                taseron.getEmail(),
                taseron.getUzmanlik()
        );

        List<Integer> items = (List<Integer>) result.get("items");

        if (items == null || items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }


    //veritabanından gelen bir satırı (resultSet) Taseron Java nesnesine çevirir
    private Taseron mapRowToTaseron(ResultSet rs, int rowNum) throws SQLException {
       // db kolon isimleri snake-case, java field isimleri camelCase
        // bu mapper iki motor arasindaki ceviriyi yapıyor

        return Taseron.builder()
                .taseronId(rs.getInt("taseron_id"))
                .firmaId(getInteger(rs, "firma_id"))
                .ad(rs.getString("ad"))
                .vergiNo(rs.getString("vergi_no"))
                .yetkiliAd(rs.getString("yetkili_ad"))
                .telefon(rs.getString("telefon"))
                .email(rs.getString("email"))
                .uzmanlik(rs.getString("uzmanlik"))
                .performansSkoru(rs.getBigDecimal("performans_skoru"))
                .aktif(getBoolean(rs, "aktif"))
                .createdAt(rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null)
                .updatedAt(rs.getTimestamp("updated_at") != null
                        ? rs.getTimestamp("updated_at").toLocalDateTime()
                        : null)
                .build();
    }

    private Integer getInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private Boolean getBoolean(ResultSet rs, String columnName) throws SQLException {
        boolean value = rs.getBoolean(columnName);
        return rs.wasNull() ? null : value;
    }
}
