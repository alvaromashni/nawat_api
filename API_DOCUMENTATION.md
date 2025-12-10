eu es# Smart Mesquita API - Documentação dos Endpoints

## Índice

1. [Visão Geral](#visão-geral)
2. [Autenticação](#autenticação)
3. [Endpoints de Autenticação](#endpoints-de-autenticação)
4. [Endpoints de Organizações](#endpoints-de-organizações)
5. [Endpoints de Doações PIX](#endpoints-de-doações-pix)
6. [Endpoints Administrativos - PIX](#endpoints-administrativos---pix)
7. [Endpoints Administrativos - Usuários](#endpoints-administrativos---usuários)
8. [Endpoints de Debug](#endpoints-de-debug)
9. [Modelos de Dados](#modelos-de-dados)
10. [Códigos de Status HTTP](#códigos-de-status-http)
11. [Rate Limiting](#rate-limiting)
12. [Changelog](#changelog)

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

Cria uma nova conta de usuário, que pode ser um usuário padrão ou um proprietário de organização (mesquita ou igreja).

**Endpoint:** `POST /api/v1/auth/register`

**Autenticação:** Não requerida

---

#### 1.1. Registro de Usuário Padrão

Ao registrar um usuário que não é proprietário de uma organização, basta omitir o campo `organization`.

**Request Body:**

```json
{
  "name": "João Silva",
  "email": "joao@example.com",
  "password": "senha123"
}
```

**Campos Obrigatórios:**

- `name` (string, 3-100 caracteres): Nome completo do usuário
- `email` (string, formato email): Email válido e único
- `password` (string, mínimo 6 caracteres): Senha de acesso

---

#### 1.2. Registro de Proprietário de Mesquita

Para registrar um usuário como proprietário de uma **mesquita**, inclua o objeto `organization` com o campo **`imaName`**.

**⚠️ IMPORTANTE:** Para mesquitas, você **DEVE** incluir o campo `imaName` (nome do Imã) e **NÃO** deve incluir o campo `priestName`.

**Request Body:**

```json
{
  "name": "Admin da Mesquita",
  "email": "admin@mesquita.com",
  "password": "senhaForte123",
  "organization": {
    "orgName": "Mesquita Central",
    "phoneNumber": "+5511987654321",
    "foundationDate": "2010-05-15",
    "administratorName": "Nome do Administrador",
    "cnpj": "12345678000199",
    "openingHours": "Segunda a Sexta: 9h-18h",
    "imaName": "Nome do Imã",
    "bankDetails": {
      "pixKey": "12345678000199",
      "pixKeyType": "CNPJ",
      "bankName": "Banco do Brasil",
      "agency": "0001",
      "accountNumber": "12345-6",
      "accountHolder": "Mesquita Central"
    },
    "addressDto": {
      "street": "Rua da Mesquita",
      "number": "100",
      "neighborhood": "Centro",
      "city": "São Paulo",
      "state": "SP",
      "zipcode": "01000-000",
      "complement": "Ao lado do mercado"
    }
  }
}
```

**Campos Obrigatórios (Mesquita):**

- `name` (string, 3-100 caracteres): Nome completo do usuário
- `email` (string, formato email): Email válido e único
- `password` (string, mínimo 6 caracteres): Senha de acesso
- `organization.orgName` (string): Nome da mesquita
- `organization.administratorName` (string): Nome do administrador responsável
- `organization.cnpj` (string, 14 dígitos): CNPJ válido da organização
- **`organization.imaName` (string): Nome do Imã** ← **Campo específico de mesquita**
- `organization.bankDetails.pixKey` (string): Chave PIX para recebimento de doações
- `organization.bankDetails.pixKeyType` (enum): Tipo da chave PIX (`EMAIL`, `PHONE`, `CPF`, `CNPJ`, `EVP`)
- `organization.bankDetails.accountHolder` (string): Nome do titular da conta bancária

---

#### 1.3. Registro de Proprietário de Igreja

Para registrar um usuário como proprietário de uma **igreja**, inclua o objeto `organization` com o campo **`priestName`**.

**⚠️ IMPORTANTE:** Para igrejas, você **DEVE** incluir o campo `priestName` (nome do Padre) e **NÃO** deve incluir o campo `imaName`.

**Request Body:**

```json
{
  "name": "Admin da Igreja",
  "email": "admin@igreja.com",
  "password": "outraSenhaForte456",
  "organization": {
    "orgName": "Igreja São Francisco",
    "phoneNumber": "+5521912345678",
    "foundationDate": "1990-01-20",
    "administratorName": "Padre Miguel",
    "cnpj": "98765432000188",
    "openingHours": "Todos os dias: 8h-20h",
    "priestName": "Padre Miguel",
    "bankDetails": {
      "pixKey": "admin@igreja.com",
      "pixKeyType": "EMAIL",
      "bankName": "Caixa Econômica Federal",
      "agency": "0002",
      "accountNumber": "98765-4",
      "accountHolder": "Igreja São Francisco"
    },
    "addressDto": {
      "street": "Avenida da Igreja",
      "number": "200",
      "neighborhood": "Bairro da Praça",
      "city": "Rio de Janeiro",
      "state": "RJ",
      "zipcode": "20000-000"
    }
  }
}
```

**Campos Obrigatórios (Igreja):**

- `name` (string, 3-100 caracteres): Nome completo do usuário
- `email` (string, formato email): Email válido e único
- `password` (string, mínimo 6 caracteres): Senha de acesso
- `organization.orgName` (string): Nome da igreja
- `organization.administratorName` (string): Nome do administrador responsável
- `organization.cnpj` (string, 14 dígitos): CNPJ válido da organização
- **`organization.priestName` (string): Nome do Padre** ← **Campo específico de igreja**
- `organization.bankDetails.pixKey` (string): Chave PIX para recebimento de doações
- `organization.bankDetails.pixKeyType` (enum): Tipo da chave PIX (`EMAIL`, `PHONE`, `CPF`, `CNPJ`, `EVP`)
- `organization.bankDetails.accountHolder` (string): Nome do titular da conta bancária

---

#### Campos Opcionais da Organização (Mesquita e Igreja):

- `organization.phoneNumber` (string): Telefone de contato
- `organization.foundationDate` (date): Data de fundação (formato `YYYY-MM-DD`)
- `organization.openingHours` (string): Horário de funcionamento
- `organization.bankDetails.bankName` (string): Nome do banco
- `organization.bankDetails.agency` (string): Agência bancária
- `organization.bankDetails.accountNumber` (string): Número da conta
- `organization.addressDto` (object): Endereço completo da organização
  - `street` (string): Nome da rua
  - `number` (string): Número
  - `neighborhood` (string): Bairro
  - `city` (string): Cidade
  - `state` (string): Estado (sigla, ex: SP, RJ)
  - `zipcode` (string): CEP
  - `complement` (string, opcional): Complemento

---

#### Diferenças entre Mesquita e Igreja

| Campo | Mesquita | Igreja |
|-------|----------|--------|
| **Campo específico** | `imaName` (obrigatório) | `priestName` (obrigatório) |
| **Campos comuns** | `orgName`, `cnpj`, `administratorName`, `bankDetails`, etc. | `orgName`, `cnpj`, `administratorName`, `bankDetails`, etc. |

**Regra:** A API detecta automaticamente o tipo de organização baseado no campo presente:
- Se o JSON contém `imaName` → cria uma **Mesquita** (Mosque)
- Se o JSON contém `priestName` → cria uma **Igreja** (Church)

---

#### Response (201 Created)

A resposta é a mesma para todos os tipos de registro e inclui os tokens de acesso e os dados básicos do usuário criado.

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "type": "Bearer",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Admin da Mesquita",
    "email": "admin@mesquita.com",
    "role": "ORG_OWNER",
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

## Endpoints de Organizações

**Autenticação:** Todos os endpoints requerem Bearer Token (usuário autenticado)

### 4.1. Obter Perfil da Organização

Retorna o perfil completo da organização associada ao usuário autenticado.

**Endpoint:** `GET /api/v1/users/me/organization-profile`

**Autenticação:** Requerida (Bearer Token - ORG_OWNER)

**Response (200 OK) - Mosque:**

```json
{
  "organizationDto": {
    "orgName": "Mesquita Central",
    "phoneNumber": "+5511987654321",
    "foundationDate": "2010-05-15",
    "administratorName": "João Silva",
    "cnpj": "12345678000199",
    "openingHours": "Segunda a Sexta: 5h-22h | Sábado e Domingo: 5h-23h",
    "bankDetails": {
      "pixKey": "12345678000199",
      "pixKeyType": "CNPJ",
      "bankName": "Banco do Brasil",
      "accountHolder": "Mesquita Central",
      "accountNumber": "12345-6",
      "agency": "0001",
      "isVerified": true,
      "verifiedAt": "2025-11-26T10:00:00"
    },
    "addressDto": {
      "street": "Rua das Flores",
      "number": "123",
      "neighborhood": "Centro",
      "city": "São Paulo",
      "state": "SP",
      "zipcode": "01234-567",
      "complement": "Próximo ao mercado"
    },
    "imaName": "Ima Ahmed"
  },
  "notificationsSettingsDto": {
    "emailNotifications": true,
    "smsNotifications": false,
    "pushNotifications": true
  }
}
```

**Response (200 OK) - Church:**

```json
{
  "organizationDto": {
    "orgName": "Igreja São Francisco",
    "phoneNumber": "+5511876543210",
    "foundationDate": "1995-08-20",
    "administratorName": "Padre Miguel",
    "cnpj": "98765432000188",
    "openingHours": "Segunda a Domingo: 6h-20h",
    "bankDetails": {
      "pixKey": "igreja@saofrancisco.com.br",
      "pixKeyType": "EMAIL",
      "bankName": "Caixa Econômica Federal",
      "accountHolder": "Igreja São Francisco",
      "accountNumber": "98765-4",
      "agency": "0123",
      "isVerified": true,
      "verifiedAt": "2025-11-25T14:30:00"
    },
    "addressDto": {
      "street": "Avenida Principal",
      "number": "456",
      "neighborhood": "Jardim Paulista",
      "city": "São Paulo",
      "state": "SP",
      "zipcode": "04567-890"
    },
    "priestName": "Padre Miguel"
  },
  "notificationsSettingsDto": {
    "emailNotifications": true,
    "smsNotifications": true,
    "pushNotifications": false
  }
}
```

**Response (404 Not Found):**

```json
{
  "timestamp": "2025-12-09T16:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usuário não possui organização associada",
  "path": "/api/v1/users/me/organization-profile"
}
```

---

### 4.2. Atualizar Perfil da Organização

Atualiza os dados da organização do usuário autenticado.

**Endpoint:** `PUT /api/v1/users/me/organization-profile`

**Autenticação:** Requerida (Bearer Token - ORG_OWNER)

**Request Body (Mosque):**

```json
{
  "organizationDto": {
    "orgName": "Mesquita Central Atualizada",
    "phoneNumber": "+5511987654321",
    "foundationDate": "2010-05-15",
    "administratorName": "João Silva Santos",
    "cnpj": "12345678000199",
    "openingHours": "Segunda a Domingo: 5h-23h",
    "bankDetails": {
      "pixKey": "12345678000199",
      "pixKeyType": "CNPJ",
      "bankName": "Banco do Brasil",
      "accountHolder": "Mesquita Central",
      "accountNumber": "12345-6",
      "agency": "0001"
    },
    "addressDto": {
      "street": "Rua das Flores",
      "number": "123",
      "neighborhood": "Centro",
      "city": "São Paulo",
      "state": "SP",
      "zipcode": "01234-567",
      "complement": "Próximo ao mercado municipal"
    },
    "imaName": "Ima Ahmed Ali"
  },
  "notificationsSettingsDto": {
    "emailNotifications": true,
    "smsNotifications": true,
    "pushNotifications": true
  }
}
```

**Request Body (Church):**

```json
{
  "organizationDto": {
    "orgName": "Igreja São Francisco de Assis",
    "phoneNumber": "+5511876543210",
    "foundationDate": "1995-08-20",
    "administratorName": "Padre Miguel",
    "cnpj": "98765432000188",
    "openingHours": "Segunda a Domingo: 6h-21h",
    "bankDetails": {
      "pixKey": "igreja@saofrancisco.com.br",
      "pixKeyType": "EMAIL",
      "bankName": "Caixa Econômica Federal",
      "accountHolder": "Igreja São Francisco",
      "accountNumber": "98765-4",
      "agency": "0123"
    },
    "addressDto": {
      "street": "Avenida Principal",
      "number": "456",
      "neighborhood": "Jardim Paulista",
      "city": "São Paulo",
      "state": "SP",
      "zipcode": "04567-890"
    },
    "priestName": "Padre Miguel Santos"
  },
  "notificationsSettingsDto": {
    "emailNotifications": true,
    "smsNotifications": true,
    "pushNotifications": false
  }
}
```

**Campos Obrigatórios:**

- `organizationDto.orgName` (string): Nome da organização
- `organizationDto.administratorName` (string): Nome do administrador
- `organizationDto.cnpj` (string, 14 dígitos): CNPJ válido com dígitos verificadores
- Para Mosque: `organizationDto.imaName` (string): Nome do Imã
- Para Church: `organizationDto.priestName` (string): Nome do Padre

**Campos Opcionais:**

- `organizationDto.phoneNumber` (string): Telefone (formato internacional)
- `organizationDto.foundationDate` (date): Data de fundação
- `organizationDto.openingHours` (string): Horário de funcionamento
- `organizationDto.bankDetails` (object): Dados bancários
- `organizationDto.addressDto` (object): Endereço completo
- `notificationsSettingsDto` (object): Configurações de notificação

**Validações Automáticas:**

- CNPJ deve ter 14 dígitos numéricos
- CNPJ deve ser válido (verificação de dígitos)
- Data de fundação não pode ser no futuro
- Telefone deve estar em formato válido (regex: `^\\+?[1-9]\\d{1,14}$`)

**Response (200 OK):**

```
(Sem conteúdo - apenas status 200)
```

**Response (400 Bad Request) - CNPJ Inválido:**

```json
{
  "timestamp": "2025-12-09T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "CNPJ inválido: 12345678000190",
  "path": "/api/v1/users/me/organization-profile"
}
```

**Response (400 Bad Request) - Dados Obrigatórios:**

```json
{
  "timestamp": "2025-12-09T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Nome da organização é obrigatório",
  "path": "/api/v1/users/me/organization-profile"
}
```

**Response (404 Not Found):**

```json
{
  "timestamp": "2025-12-09T16:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usuário não possui organização associada",
  "path": "/api/v1/users/me/organization-profile"
}
```

---

### 4.3. Obter Configurações de Notificação

Retorna as configurações de notificação do usuário.

**Endpoint:** `GET /api/v1/users/me/notification-settings`

**Autenticação:** Requerida (Bearer Token)

**Response (200 OK):**

```json
{
  "emailNotifications": true,
  "smsNotifications": false,
  "pushNotifications": true
}
```

---

### 4.4. Atualizar Configurações de Notificação

Atualiza as preferências de notificação do usuário.

**Endpoint:** `PUT /api/v1/users/me/notification-settings`

**Autenticação:** Requerida (Bearer Token)

**Request Body:**

```json
{
  "emailNotifications": true,
  "smsNotifications": true,
  "pushNotifications": false
}
```

**Response (200 OK):**

```
(Sem conteúdo - apenas status 200)
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

**Endpoint:** `POST /api/v1/admin/post/user`

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

**Endpoint:** `GET /api/v1/admin/get/user?email={email}`

**Autenticação:** Requerida (Bearer Token - apenas ADMIN)

**Query Parameters:**

- `email` (string): Email do usuário

**Response (200 OK):**

```
(Corpo da resposta varia, mas deve retornar o objeto do usuário)
```

---

### 15. Deletar Usuário por Email

Remove um usuário do sistema.

**Endpoint:** `DELETE /api/v1/admin/delete/user?email={email}`

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

**Endpoint:** `PUT /api/v1/admin/put/user?email={email}`

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

**Endpoint:** `POST /api/v1/admin/{userId}/verify-pix?proofUrl={url}`

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
  "name": "Admin da Mesquita",
  "email": "admin@mesquita.com",
  "password": "senhaForte123",
  "organization": {
    "orgName": "Mesquita Central",
    "administratorName": "Admin da Mesquita",
    "cnpj": "12345678000199",
    "imaName": "Imã da Mesquita",
    "bankDetails": {
      "pixKey": "12345678000199",
      "pixKeyType": "CNPJ",
      "accountHolder": "Mesquita Central"
    }
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
**Última Atualização:** 09/12/2025

---

## Changelog

### v1.1.0 - 09/12/2025

#### ✨ Novidades

**Endpoints de Organizações:**
- ✅ **Adicionado**: `GET /api/v1/users/me/organization-profile` - Obter perfil da organização
- ✅ **Adicionado**: `PUT /api/v1/users/me/organization-profile` - Atualizar perfil da organização
- ✅ **Adicionado**: `GET /api/v1/users/me/notification-settings` - Obter configurações de notificação
- ✅ **Adicionado**: `PUT /api/v1/users/me/notification-settings` - Atualizar configurações de notificação

**Suporte Multi-Organização:**
- ✅ Suporte completo para **Mesquitas** (Mosque) com campo `imaName`
- ✅ Suporte completo para **Igrejas** (Church) com campo `priestName`
- ✅ Herança polimórfica com estratégia JOINED

**Validações:**
- ✅ Validação de CNPJ com dígitos verificadores
- ✅ Validação de formato de telefone internacional
- ✅ Validação de data de fundação (não pode ser futura)
- ✅ Validação de campos obrigatórios (nome, administrador, CNPJ)

**Exceções Customizadas:**
- ✅ `OrganizationNotFoundException` (404)
- ✅ `InvalidCnpjException` (400)
- ✅ `InvalidOrganizationDataException` (400)
- ✅ `OrganizationAlreadyExistsException` (409)

**Repositório:**
- ✅ Criado `OrganizationRepository` com queries otimizadas
- ✅ Busca por CNPJ, cidade, estado, nome
- ✅ Filtro de organizações ativas
- ✅ Filtro de organizações aptas a receber pagamentos

#### 🔧 Alterações

**BREAKING CHANGES:**
- ⚠️ **Renomeado**: Endpoint `/api/v1/users/me/mosque-profile` → `/api/v1/users/me/organization-profile`
- ⚠️ **Removido**: Campo `cnpj` de `BankDetails` (agora está apenas em `Organization`)

**Melhorias:**
- ✅ Método `getMosqueProfile()` renomeado para `getOrganizationProfile()`
- ✅ Método `updateMosqueProfile()` renomeado para `updateOrganizationProfile()`
- ✅ Mapper `OrganizationMapper` com método `updateOrganizationFromDto()` corrigido
- ✅ Relacionamento User-Organization com cascade e helper methods
- ✅ Métodos helper em User: `getBankDetails()`, `hasValidPixKey()`

**Correções:**
- ✅ Corrigido modificador de acesso em `Mosque.imaName` (agora é `private`)
- ✅ Corrigido erro de coluna duplicada `cnpj` no mapeamento JPA
- ✅ Corrigido assinatura do método `updateUserFromDto` no mapper

#### 📚 Documentação

- ✅ Adicionada seção completa de **Endpoints de Organizações**
- ✅ Documentados 4 novos endpoints
- ✅ Exemplos de request/response para Mosque e Church
- ✅ Documentação de validações e exceções
- ✅ Adicionado changelog com histórico de versões
- ✅ **Melhorada documentação do endpoint `/register`:**
  - Separada em 3 subseções: Usuário Padrão, Mesquita e Igreja
  - Destacada diferença entre `imaName` (Mesquita) e `priestName` (Igreja)
  - Adicionada tabela comparativa entre Mesquita e Igreja
  - Explicado como a API detecta automaticamente o tipo de organização

---

### v1.0.0 - 26/11/2025

#### Lançamento Inicial

- ✅ Autenticação JWT com refresh tokens
- ✅ Endpoints de doações PIX
- ✅ Geração de QR Codes dinâmicos
- ✅ Rate limiting por IP e usuário
- ✅ Confirmação manual de pagamentos
- ✅ Endpoints administrativos
- ✅ Endpoints de debug

---

**Total de Endpoints:** 24 (4 novos na v1.1.0)