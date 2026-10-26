# Secure Booking Platform

Plataforma de agendamentos com **controle de acesso por perfil** e **proteção contra reservas duplicadas**.
Prestadores publicam horários, clientes reservam, e o sistema garante que **duas pessoas nunca ficam com o mesmo horário**,
mesmo clicando no mesmo milissegundo.

**Stack:** Java 17 · Spring Boot 3 · Spring Security (OAuth2 Resource Server + JWT) · Spring Data JPA · PostgreSQL · Docker · JUnit 5

## O que o projeto demonstra

### Autenticação e autorização (JWT)
- `POST /auth/login` devolve um token JWT assinado (HS256) com id, e-mail e perfil do usuário.
- API **stateless**: sem sessão no servidor; cada requisição envia `Authorization: Bearer <token>`.
- Perfis: `USER` (reserva), `PROVIDER` (publica horários), `ADMIN` (vê tudo e cancela qualquer reserva).
- Regras de acesso com `@PreAuthorize`. O cadastro público sempre cria `USER`: ninguém vira admin sozinho.
- Senhas com BCrypt; erros de login não revelam se o e-mail existe.

### Concorrência com Pessimistic Lock
`BookingService.book()` busca o horário com `SELECT ... FOR UPDATE` (`@Lock(PESSIMISTIC_WRITE)`).
Se 10 pessoas tentam reservar o mesmo horário ao mesmo tempo, o banco as coloca em fila: a primeira reserva,
as outras entram em seguida, enxergam o horário como `BOOKED` e recebem **409 Conflict**.

O teste `BookingConcurrencyTest` prova isso com 10 threads disputando um horário e exige exatamente 1 vencedor.

### Strategy Pattern para regras de agendamento
Cada tipo de horário tem sua regra em uma classe própria que implementa `BookingPolicy`:

| Tipo | Regra |
|---|---|
| `STANDARD` | 2h de antecedência; máximo de 3 reservas ativas |
| `PREMIUM` | 24h de antecedência; apenas 1 reserva premium ativa |
| `EXPRESS` | só nas próximas 24h; mínimo de 15 min de antecedência |

Para criar um novo tipo basta adicionar uma classe `@Component`: o `BookingService` não muda (princípio Aberto/Fechado).

### Camadas
```
web (controllers, DTOs, tratamento de erros)
 └─ service (regras de negócio e transações)
     ├─ policy (Strategy)
     └─ repository (Spring Data JPA)
         └─ domain (entidades)
security (JWT, perfis) · config (relógio, seed)
```

## Como rodar

```bash
docker compose up --build
```
API em `http://localhost:8080`. Sobem automaticamente um admin (`admin@booking.com` / `Admin@12345`)
e um prestador (`provider@booking.com` / `Provider@12345`). **Troque as senhas e o `JWT_SECRET` fora do ambiente de estudo.**

## Passo a passo

```bash
# 1. Login do prestador e criação de um horário
TOKEN_P=$(curl -s -X POST localhost:8080/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"provider@booking.com","password":"Provider@12345"}' | sed 's/.*"accessToken":"\([^"]*\)".*/\1/')

curl -X POST localhost:8080/slots -H "Authorization: Bearer $TOKEN_P" -H 'Content-Type: application/json' \
  -d '{"startTime":"2030-06-10T10:00:00","endTime":"2030-06-10T11:00:00","type":"STANDARD","price":120.00}'

# 2. Cliente se cadastra, faz login e reserva
curl -X POST localhost:8080/auth/register -H 'Content-Type: application/json' \
  -d '{"name":"Ana","email":"ana@email.com","password":"senha12345"}'
TOKEN_U=$(curl -s -X POST localhost:8080/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"ana@email.com","password":"senha12345"}' | sed 's/.*"accessToken":"\([^"]*\)".*/\1/')

curl localhost:8080/slots -H "Authorization: Bearer $TOKEN_U"                       # horários livres
curl -X POST localhost:8080/slots/1/book -H "Authorization: Bearer $TOKEN_U"        # 201 Created
curl -X POST localhost:8080/slots/1/book -H "Authorization: Bearer $TOKEN_U"        # 409 (já reservado)

# 3. Cliente tentando criar horário => 403 Forbidden
curl -X POST localhost:8080/slots -H "Authorization: Bearer $TOKEN_U" -H 'Content-Type: application/json' \
  -d '{"startTime":"2030-06-11T10:00:00","endTime":"2030-06-11T11:00:00","type":"STANDARD","price":50}'
```

## Endpoints

| Método | Rota | Quem |
|---|---|---|
| POST | `/auth/register`, `/auth/login` | público |
| GET | `/slots` (horários livres) | autenticado |
| POST | `/slots` | PROVIDER, ADMIN |
| GET | `/slots/mine` | PROVIDER, ADMIN |
| POST | `/slots/{id}/book` | USER |
| GET | `/bookings/me` | USER |
| DELETE | `/bookings/{id}` (até 1h antes) | dono da reserva ou ADMIN |
| GET | `/admin/bookings` | ADMIN |

## Testes

```bash
mvn test
```
- `BookingPolicyTest`: regras de cada estratégia.
- `BookingConcurrencyTest`: 10 threads disputando o mesmo horário (usa H2 em memória, sem precisar de Docker).

## Próximos passos possíveis

- Refresh tokens e revogação de tokens.
- Migrações com Flyway e testes com Testcontainers (PostgreSQL real).
- Notificações por e-mail ao confirmar/cancelar reservas.
