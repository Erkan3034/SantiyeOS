package com.santiyeos.api.repository.impl;

import com.santiyeos.api.repository.AbonelikLimitRepository;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Repository
public class AbonelikLimitRepositoryImpl implements AbonelikLimitRepository {

    private final SimpleJdbcCall abonelikLimitKontrolCall;

    public AbonelikLimitRepositoryImpl(DataSource dataSource) {
        this.abonelikLimitKontrolCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_abonelik_limit_kontrol")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_firma_id", Types.INTEGER),
                        new SqlParameter("p_kaynak_tipi", Types.VARCHAR)
                )
                .returningResultSet("items", (rs, rowNum) -> new LimitKontrolResult(
                        rs.getInt("aktif_abonelik_var_mi") == 1,
                        rs.getInt("plan_limit"),
                        rs.getInt("kullanim_sayisi")
                ));
    }

    @Override
    public boolean aktifAbonelikVarMi(Integer firmaId) {
        return kontrolEt(firmaId, "PROJE").aktifAbonelikVarMi();
    }

    @Override
    public int planLimit(Integer firmaId, String kaynakTipi) {
        return kontrolEt(firmaId, kaynakTipi).planLimit();
    }

    @Override
    public int kullanimSayisi(Integer firmaId, String kaynakTipi) {
        return kontrolEt(firmaId, kaynakTipi).kullanimSayisi();
    }

    private LimitKontrolResult kontrolEt(Integer firmaId, String kaynakTipi) {
        Map<String, Object> result = abonelikLimitKontrolCall.execute(firmaId, kaynakTipi);
        List<LimitKontrolResult> items = getItems(result);

        if (items.isEmpty()) {
            return new LimitKontrolResult(false, 0, 0);
        }

        return items.get(0);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getItems(Map<String, Object> result) {
        Object items = result.get("items");
        return items == null ? List.of() : (List<T>) items;
    }

    private record LimitKontrolResult(
            boolean aktifAbonelikVarMi,
            int planLimit,
            int kullanimSayisi
    ) {
    }
}