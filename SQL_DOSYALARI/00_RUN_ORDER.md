# SantiyeOS Database Final Run Order

Bu klasor final veritabani paketidir. Orijinal `C:\yedekler\capstone_project\database` dosyalari degistirilmedi.

Workbench uzerinde dosyalari asagidaki sirayla calistir:

1. `00_init.sql`
2. `01_tablolar/01_firma.sql`
3. `01_tablolar/02_kullanici.sql`
4. `01_tablolar/03_taseron_ve_iliskili_tablolar.sql`
5. `01_tablolar/04_proje_is_yonetimi.sql`
6. `01_tablolar/05_hakedis_odeme.sql`
7. `01_tablolar/06_malzeme_stok.sql`
8. `01_tablolar/07_bildirim.sql`
9. `02_fonksiyonlar/01_kullanici_yetki.sql`
10. `02_fonksiyonlar/02_proje_butce_ve_kullanici_yetki.sql`
11. `02_fonksiyonlar/03_abonelik_limit.sql`
12. `03_prosedurler/01_yardimci_performans_guncelleme.sql`
13. `03_prosedurler/02_firma.sql`
14. `03_prosedurler/03_kullanici.sql`
15. `03_prosedurler/04_auth.sql`
16. `03_prosedurler/05_refresh_token.sql`
17. `03_prosedurler/06_abonelik.sql`
18. `03_prosedurler/07_proje.sql`
19. `03_prosedurler/08_proje_kullanici_atama.sql`
20. `03_prosedurler/09_taseron.sql`
21. `03_prosedurler/10_sozlesme.sql`
22. `03_prosedurler/11_is_emri.sql`
23. `03_prosedurler/12_is_emri_not_rapor.sql`
24. `03_prosedurler/13_abonelik_limit.sql`
25. `03_prosedurler/14_hakedis.sql`
26. `03_prosedurler/15_odeme.sql`
27. `03_prosedurler/16_malzeme_stok.sql`
28. `03_prosedurler/17_bildirim.sql`
29. `03_prosedurler/18_raporlar.sql`
30. `03_prosedurler/19_lookup.sql`
31. `04_triggers/01_triggers.sql`
32. `05_events/01_events.sql`

Notlar:

- Hak edis unique index duzeltmesi `03_prosedurler/14_hakedis.sql` icine gomuldu.
- Abonelik limit fonksiyonlari `02_fonksiyonlar/03_abonelik_limit.sql` icinde, DAL tarafinin cagiracagi prosedur ise `03_prosedurler/13_abonelik_limit.sql` icinde.
- Final klasorde test/debug amacli `SELECT *`, `SHOW INDEX` gibi komutlar tutulmadi.
- Bu paket tablo verisini silmek icin degil, temiz kurulum veya kontrollu migration icin hazirlandi.
