# FootyBox

FootyBox არის საბაკალავრო პროექტი — ფეხბურთის მატჩების არქივი და პერსონალური დღიური. იდეა Letterboxd-ის მსგავსია:
მომხმარებელი უყურებს მატჩებს, აფასებს, წერს შეხსენებებს და ინახავს საყვარელ თამაშებს ერთ ადგილას.

სისტემა მუშაობს რეალურ მონაცემებზე: ანგარიშები, დღიური, შეფასებები და შენახული მატჩები ინახება PostgreSQL-ში და
აპლიკაციის გადატვირთვის შემდეგაც რჩება.

## ძირითადი ფუნქციები

- მომხმარებლის რეგისტრაცია და შესვლა JWT ავტორიზაციით
- მატჩების არქივი ძიებით, შეჯიბრების და წლის ფილტრებით
- Spoiler-free რეჟიმი — შედეგების დამალვა სანამ თვითონ არ ნახავ
- მატჩის დეტალური გვერდი, საზოგადო შეფასებები, მიმოხილვები და კომენტარები
- Favourite — მატჩის შენახვა
- Log / Review — რეიტინგი, ტექსტი, ნახვის თარიღი, სტადიონი, Player of the Match, spoiler ფლაგი
- პროფილი — bio, საყვარელი გუნდი, avatar/banner (preset ან ატვირთვა)
- სურვილისამებრ: football-data.org API-დან მატჩების იმპორტი (ნაგულისხმევად გამორთული)

## გამოყენებული ტექნოლოგიები

| კატეგორია        | ტექნოლოგია                                                             |
|------------------|------------------------------------------------------------------------|
| Backend          | Java 17, Spring Boot 3.5, Spring MVC, Spring Security, Spring Data JPA |
| მონაცემთა ბაზა   | PostgreSQL 16, Flyway migrations                                       |
| Build            | Maven                                                                  |
| Frontend         | Static HTML, CSS, JavaScript (Spring Boot-ის static resources)         |
| ინფრასტრუქტურა   | Docker Compose                                                         |
| API დოკუმენტაცია | Swagger / OpenAPI                                                      |
| უსაფრთხოება      | BCrypt, JWT                                                            |

## პროექტის სტრუქტურა

```
FootyBox/
├── src/main/java/com/footybox/   # Backend (controllers, services, repositories, security)
├── src/main/resources/
│   ├── db/migration/               # Flyway SQL migrations (V1–V7)
│   ├── static/                     # HTML, CSS, JS, სურათები
│   └── application.yml             # კონფიგურაცია
├── src/test/java/                  # JUnit ტესტები
├── compose.yaml                    # PostgreSQL Docker სერვისი
├── pom.xml                         # Maven dependencies
├── README.md                       # ეს ფაილი
```

Backend პაკეტები: `auth`, `user`, `diary`, `football`, `importjob`, `provider`, `security`, `search`.

Frontend: `js/app.js` — API მოთხოვნები; `assets/js/ui.js` — გაზიარებული UI ელემენტები.

## მოთხოვნები

- JDK 17
- Maven 3.9+ ან პროექტის Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Docker Desktop (PostgreSQL-ისთვის)

## ინსტალაცია და გაშვება

### 1. PostgreSQL (Docker)

```powershell
cd FootyBox
docker compose up -d
docker compose ps
```

- Host პორტი: **5433** (container-ში 5432)
- ბაზა: `footybox`, მომხმარებელი/პაროლი: `footybox` / `footybox`
- მონაცემები ინახება volume-ში `footybox-postgres-data`
- `docker compose down -v` მონაცემებს სრულად შლის — გამოიყენეთ მხოლოდ განზრახ

Flyway აპლიკაციის გაშვებისას ავტომატურად ასრულებს migration-ებს.

### 2. Spring Boot აპლიკაცია

**IntelliJ IDEA:** გახსენით `pom.xml` Maven პროექტად, SDK და Maven Runner — Java 17, გაუშვით
`com.footybox.FootyboxApplication`.

**ტერმინალი:**

```powershell
cd FootyBox

$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

.\mvnw.cmd spring-boot:run
```

### 3. მისამართები

| რესურსი    | URL                                         |
|------------|---------------------------------------------|
| აპლიკაცია  | http://localhost:8080/                      |
| Swagger UI | http://localhost:8080/swagger-ui.html       |
| JDBC       | `jdbc:postgresql://127.0.0.1:5433/footybox` |

