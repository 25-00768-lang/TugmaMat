# TugmaMat

Web-based construction material reuse and compatibility matching system.
Java 17 + Spring Boot 3 + Thymeleaf + Spring Data JPA + H2 (file database).

## How to run
1. Install JDK 17 (or newer) and Maven.
2. Inside this folder: `mvn spring-boot:run`
3. Open http://localhost:8080

(In IntelliJ / Eclipse / VS Code: open the folder as a Maven project and run `TugmaMatApplication`.)

## Demo accounts
| Role      | Name      | Email                     | Password     |
|-----------|-----------|---------------------------|--------------|
| Provider  | Provider  | provider@tugmamat.ph      | provider123  |
| Requester | Requester | requester@tugmamat.ph     | requester123 |
| Admin     | Admin     | admin@tugmamat.ph         | admin123     |

The database lives in `./data/` and uploaded images live in `./uploads/`.
To reset everything, delete those two folders. (If you keep an older `./data`, the former
demo users "Mang Roy" and "Aling Nena" are renamed to "Provider" and "Requester" on startup.)

## Structure
- `model/`    entities (User, Material, MaterialRequest, Message) and enums
- `repo/`     Spring Data repositories
- `service/`  `MatchingService`, the compatibility score and explanation
- `web/`      controller, login filter, demo data
- `resources/templates/`  Thymeleaf pages
- `resources/static/css/` styling and animations
- `resources/static/js/`  `anim.js`, scroll-reveal and counting numbers

## How the compatibility score is calculated (100 points)
| Criteria      | Points | How |
|---------------|--------|-----|
| Category      | 20     | Same = 20, different = 0 |
| Name / type   | 20     | Share of requested words found in the name or description |
| Size          | 20     | Length and width: listing / requested (capped at 100%) |
| Quantity      | 20     | available / needed (capped at 100%) |
| Condition     | 10     | Meets the requirement = 10, each step lower = -4 |
| Location      | 10     | Same or overlapping location = 10; "Entire Batangas" matches any place in Batangas |

70% and up: close match. 40% to 69%: alternative. Below 40%: not shown.
Everything lives in `MatchingService.java`, so the weights are easy to change.

## Features
- Register / log in as Provider, Requester or Admin
- Post and edit listings, upload photos (demo data already has photos)
- Request materials, delete requests, see compatibility scores and alternatives
- Availability tracking (Available / Reserved / Given away), messages to the provider, history and reports
- Taking materials works like a Shopee order: the requester picks a quantity and takes it (no code). Only the provider/admin has the Pickups page, where they mark pieces as picked up or cancel
- Admin panel for reported listings
- Animations on every box: staggered scroll-reveal, hover lift, counting numbers, drawing icons,
  filling score tape, sliding table rows, neumorphic buttons (`style.css`, `anim.js`).
  Respects the OS "reduce motion" setting.

## Tests
`mvn test` runs `MatchingServiceTest` (92%, 80% and 44% on the demo data).

## Demo material photos
`static/img/materials/` holds the demo listing photos (realistic jpg photos: lumber, plywood, tiles, cement, sand, gravel, steel, roofing, PVC pipe, wire, paint, windows, doors, hollow blocks), which `DataSeeder` assigns. About 26 demo listings across Batangas are seeded.
With an older `./data`, demo listings without a photo get one on restart.

## Hero image
`static/img/hero.jpg` is included. Replace it with your own photo (e.g. a construction site or leftover lumber) if you like.
