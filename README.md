# Felhő alapú elosztott rendszerek laboratórium<br>WEB app PaaS környezetben - dokumentáció

## Elkészült funkciók
- [x] Fényképek feltöltése
- [ ] Fényképek törlése
- [x] Minden fényképnek legyen neve (max. 40 karakter), és feltöltési dátuma (év-hó-nap óra:perc)
- [x] Fényképek nevének és dátumának listázása.
- [ ] Fényképek listázása név és dátum szerint rendezve.
- [x] Lista egy elemére kattintva mutassa meg a név mögötti képet.
- [x] Felhasználókezelés (regisztráció, belépés, kilépés).
- [x] Feltöltés, törlés csak bejelentkezett felhasználónak engedélyezett.

Az alkalmazást OpenShift platformra telepítettük ki (OpenShift Dedicated). A fejlesztéshez a Kotlin nyelvet választottuk, a backend-hez a Ktor library, míg a frontend-hez a Kotlin HTML DSL-t használtunk, amellyel server-side renderinghez hasonló elven állítható elő a webes felület.<br>
A kitelepített alkalmazás 2 Pod-ból áll:
- Backend: Ebben fut a Kotlinban implementált alkalmazás, Deployment-ként van kitelepítve.
- MongoDB: Ebben egy MongoDB szerver fut, StatefulSet-ként van kitelepítve, az OpenShift automatikusan létrehoz hozzá egy PersistentVolume-ot, hogy az adatok ne vesszenek el a pod leállásakor.

## Adatbázis

A MongoDB adatbázisban az alábbi entitások vannak eltárolva:
- Felhasználók: Minden felhasználóról a felhasználónév, és a jelszó hash, egy MongoDB collection-ben. Ennek kezelését a backendben a CredentialRepository végzi.
- Képek: Minden képről a fájlnév, a feltöltés dátuma, valamint a tartalma (bináris formában), ez a MongoDB GridFS funkciójával lett megvalósítva. Ennek kezelését a PictureRepository végzi a backendben.

## Deployment

Az alkalmazás OpenShift környezetbe történő kitelepítéséhez az alábbi lépéseken kellett végigmennünk:
- A GitHub repo secret-ek közé fel kellett vennünk egy DockerHub account felhasználónevét és jelszavát, amely alá push-olásra kerül a backendből buildelt Docker image.
- Egy GitHub Actions Workflow segítségével beállítottuk, hogy a repo-ba történő push hatására elkészüljön egy új Docker image, és feltöltésre kerüljön az előző DockerHub account alá egy image repository-ba.
- Bitnami Helm repository hozzáadása OpenShift-en (Developer/Helm/Repositories).
- Bitnami MongoDB Helm Chart létrehozása (Developer/+Add/Helm Chart), itt a konfigurációnál meg kellett adni az Authentication Configuration alatt a MongoDB admin user nevet és jelszót, valamint az adatbázis nevét.
- Ezután létre kellett hozni egy Deployment-et, ami a backendből build-elt Docker image-et futtatja (Developer/+Add/Import from Git), amelyhez meg kellett adni a GitHub repo URL-t, továbbá Dockerfile Import Strategy lett kiválasztva. A repo-ban lévő Dockerfile a legutoljára build-elt image-re mutat. Ezen kívül meg kellett adni egy környezeti változót CONNECTION_STRING néven, amely segítségével a backend csatlakozni tud a MongoDB adatbázishoz (Administrator/Workloads/Deployment alatt a Deployment-et kiválasztva, és azon belül az Environment tab).
- A GitHub repo secret-ek közé ezután felvettük a Developer/Builds/<build neve>/WebHooks szekció alatt található Generic WebHook URL-t.
- A Github Actions Workflow kiegészítettük, hogy minden push hatására, a Docker image build-elése után frissüljön az OpenShift build, a WebHook URL segítségével.
- A MongoDB StatefulSet-re beállítottuk, hogy 1 példányban kerüljön futtatásra, míg a Backend Deployment-re automatikus skálázás lett bekapcsolva.

## API
- GET /: redirect to /list
- GET /list: Kilistázza a feltöltött képeket
- GET /detail/{name}: Megjeleníti a képet, és a nevét+dátumát
- GET /images/{name}: Ezzel tölthető le egy adott kép
- GET /upload: Feltöltés oldal megnyitása (de csak akkor, ha be van jelentkezve).
- POST /upload: Feltölti a képet, és elmenti az adatbázisba, majd redirect to /list
- GET /login: Bejelentkező felület
- POST /login: Bejelentkezés: Ellenőrzi, hogy érvényesek-e a session adatok, majd elmenti a sessiont (Ktor kezeli).
- GET /logout: Kijelentkeztet, majd redirect to /list
- GET /register: Regisztráló felület
- POST /register: Regisztrálás -> /list