თუ ბაზაში უკვე არის იმპორტირებული მატჩები, football-data.org token ჩვეულებრივ browsing-ისთვის არ არის საჭირო.

## აწყობა და ტესტები

```powershell
cd FootyBox

$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

mvn clean package
```

ბრძანება აკომპილირებს პროექტს, ასრულებს ტესტებს და ქმნის executable JAR-ს `target/` საქაღალდეში.

## დემო ნაკადი (Live Demo)

1. გახსენით http://localhost:8080/ და **Sign up**-ით შექმენით ანგარიში.
2. **Sign in** email-ით და პაროლით (JWT ინახება ბრაუზერში).
3. **Archive** — მოძებნეთ მატჩი, აირჩიეთ შეჯიბრება ან წელი, სცადეთ spoiler-free toggle.
4. გახსენით მატჩი, ნახეთ ანგარიში და მიმოხილვები.
5. **Favourite** — შეინახეთ მატჩი.
6. **Log / Review** — დაამატეთ რეიტინგი, ტექსტი, თარიღი და სურვილისამებრ stadium / Player of the Match.
7. **Profile** — ნახეთ დღიური, შენახული მატჩები; შეცვალეთ bio, avatar ან banner.

## არქივის ფილტრები

- **წელი (2020s)** — ფილტრავს ათწლეულის მიხედვით.
- **დალაგება** — **Newest** (ახალი პირველი) ან **Oldest** (ძველი პირველი).
- **ძიება** — გუნდის ან შეჯიბრების სახელით.

## მატჩების იმპორტი (არასავალდებულო)

იმპორტი ნაგულისხმევად გამორთულია. API token მხოლოდ IntelliJ run configuration-ში ან გარემოს ცვლადებში — არა Git-ში.

```text
FOOTBALL_DATA_TOKEN=your-private-token
FOOTBALL_IMPORT_ENABLED=true
FOOTBALL_IMPORT_COMPETITION_CODES=PL
FOOTBALL_IMPORT_FROM_YEAR=2025
FOOTBALL_IMPORT_TO_YEAR=2025
FOOTBALL_IMPORT_EXIT_AFTER_RUN=true
```

წარმატებული probe-ის შემდეგ შეგიძლიათ გაფართოებული იმპორტი. ჩვეულებრივ demo-სთვის დააყენეთ
`FOOTBALL_IMPORT_ENABLED=false`.

## შეზღუდვები

- Team, player და list detail გვერდები static sample კონტენტითაა და ცალკე backend API-ებს არ იყენებს.
- Social feed, follows და მომხმარებლის lists ამ ვერსიაში არ არის.
- JWT ინახება browser `localStorage`-ში; logout მხოლოდ client token-ს შლის.
- `application.yml` და `compose.yaml`-ის default credentials მხოლოდ ლოკალური განვითარებისთვისაა.
- არქივის მოცულობა დამოკიდებულია football-data.org ანგარიშზე; აპლიკაცია მიუწვდომელ ისტორიულ მონაცემებს არ „გამოიგონებს“.
- რედაქტორული სურათები განკუთვნილია არაჯარო სასწავლო პროექტისთვის.

## რა შემიძლია ავხსნა

- **ავტორიზაცია:** BCrypt პაროლი, JWT login/register, `/api/auth/me`
- **PostgreSQL:** JPA entities, repositories, Flyway V1–V7, მონაცემების persistence restart-ის შემდეგ
- **არქივი:** `/api/matches`, `/api/competitions`, ფილტრები და spoiler-safe ველები
- **დღიური:** `/api/diary/logs` — rating, review, watched date, stadium, POTM, spoiler flag
- **Favourites და reviews:** შენახვა, owner-only edit/delete, კომენტარები
- **პროფილი:** bio, favourite team, avatar/banner upload PostgreSQL-ში
- **Docker + Flyway:** `compose.yaml`, პორტი 5433, named volume
- **Backend/Frontend სტრუქტურა:** controller → service → repository; static frontend + REST API

## უსაფრთხოება

Production გარემოში გამოიყენეთ ძლიერი `JWT_SECRET` და production DB credentials. Development default-ები საჯაროდ არ
გამოიყენოთ.
