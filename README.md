**Algorithm Visualizer Stack**
- Cinci servicii: `user-service` (Spring Boot + MySQL), `authentification-service` (Spring Boot + MySQL pentru auth), `algo-service` (Spring Boot, pașii sorting network), `explanation-engine` (Spring Boot, generează explicații AI prin Ollama), `frontend` (React + Nginx).
- Docker Compose pornește toate serviciile; porturi: 3000 (frontend), 8080 (user API), 8081 (auth API), 8082 (algo API), 8083 (explanation API), 3306/3307 (MySQL).

**AI explanations (explanation-engine + Ollama)**
- Toată logica LLM este izolată în `explanation-engine`.
- Endpoint: `POST /api/explanations` cu payload:
```
{
  "algorithm": "BITONIC | ODD_EVEN",
  "direction": "ASC | DESC",
  "stepType": "COMPARATOR",
  "stepIndex": 12,
  "stageIndex": 3,
  "leftIndex": 2,
  "rightIndex": 5,
  "leftValue": 7,
  "rightValue": 4,
  "swapped": true,
  "arrayState": [1,4,7,8,...]
}
```
- Conectare la Ollama local (implicit `http://host.docker.internal:11434/api/chat`, model `llama3`). Poți schimba cu variabilele de mediu `OLLAMA_BASE_URL` și `OLLAMA_MODEL`.
- Model implicit setat acum: `llama3.2` (variabilă `OLLAMA_MODEL` în `compose.yaml` și `application.yml`).
- Frontend-ul afișează lângă comparator un buton „Explicație AI (Ollama)”; la click trimite pasul curent către `explanation-engine` și afișează răspunsul.
- UX recomandată: două stări clare — **Run mode** (animația curge) și **Inspect mode** (pauză automată pe pasul selectat). Selectarea unui pas din network, timeline sau butonul „Explică acest pas” pune execuția pe pauză, fixează UI pe snapshot și deschide panoul de explicații AI. Resume repornește animația din pasul curent.

**API algo-service (student vizualizare)**
- POST `/api/sorting-networks/execute` — primește `{ values, algorithm: BITONIC|ODD_EVEN, direction: ASC|DESC }` și întoarce pași + metrici.
- GET `/api/sorting-networks/algorithms` — listează algoritmii disponibili (Bitonic, Odd-Even).
- GET `/api/sorting-networks/examples` — șiruri demo pentru UI (Random 8, Reverse 8, Sorted 8).

**Format fișier valori (upload în UI)**
- Upload-ul acceptă fișiere `.txt`, `.csv`, `.dat` cu minim 2 și maxim 100 valori.
- Separatori acceptați: virgulă, spațiu, punct și virgulă sau linie nouă.
- Exemple valide de conținut:
```
7,2,9,1,5,3
```
```
7 2 9 1 5 3
```
```
7;2;9;1;5;3
```

**How to Run**
- Prerequisites: Docker + Docker Compose.
- From project root run: `docker compose up --build`
- Wait for MySQL health checks to pass; Spring Boot services start afterward. Data is stored in Docker volumes (`user-db-data`, `auth-db-data`).

**User Seed Data (loaded automatically)**
`user-service` seeds the `users` table on startup via `data.sql` with `spring.jpa.defer-datasource-initialization=true`, so inserts run after Hibernate creates the schema.

| Role      | First    | Last    | Email                         | Phone         | Address                                |
|-----------|----------|---------|-------------------------------|---------------|----------------------------------------|
| ADMIN     | Alice    | Admin   | alice.admin@example.com       | +1 555 000 0001 | 123 Admin St, Tech City                |
| STUDENT   | Bob      | Builder | bob.student@example.com       | +1 555 000 0002 | 42 Campus Dr, College Town             |
| PROFESOR  | Carol    | Coder   | carol.prof@example.com        | +1 555 000 0003 | 9 Faculty Rd, University               |
| STUDENT   | Dave     | Data    | dave.student@example.com      | +1 555 000 0004 | 77 Library Ln, College Town            |
| ADMIN     | Eve      | Engineer| eve.admin@example.com         | +1 555 000 0005 | 500 Security Blvd, Tech City           |
| PROFESOR  | Frank    | Faculty | frank.prof@example.com        | +1 555 000 0006 | 11 Lecture Hall, University            |

**Credențiale autentificare (seed `authentification-service`)**
| Email                       | Parolă        | Rol       |
|-----------------------------|---------------|-----------|
| alice.admin@example.com     | Admin         | ADMIN     |
| bob.student@example.com     | Student123    | STUDENT   |
| carol.prof@example.com      | Professor123  | PROFESOR  |

**Key Endpoints**
- `GET /api/users` — list users
- `GET /api/users/{id}` — fetch one
- `POST /api/users` — create (body: AppUser JSON)
- `PUT /api/users/{id}` — update
- `DELETE /api/users/{id}` — remove

**Notes**
- Database credentials for `user-service` are set via environment in `compose.yaml` (`root` / `root`, DB `user_service`).
- Seed data lives in `user-service/src/main/resources/data.sql`; adjust values there if you need different defaults.
