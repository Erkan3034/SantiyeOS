// Java’dan DB’ye konuşmak
// projede doğrudan SQL yazmayacağız stored procedure çağıracağız

package com.santiyeos.api.repository;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.Taseron;
import java.util.List;

//taseron için hangi işlemler yapılabilir (sozlesme)?
public interface TaseronRepository {

    PageResult<Taseron> listele(Integer firmaId, int limit, int offset); // limit, offset parametreleri

    Taseron getir(Integer firmaId, Integer taseronId);

    Integer ekle(Integer firmaId, Taseron taseron);

    Integer guncelle(Integer firmaId, Integer taseronId, Taseron taseron);

    Integer sil(Integer firmaId, Integer taseronId, Integer kullaniciId);

}
