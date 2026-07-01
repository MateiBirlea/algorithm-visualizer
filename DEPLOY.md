# Deploy VPS Hetzner

Acest fisier descrie rularea pe VPS Ubuntu folosind configuratia de productie `compose.prod.yaml`.
Configuratia locala `compose.yaml` ramane separata pentru development local.

## 1. Conectare pe VPS

```bash
ssh root@91.98.194.75
```

## 2. Clonare proiect

```bash
cd /opt
git clone <repository-url> algorithm-visualizer
cd algorithm-visualizer
```

## 3. Pornire servicii

```bash
docker compose -f compose.prod.yaml up -d --build
```

## 4. Descarcare model Ollama

Ollama ruleaza ca serviciu Docker separat. Modelul folosit de aplicatie este `llama3.2`.

```bash
docker exec -it ollama ollama pull llama3.2
```

## 5. Verificare containere

```bash
docker ps
```

```bash
docker compose -f compose.prod.yaml ps
```

## 6. Verificare loguri

```bash
docker compose -f compose.prod.yaml logs -f
```

Pentru un singur serviciu:

```bash
docker compose -f compose.prod.yaml logs -f frontend
docker compose -f compose.prod.yaml logs -f authentification-service
docker compose -f compose.prod.yaml logs -f algo-service
docker compose -f compose.prod.yaml logs -f explanation-engine
docker compose -f compose.prod.yaml logs -f classroom-service
```

## 7. Acces aplicatie

Frontend:

```text
http://91.98.194.75:3000
```

API-uri expuse direct:

```text
authentification-service: http://91.98.194.75:8081
algo-service: http://91.98.194.75:8082
explanation-engine: http://91.98.194.75:8083
classroom-service: http://91.98.194.75:8084
user-service: http://91.98.194.75:8080
```

## 8. Oprire servicii

```bash
docker compose -f compose.prod.yaml down
```

## 9. Oprire si stergere volume persistente

Atentie: comanda sterge datele MySQL si modelele Ollama descarcate.

```bash
docker compose -f compose.prod.yaml down -v
```
