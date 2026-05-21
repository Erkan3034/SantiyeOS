package com.santiyeos.api.repository.impl;

import com.santiyeos.api.model.AbonelikPlan;
import com.santiyeos.api.repository.AbonelikPlanRepository;
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
public class AbonelikPlanRepositoryImpl implements AbonelikPlanRepository {

    private final SimpleJdbcCall planListeleCall;
    private final SimpleJdbcCall planGetirCall;

    public AbonelikPlanRepositoryImpl(DataSource dataSource) {
        RowMapper<AbonelikPlan> planRowMapper = this::mapRowToPlan;

        this.planListeleCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_abonelik_plan_listele")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_aktif", Types.TINYINT)
                )
                .returningResultSet("items", planRowMapper);

        this.planGetirCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_abonelik_plan_getir")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_plan_id", Types.INTEGER)
                )
                .returningResultSet("items", planRowMapper);
    }

    @Override
    public List<AbonelikPlan> listele(Boolean aktif) {
        Map<String, Object> result = planListeleCall.execute(toTinyInt(aktif));
        return getItems(result);
    }

    @Override
    public AbonelikPlan getir(Integer planId) {
        Map<String, Object> result = planGetirCall.execute(planId);
        List<AbonelikPlan> items = getItems(result);

        return items.isEmpty() ? null : items.get(0);
    }

    private AbonelikPlan mapRowToPlan(ResultSet rs, int rowNum) throws SQLException {
        return AbonelikPlan.builder()
                .planId(rs.getInt("plan_id"))
                .ad(rs.getString("ad"))
                .maxProje(rs.getInt("max_proje"))
                .maxKullanici(rs.getInt("max_kullanici"))
                .maxTaseron(rs.getInt("max_taseron"))
                .aylikUcret(rs.getBigDecimal("aylik_ucret"))
                .aktif(rs.getBoolean("aktif"))
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
}