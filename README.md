# Felhő alapú elosztott rendszerek laboratórium<br>WEB app PaaS környezetben - dokumentáció

## Development status
- [x] Fényképek feltöltése
- [ ] Fényképek törlése
- [x] Minden fényképnek legyen neve (max. 40 karakter), és feltöltési dátuma (év-hó-nap óra:perc)
- [x] Fényképek nevének és dátumának listázása.
- [ ] Fényképek listázása név és dátum szerint rendezve.
- [x] Lista egy elemére kattintva mutassa meg a név mögötti képet.
- [x] Felhasználókezelés (regisztráció, belépés, kilépés).
- [x] Feltöltés, törlés csak bejelentkezett felhasználónak engedélyezett.

## Implementation
- Platform: OpenShift
- Backend: Ktor
- Frontend: Kotlin HTML DSL (Server-side rendering)
- Pods
  * Backend: Deployment
  * MongoDB: StatefulSet
- Database (MongoDB)
  * felhasználóadatok, jelszó hashelve (CredentialRepository)
  * Kép (GridFS), metaadatok (PictureRepository)
- Setup:
  * GitHub repo secrets: Dockerhub name, password, OpenShift WebHook URL
  * GitHub Actions Workflow: Build docker image, deploy to OpenShift.
  * Add Bitnami Helm repository
  * MongoDB Helm Chart hozzáadása: PIC, password, database name
  * photo-album deployment: git import URL, database connection string (Administrator/Workloads/Deployment/photo-album-ui/Environment)
  * WebHook URL: Builds/BuildConfigs/WebHooks/Generic
  * MongoDB: fix 1 pod
  * photo-album deployment: autoscaling
- Build:
  0) Push
  1) Build and publish Docker image (Gradle task)
  2) WebHook for OpenShift deployment

- Endpoints:
    * GET /: redirect to /list
    * GET /list: Kilistázza a feltöltött képeket
    * GET /detail/{name}: Megjeleníti a képet, és a nevét+dátumát
    * GET /images/{name}: Ezzel tölthető le egy adott kép
    * GET /upload: Feltöltés oldal megnyitása (de csak akkor, ha be van jelentkezve).
    * POST /upload: Feltölti a képet, és elmenti az adatbázisba, majd redirect to /list
    * GET /login: Bejelentkező felület
    * POST /login: Bejelentkezés: Ellenőrzi, hogy érvényesek-e a session adatok, majd elmenti a sessiont (Ktor kezeli).
    * GET /logout: Kijelentkeztet, majd redirect to /list
    * GET /register: Regisztráló felület
    * POST /register: Regisztrálás -> /list

