# Smart Mesquita API - Documentação dos Endpoints

## Índice

1. [Visão Geral](#visão-geral)
2. [Autenticação](#autenticação)
3. [Endpoints de Autenticação](#endpoints-de-autenticação)
4. [Endpoints de Doações PIX](#endpoints-de-doações-pix)
5. [Endpoints Administrativos - PIX](#endpoints-administrativos---pix)
6. [Endpoints Administrativos - Usuários](#endpoints-administrativos---usuários)
7. [Endpoints de Debug](#endpoints-de-debug)
8. [Modelos de Dados](#modelos-de-dados)
9. [Códigos de Status HTTP](#códigos-de-status-http)
10. [Rate Limiting](#rate-limiting)

---

## Visão Geral

**Base URL:** `http://localhost:8080` (desenvolvimento)

**Content-Type:** `application/json`

**Autenticação:** Bearer Token (JWT)

### Headers Padrão

```http
Content-Type: application/json
Authorization: Bearer {token}
```

---

## Autenticação

A API utiliza JWT (JSON Web Tokens) para autenticação. Após fazer login ou registro, você receberá:

- `token`: Token de acesso (válido por tempo limitado)
- `refreshToken`: Token para renovar o acesso
- `type`: Tipo do token (sempre "Bearer")

### Como usar o token

Inclua o token no header `Authorization` de todas as requisições protegidas:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Endpoints de Autenticação

### 1. Registrar Novo Usuário

Cria uma nova conta de usuário.

**Endpoint:** `POST /api/v1/auth/register`

**Autenticação:** Não requerida

**Request Body:**

```json
{
  "name": "João Silva",
  "email": "joao@example.com",
  "password": "senha123",
  "role": "USER",
  "bankDetails": {
    "pixKey": "joao@example.com",
    "pixKeyType": "EMAIL",
    "bankName": "Banco do Brasil",
    "accountHolder": "João Silva",
    "cnpj": "12345678000199",
    "bankBranch": "0001",
    "accountNumber": "12345-6"
  }
}
```

**Campos Obrigatórios:**

- `name` (string, 3-100 caracteres): Nome completo
- `email` (string, formato email): Email válido
- `password` (string, mínimo 6 caracteres): Senha
- `role` (enum): `ADMIN`, `STAFF`, `MESQUITA_OWNER`, ou `USER`

**Campos Opcionais:**

- `bankDetails` (object): Dados bancários (obrigatório apenas para `MESQUITA_OWNER`)
  - `pixKey` (string): Chave PIX
  - `pixKeyType` (enum): `EMAIL`, `PHONE`, `CPF`, `CNPJ`, `EVP`
  - `bankName` (string): Nome do banco
  - `accountHolder` (string): Titular da conta
  - `cnpj` (string): CNPJ da mesquita
  - `bankBranch` (string): Agência
  - `accountNumber` (string): Número da conta

**Response (201 Created):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "type": "Bearer",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "João Silva",
    "email": "joao@example.com",
    "role": "USER",
    "isActive": true,
    "hasPixKey": true
  }
}
```

---

### 2. Login

Autentica um usuário existente.

**Endpoint:** `POST /api/v1/auth/login`

**Autenticação:** Não requerida

**Rate Limit:** 5 requisições por 60 segundos (por IP)

**Request Body:**

```json
{
  "email": "joao@example.com",
  "password": "senha123"
}
```

**Campos Obrigatórios:**

- `email` (string, formato email): Email do usuário
- `password` (string): Senha

**Response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "type": "Bearer",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "João Silva",
    "email": "joao@example.com",
    "role": "USER",
    "isActive": true,
    "hasPixKey": true
  }
}
```

**Response (401 Unauthorized):**

```json
{
  "timestamp": "2025-11-26T14:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciais inválidas",
  "path": "/api/v1/auth/login"
}
```

---

### 3. Verificar Token

Valida se o token JWT é válido.

**Endpoint:** `GET /api/v1/auth/verify`

**Autenticação:** Requerida (Bearer Token)

**Response (200 OK):**

```
(Sem conteúdo - apenas status 200)
```

**Response (401 Unauthorized):**

```json
{
  "timestamp": "2025-11-26T14:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token inválido ou expirado"
}
```

---

### 4. Renovar Token (Refresh)

Gera um novo par de tokens usando o refresh token.

**Endpoint:** `POST /api/v1/auth/refresh`

**Autenticação:** Não requerida (usa refreshToken no body)

**Request Body:**

```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Campos Obrigatórios:**

- `token` (string): Refresh token recebido no login/registro

**Response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "660e8400-e29b-41d4-a716-446655440001",
  "type": "Bearer",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "João Silva",
    "email": "joao@example.com",
    "role": "USER",
    "isActive": true,
    "hasPixKey": true
  }
}
```

---

## Endpoints de Doações PIX

### 5. Criar Cobrança PIX

Cria uma nova cobrança PIX com QR Code.

**Endpoint:** `POST /api/v1/donations/{localId}/pix`

**Autenticação:** Requerida (Bearer Token)

**Rate Limit:** 1 requisição por 10 segundos (por usuário)

**Path Parameters:**

- `localId` (string): ID local da doação (gerado pelo totem/cliente)

**Request Body:**

```json
{
  "amountCents": 5000,
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
  "expiresMinutes": 30
}
```

**Campos Obrigatórios:**

- `amountCents` (integer, 100-1000000): Valor em centavos (ex: 5000 = R$50,00)
- `idempotencyKey` (string, max 100): UUID único para prevenir duplicatas

**Campos Opcionais:**

- `expiresMinutes` (integer, 1-60): Tempo de expiração em minutos (padrão: 10)

**Response (201 Created):**

```json
{
  "txid": "TX123456789ABCDEF",
  "qrPayload": "00020126580014br.gov.bcb.pix...",
  "qrImageBase64": "iVBORw0KGgoAAAANSUhEUgAA...",
  "expiresAt": 1732642800000,
  "amountCents": 5000
}
```

**Descrição dos campos:**

- `txid` (string): ID da transação PIX
- `qrPayload` (string): String Pix Copia e Cola (Brcode)
- `qrImageBase64` (string): Imagem do QR Code em Base64
- `expiresAt` (long): Timestamp de expiração (milissegundos desde epoch)
- `amountCents` (integer): Valor em centavos

**Response (400 Bad Request):**

```json
{
  "timestamp": "2025-11-26T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Usuário não possui chave PIX verificada",
  "path": "/api/v1/donations/LOCAL-123/pix"
}
```

---

### 6. Consultar Cobrança por Local ID

Busca uma cobrança pelo ID local.

**Endpoint:** `GET /api/v1/donations/{localId}`

**Autenticação:** Requerida (Bearer Token)

**Path Parameters:**

- `localId` (string): ID local da doação

**Response (200 OK):**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "localDonationId": "LOCAL-123",
  "txid": "TX123456789ABCDEF",
  "amountCents": 5000,
  "status": "PENDING",
  "qrPayload": "00020126580014br.gov.bcb.pix...",
  "qrImageBase64": "iVBORw0KGgoAAAANSUhEUgAA...",
  "expiresAt": "2025-11-26T15:00:00",
  "createdAt": "2025-11-26T14:30:00",
  "userName": "João Silva",
  "receiptImageUrl": null
}
```

**Response (404 Not Found):**

```json
{
  "timestamp": "2025-11-26T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Cobrança não encontrada",
  "path": "/api/v1/donations/LOCAL-999"
}
```

---

### 7. Consultar Cobrança por TXID

Busca uma cobrança pelo Transaction ID do PIX.

**Endpoint:** `GET /api/v1/donations/txid/{txid}`

**Autenticação:** Requerida (Bearer Token)

**Path Parameters:**

- `txid` (string): Transaction ID da cobrança PIX

**Response (200 OK):**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "localDonationId": "LOCAL-123",
  "txid": "TX123456789ABCDEF",
  "amountCents": 5000,
  "status": "PAID",
  "qrPayload": "00020126580014br.gov.bcb.pix...",
  "qrImageBase64": "iVBORw0KGgoAAAANSUhEUgAA...",
  "expiresAt": "2025-11-26T15:00:00",
  "createdAt": "2025-11-26T14:30:00",
  "userName": "João Silva",
  "receiptImageUrl": "https://example.com/receipt.jpg"
}
```

---

### 8. Confirmar Cobrança Manualmente

Confirma manualmente uma cobrança (apenas STAFF/ADMIN).

**Endpoint:** `POST /api/v1/donations/{localId}/confirm-manual`

**Autenticação:** Requerida (Bearer Token - STAFF ou ADMIN)

**Path Parameters:**

- `localId` (string): ID local da doação

**Request Body:**

```json
{
  "receiptUrl": "https://example.com/receipt.jpg",
  "notes": "Pagamento confirmado via extrato bancário"
}
```

**Campos Obrigatórios:**

- `receiptUrl` (string): URL do comprovante

**Campos Opcionais:**

- `notes` (string): Observações sobre a confirmação

**Response (200 OK):**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "localDonationId": "LOCAL-123",
  "txid": "TX123456789ABCDEF",
  "amountCents": 5000,
  "status": "CONFIRMED_MANUALLY",
  "qrPayload": "00020126580014br.gov.bcb.pix...",
  "qrImageBase64": "iVBORw0KGgoAAAANSUhEUgAA...",
  "expiresAt": "2025-11-26T15:00:00",
  "createdAt": "2025-11-26T14:30:00",
  "userName": "João Silva",
  "receiptImageUrl": "https://example.com/receipt.jpg"
}
```

---

## Endpoints Administrativos - PIX

**Autenticação:** Todos os endpoints requerem role `ADMIN` ou `STAFF`

### 9. Expirar Cobranças Antigas

Força a expiração de cobranças pendentes antigas.

**Endpoint:** `POST /api/admin/pix/expire-old-charges`

**Autenticação:** Requerida (Bearer Token - apenas ADMIN)

**Response (200 OK):**

```json
{
  "message": "Cobranças expiradas com sucesso",
  "expiredCount": 15
}
```

---

### 10. Buscar Cobrança por ID (Admin)

Busca uma cobrança específica por ID interno.

**Endpoint:** `GET /api/admin/pix/charges/{chargeId}`

**Autenticação:** Requerida (Bearer Token - ADMIN ou STAFF)

**Path Parameters:**

- `chargeId` (UUID): ID interno da cobrança

**Response (200 OK):**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "localDonationId": "LOCAL-123",
  "txid": "TX123456789ABCDEF",
  "amountCents": 5000,
  "status": "PENDING",
  "qrPayload": "00020126580014br.gov.bcb.pix...",
  "qrImageBase64": "iVBORw0KGgoAAAANSUhEUgAA...",
  "expiresAt": "2025-11-26T15:00:00",
  "createdAt": "2025-11-26T14:30:00",
  "userName": "João Silva",
  "receiptImageUrl": null
}
```

**Status:** ⚠️ Endpoint em desenvolvimento (retorna 200 vazio)

---

### 11. Atualizar Status de Cobrança

Altera manualmente o status de uma cobrança (use com cautela).

**Endpoint:** `PATCH /api/admin/pix/charges/{chargeId}/status`

**Autenticação:** Requerida (Bearer Token - apenas ADMIN)

**Path Parameters:**

- `chargeId` (UUID): ID interno da cobrança

**Request Body:**

```json
{
  "status": "PAID",
  "reason": "Pagamento confirmado via extrato bancário"
}
```

**Response (200 OK):**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "localDonationId": "LOCAL-123",
  "txid": "TX123456789ABCDEF",
  "amountCents": 5000,
  "status": "PAID",
  "qrPayload": "00020126580014br.gov.bcb.pix...",
  "qrImageBase64": "iVBORw0KGgoAAAANSUhEUgAA...",
  "expiresAt": "2025-11-26T15:00:00",
  "createdAt": "2025-11-26T14:30:00",
  "userName": "João Silva",
  "receiptImageUrl": null
}
```

**Status:** ⚠️ Endpoint em desenvolvimento (retorna 200 vazio)

---

### 12. Importar Extrato Bancário

Importa um extrato bancário para reconciliação (futuro).

**Endpoint:** `POST /api/admin/pix/import-extract`

**Autenticação:** Requerida (Bearer Token - apenas ADMIN)

**Response (200 OK):**

```json
{
  "message": "Funcionalidade em desenvolvimento"
}
```

**Status:** ⚠️ Endpoint em desenvolvimento

---

## Endpoints Administrativos - Usuários

**Autenticação:** Todos os endpoints requerem role `ADMIN`

### 13. Criar Usuário

Cria um novo usuário (admin).

**Endpoint:** `POST /api/v1/users/post/user`

**Autenticação:** Requerida (Bearer Token - apenas ADMIN)

**Request Body:**

```json
{
  "name": "Maria Santos",
  "email": "maria@example.com",
  "password": "senha123",
  "role": "STAFF",
  "enabled": true,
  "bankDetails": {
    "pixKey": "maria@example.com",
    "pixKeyType": "EMAIL"
  }
}
```

**Response (200 OK):**

```
(Sem conteúdo - apenas status 200)
```

---

### 14. Buscar Usuário por Email

Busca um usuário pelo email.

**Endpoint:** `GET /api/v1/users/get/user?email={email}`

**Autenticação:** Requerida (Bearer Token - apenas ADMIN)

**Query Parameters:**

- `email` (string): Email do usuário

**Response (200 OK):**

```
(Sem conteúdo - apenas status 200)
```

**Nota:** Endpoint retorna 200 vazio (implementação pode precisar ajuste)

---

### 15. Deletar Usuário por Email

Remove um usuário do sistema.

**Endpoint:** `DELETE /api/v1/users/delete/user?email={email}`

**Autenticação:** Requerida (Bearer Token - apenas ADMIN)

**Query Parameters:**

- `email` (string): Email do usuário a ser deletado

**Response (200 OK):**

```
(Sem conteúdo - apenas status 200)
```

---

### 16. Atualizar Usuário

Atualiza dados de um usuário existente.

**Endpoint:** `PUT /api/v1/users/put/user?email={email}`

**Autenticação:** Requerida (Bearer Token - apenas ADMIN)

**Query Parameters:**

- `email` (string): Email do usuário a ser atualizado

**Request Body:**

```json
{
  "name": "Maria Santos Silva",
  "email": "maria.silva@example.com"
}
```

**Response (200 OK):**

```
(Sem conteúdo - apenas status 200)
```

---

### 17. Verificar Chave PIX de Usuário

Aprova/verifica a chave PIX de um usuário.

**Endpoint:** `POST /api/v1/users/{userId}/verify-pix?proofUrl={url}`

**Autenticação:** Requerida (Bearer Token - apenas ADMIN)

**Path Parameters:**

- `userId` (UUID): ID do usuário

**Query Parameters:**

- `proofUrl` (string, opcional): URL do comprovante de titularidade

**Response (200 OK):**

```json
"Chave PIX verificada com sucesso para o usuário 123e4567-e89b-12d3-a456-426614174000"
```

---

## Endpoints de Debug

**Autenticação:** Requerida (qualquer usuário autenticado)

### 18. Verificar Dados do Usuário Atual

Retorna informações completas do usuário autenticado.

**Endpoint:** `GET /api/debug/me`

**Autenticação:** Requerida (Bearer Token)

**Response (200 OK):**

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "João Silva",
  "email": "joao@example.com",
  "role": "MESQUITA_OWNER",
  "isActive": true,
  "bankDetails": {
    "pixKey": "joao@example.com",
    "pixKeyType": "EMAIL",
    "bankName": "Banco do Brasil",
    "accountHolder": "João Silva",
    "isVerified": true,
    "verifiedAt": "2025-11-26T10:00:00"
  },
  "hasPixKey": true,
  "hasValidPixKey": true,
  "canReceivePayments": true
}
```

---

### 19. Verificar Chave PIX

Verifica o status da chave PIX do usuário autenticado.

**Endpoint:** `GET /api/debug/pix-key`

**Autenticação:** Requerida (Bearer Token)

**Response (200 OK) - Chave Verificada:**

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "joao@example.com",
  "pixKey": "joao@example.com",
  "pixKeyType": "EMAIL",
  "isVerified": true,
  "status": "VERIFIED",
  "message": "Chave PIX válida e verificada",
  "hasPixKey": true,
  "canCreateCharges": true,
  "success": "Você pode criar cobranças PIX!"
}
```

**Response (200 OK) - Chave Não Verificada:**

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "joao@example.com",
  "pixKey": "joao@example.com",
  "pixKeyType": "EMAIL",
  "isVerified": false,
  "status": "NOT_VERIFIED",
  "message": "Chave PIX cadastrada mas não verificada",
  "hasPixKey": true,
  "canCreateCharges": false,
  "warning": "Você precisa verificar a chave PIX antes de receber pagamentos"
}
```

**Response (200 OK) - Sem Chave PIX:**

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "joao@example.com",
  "status": "NO_PIX_KEY",
  "message": "Chave PIX não cadastrada",
  "hasPixKey": false,
  "canCreateCharges": false
}
```

---

### 20. Verificar Chave PIX (Simulação)

Simula a verificação de uma chave PIX para testes.

**Endpoint:** `POST /api/debug/verify-pix`

**Autenticação:** Requerida (Bearer Token)

**Response (200 OK):**

```json
{
  "message": "Chave PIX verificada com sucesso (simulação)",
  "pixKey": "joao@example.com",
  "isVerified": true,
  "warning": "ATENÇÃO: Esta é uma verificação simulada para testes!"
}
```

**Response (400 Bad Request):**

```json
{
  "error": "Usuário não tem chave PIX cadastrada"
}
```

---

## Modelos de Dados

### UserRole (Enum)

```
ADMIN          - Administrador do sistema (acesso total)
STAFF          - Staff/funcionário (pode validar comprovantes)
MESQUITA_OWNER - Dono de mesquita (recebe doações)
USER           - Usuário comum
```

### PixKeyType (Enum)

```
EMAIL  - E-mail
PHONE  - Telefone celular (+55DDNNNNNNNNN)
CPF    - CPF (11 dígitos)
CNPJ   - CNPJ (14 dígitos)
EVP    - Chave aleatória (UUID)
```

### PixChargeStatus (Enum)

```
PENDING            - Cobrança criada, aguardando pagamento
PAID               - Pagamento detectado automaticamente
CONFIRMED_MANUALLY - Pagamento confirmado manualmente por staff
EXPIRED            - Cobrança expirou sem pagamento
CANCELLED          - Cobrança cancelada
```

---

## Códigos de Status HTTP

### Sucesso

- `200 OK` - Requisição bem-sucedida
- `201 Created` - Recurso criado com sucesso

### Erro do Cliente

- `400 Bad Request` - Dados inválidos ou requisição malformada
- `401 Unauthorized` - Não autenticado ou token inválido
- `403 Forbidden` - Sem permissão para acessar o recurso
- `404 Not Found` - Recurso não encontrado
- `429 Too Many Requests` - Rate limit excedido

### Erro do Servidor

- `500 Internal Server Error` - Erro interno do servidor

---

## Rate Limiting

Alguns endpoints possuem limitação de taxa para prevenir abuso:

### Endpoint de Login

- **Limite:** 5 requisições por 60 segundos
- **Tipo:** Por IP
- **Header de Resposta:**
  ```
  X-RateLimit-Limit: 5
  X-RateLimit-Remaining: 4
  X-RateLimit-Reset: 1732642800
  ```

### Endpoint de Criação de Cobrança PIX

- **Limite:** 1 requisição por 10 segundos
- **Tipo:** Por usuário autenticado
- **Header de Resposta:**
  ```
  X-RateLimit-Limit: 1
  X-RateLimit-Remaining: 0
  X-RateLimit-Reset: 1732642810
  ```

### Response quando o limite é excedido (429)

```json
{
  "timestamp": "2025-11-26T14:30:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit excedido. Tente novamente em 10 segundos.",
  "path": "/api/v1/donations/LOCAL-123/pix"
}
```

---

## Exemplo de Fluxo Completo

### 1. Registrar Usuário Dono de Mesquita

```bash
POST /api/v1/auth/register
{
  "name": "Mesquita Central",
  "email": "contato@mesquitacentral.com",
  "password": "senhaSegura123",
  "role": "MESQUITA_OWNER",
  "bankDetails": {
    "pixKey": "12345678000199",
    "pixKeyType": "CNPJ",
    "bankName": "Banco do Brasil",
    "accountHolder": "Mesquita Central",
    "cnpj": "12345678000199",
    "accountNumber": "12345-6"
  }
}
```

### 2. Verificar Chave PIX (por um Admin)

```bash
POST /api/v1/users/123e4567-e89b-12d3-a456-426614174000/verify-pix?proofUrl=https://example.com/proof.pdf
Authorization: Bearer {admin_token}
```

### 3. Login do Usuário

```bash
POST /api/v1/auth/login
{
  "email": "contato@mesquitacentral.com",
  "password": "senhaSegura123"
}
```

### 4. Criar Cobrança PIX

```bash
POST /api/v1/donations/DOA-001/pix
Authorization: Bearer {token}
{
  "amountCents": 10000,
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
  "expiresMinutes": 30
}
```

### 5. Consultar Status da Cobrança

```bash
GET /api/v1/donations/DOA-001
Authorization: Bearer {token}
```

### 6. Confirmar Manualmente (se necessário)

```bash
POST /api/v1/donations/DOA-001/confirm-manual
Authorization: Bearer {staff_token}
{
  "receiptUrl": "https://example.com/receipt.jpg",
  "notes": "Confirmado via extrato bancário"
}
```

---

## Notas Importantes

1. **Idempotência:** Sempre use uma `idempotencyKey` única ao criar cobranças PIX para evitar duplicatas
2. **Expiração:** Cobranças PIX expiram automaticamente após o tempo configurado
3. **Verificação:** Usuários devem ter a chave PIX verificada antes de criar cobranças
4. **Rate Limiting:** Respeite os limites de taxa para evitar bloqueios temporários
5. **Segurança:** Nunca exponha tokens em logs ou URLs. Use HTTPS em produção
6. **Timestamps:** Todos os timestamps são em UTC. Converta para timezone local no frontend

---

## Suporte

Para dúvidas ou problemas com a API, entre em contato com a equipe de desenvolvimento.

**Versão da API:** v1
**Última Atualização:** 26/11/2025