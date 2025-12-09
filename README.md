# 🕌 Smart Mesquita API

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13+-blue?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?style=for-the-badge&logo=redis)
![Maven](https://img.shields.io/badge/Maven-3.9+-purple?style=for-the-badge&logo=apache-maven)

**API REST para gerenciamento de doações PIX em organizações religiosas**

[📖 Documentação da API](./API_DOCUMENTATION.md) • [🧪 Guia de Testes](./TESTES.md) • [🐛 Reportar Bug](https://github.com/seu-usuario/smartMesquitaApi/issues)

</div>

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Executando o Projeto](#-executando-o-projeto)
- [Testes](#-testes)
- [Documentação da API](#-documentação-da-api)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Contribuindo](#-contribuindo)
- [Licença](#-licença)

---

## 🎯 Sobre o Projeto

**Smart Mesquita API** é uma solução completa para gestão de doações via PIX para organizações religiosas (mesquitas e igrejas). O sistema permite o gerenciamento de organizações, autenticação segura, geração de QR Codes PIX e controle completo de doações.

### 🌟 Principais Características

- 🔐 **Autenticação JWT** com refresh tokens
- 🏢 **Multi-organização** (suporte a Mesquitas e Igrejas)
- 💰 **Doações PIX** com geração dinâmica de QR Codes
- ✅ **Validação de CNPJ** com algoritmo de dígitos verificadores
- 🔒 **Criptografia** de dados sensíveis (chaves PIX, dados bancários)
- 🚦 **Rate Limiting** por IP e usuário via AOP
- 📊 **Auditoria** de transações e alterações
- 🎨 **Herança Polimórfica** para diferentes tipos de organizações
- ⚡ **Cache Redis** para performance
- 🛡️ **Validações** completas com Bean Validation

---

## ✨ Funcionalidades

### Autenticação e Usuários
- ✅ Registro de usuários com diferentes roles (ADMIN, STAFF, ORG_OWNER, USER)
- ✅ Login com JWT (validade: 24h)
- ✅ Refresh tokens (validade: 30 dias)
- ✅ Verificação de tokens
- ✅ Gerenciamento de perfil de organização

### Organizações
- ✅ Cadastro de Mesquitas e Igrejas
- ✅ Validação de CNPJ com dígitos verificadores
- ✅ Gerenciamento de dados bancários
- ✅ Validação de chaves PIX
- ✅ Busca por cidade, estado, nome

### Doações PIX
- ✅ Criação de cobranças com QR Code
- ✅ Geração de EMV/Brcode (Pix Copia e Cola)
- ✅ Imagens de QR Code em Base64
- ✅ Expiração automática de cobranças
- ✅ Confirmação manual por staff
- ✅ Consulta por ID local ou TXID
- ✅ Idempotência para prevenir duplicatas

### Segurança
- ✅ Rate limiting (login: 5/min, doações: 1/10s)
- ✅ Criptografia AES para dados sensíveis
- ✅ CORS configurável
- ✅ Validação de roles e permissões
- ✅ Spring Security integrado

---

## 🏗️ Arquitetura

### Padrões Utilizados

- **Layered Architecture** - Separação em camadas (Controller, Service, Repository, Domain)
- **DTO Pattern** - Isolamento de modelos de domínio e transferência
- **Repository Pattern** - Abstração de acesso a dados com Spring Data JPA
- **AOP (Aspect-Oriented Programming)** - Rate limiting via aspectos
- **Herança Polimórfica** - `InheritanceType.JOINED` para Organization (Church/Mosque)
- **Builder Pattern** - Construção fluente de objetos
- **Mapper Pattern** - Conversão entre DTOs e Entities

### Camadas do Sistema

```
┌─────────────────────────────────────┐
│     Controllers (REST API)          │ ← Endpoints REST
├─────────────────────────────────────┤
│     Services (Business Logic)       │ ← Lógica de negócio
├─────────────────────────────────────┤
│     Repositories (Data Access)      │ ← Spring Data JPA
├─────────────────────────────────────┤
│     Entities (Domain Models)        │ ← Models JPA
├─────────────────────────────────────┤
│     Infrastructure                  │ ← Utils, Config, Security
└─────────────────────────────────────┘
```

---

## 🛠️ Tecnologias

### Core
- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.6** - Framework principal
- **Maven** - Gerenciamento de dependências

### Persistência
- **PostgreSQL 13+** - Banco de dados relacional
- **Spring Data JPA** - ORM (Hibernate)
- **Flyway** - Migrations de banco de dados

### Cache e Performance
- **Redis 7** - Cache em memória
- **Spring Session Data Redis** - Gerenciamento de sessões

### Segurança
- **Spring Security** - Framework de segurança
- **JWT (java-jwt)** - Tokens de autenticação
- **BCrypt** - Hash de senhas
- **AES** - Criptografia de dados sensíveis

### QR Code e PIX
- **ZXing (3.5.3)** - Geração de QR Codes
- **EMV Generator** - Payloads PIX/Brcode

### Utilitários
- **Lombok** - Redução de boilerplate
- **Bean Validation** - Validações

### Testes
- **JUnit 5** - Framework de testes
- **Spring Boot Test** - Testes de integração
- **Mockito** - Mocks

---

## 📦 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 21** ou superior ([Download](https://adoptium.net/))
- **Maven 3.9+** (ou use o `./mvnw` incluído)
- **PostgreSQL 13+** ([Download](https://www.postgresql.org/download/))
- **Redis 7+** ([Download](https://redis.io/download/) ou via Docker)
- **Git** ([Download](https://git-scm.com/))

### Verificando as versões

```bash
java -version       # Java 21.x.x
mvn -version        # Maven 3.9.x
psql --version      # PostgreSQL 13.x
redis-server --version  # Redis 7.x
```

---

## 🚀 Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/smartMesquitaApi.git
cd smartMesquitaApi
```

### 2. Configure o PostgreSQL

Crie o banco de dados:

```sql
CREATE DATABASE smartMesquita;
CREATE USER smartuser WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE smartMesquita TO smartuser;
```

### 3. Inicie o Redis

**Opção 1 - Docker (Recomendado):**

```bash
docker-compose up -d
```

**Opção 2 - Local:**

```bash
redis-server
```

### 4. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto (ou configure em `application.properties`):

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/smartMesquita
DATABASE_USERNAME=smartuser
DATABASE_PASSWORD=your_password

# JWT
JWT_SECRET=your-super-secret-jwt-key-minimum-256-bits-change-in-production

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://app.smartmesquita.com

# Redis (se não estiver no padrão)
REDIS_HOST=localhost
REDIS_PORT=6379
```

### 5. Compile o projeto

```bash
./mvnw clean install
```

ou

```bash
mvn clean install
```

---

## ⚙️ Configuração

### application.properties

O arquivo `src/main/resources/application.properties` contém as configurações principais:

```properties
# Application
spring.application.name=smartMesquitaApi

# Database
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5433/smartMesquita}
spring.datasource.username=${DATABASE_USERNAME:postgres}
spring.datasource.password=${DATABASE_PASSWORD:Alvinho@25}
spring.jpa.hibernate.ddl-auto=create

# JWT
jwt.secret=${JWT_SECRET:dev-secret-key-change-in-production-minimum-256-bits-required}

# CORS
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000}

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# Rate Limit
ratelimit.default.limit=10
ratelimit.default.duration=1
ratelimit.default.unit=MINUTES
```

### Profiles

- **dev** - Desenvolvimento (padrão)
- **prod** - Produção (configure variáveis de ambiente)

---

## ▶️ Executando o Projeto

### Modo Desenvolvimento

```bash
./mvnw spring-boot:run
```

ou

```bash
mvn spring-boot:run
```

### Modo Produção

```bash
./mvnw clean package
java -jar target/smartMesquitaApi-0.0.1-SNAPSHOT.jar
```

### Docker (Futuro)

```bash
docker build -t smartmesquita-api .
docker run -p 8080:8080 smartmesquita-api
```

### Verificando se está rodando

Acesse: http://localhost:8080/swagger-ui.html

Ou teste o endpoint de health:

```bash
curl http://localhost:8080/actuator/health
```

---

## 🧪 Testes

### Executar todos os testes

```bash
./mvnw test
```

### Executar testes específicos

```bash
./mvnw test -Dtest=PixChargeServiceTest
```

### Cobertura de testes

```bash
./mvnw clean verify
```

### Testes Disponíveis

- ✅ `PixChargeServiceTest` - Testes de serviço PIX
- ✅ `PixChargeRepositoryTest` - Testes de repository
- ✅ `EmvPayloadGeneratorTest` - Testes de geração EMV
- ✅ `QrcodeImageGeneratorTest` - Testes de QR Code
- ✅ `PixKeyValidatorTest` - Testes de validação PIX
- ✅ `RateLimitAspectTest` - Testes de rate limiting
- ✅ `TokenConfigTest` - Testes de JWT

---

## 📖 Documentação da API

A documentação completa dos endpoints está disponível em:

- **[API_DOCUMENTATION.md](./API_DOCUMENTATION.md)** - Documentação detalhada de todos os endpoints
- **Swagger UI** - http://localhost:8080/swagger-ui.html (quando o servidor estiver rodando)
- **OpenAPI JSON** - http://localhost:8080/v3/api-docs

### Endpoints Principais

| Grupo | Endpoints | Descrição |
|-------|-----------|-----------|
| **Auth** | `/api/v1/auth/*` | Autenticação (login, register, refresh) |
| **Organizations** | `/api/v1/users/me/organization-profile` | Perfil de organização |
| **Donations** | `/api/v1/donations/*` | Criação e consulta de doações PIX |
| **Admin** | `/api/admin/*` | Endpoints administrativos |
| **Debug** | `/api/debug/*` | Endpoints de debug |

---

## 📁 Estrutura do Projeto

```
smartMesquitaApi/
├── src/
│   ├── main/
│   │   ├── java/br/com/smartmesquitaapi/
│   │   │   ├── api/                     # Exceções e DTOs globais
│   │   │   │   ├── exception/
│   │   │   │   └── dto/
│   │   │   ├── auth/                    # Autenticação JWT
│   │   │   │   ├── dto/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AuthService.java
│   │   │   │   └── RefreshToken.java
│   │   │   ├── config/                  # Configurações
│   │   │   │   ├── WebMvcConfig.java
│   │   │   │   ├── cache/
│   │   │   │   └── crypto/
│   │   │   ├── organization/            # Organizações (Mosque/Church)
│   │   │   │   ├── domain/
│   │   │   │   │   ├── Organization.java
│   │   │   │   │   ├── Mosque.java
│   │   │   │   │   └── Church.java
│   │   │   │   ├── dto/
│   │   │   │   ├── mapper/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   └── exception/
│   │   │   ├── pix/                     # Doações PIX
│   │   │   │   ├── controller/
│   │   │   │   ├── domain/
│   │   │   │   ├── dto/
│   │   │   │   ├── exception/
│   │   │   │   ├── infrastructure/
│   │   │   │   ├── PixChargeService.java
│   │   │   │   └── PixChargeRepository.java
│   │   │   ├── ratelimit/               # Rate Limiting (AOP)
│   │   │   │   ├── annotations/
│   │   │   │   ├── keygenerators/
│   │   │   │   ├── RateLimitAspect.java
│   │   │   │   └── RateLimitService.java
│   │   │   ├── security/                # Segurança
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── SecurityFilter.java
│   │   │   │   └── TokenConfig.java
│   │   │   ├── user/                    # Usuários
│   │   │   │   ├── controller/
│   │   │   │   ├── domain/
│   │   │   │   ├── dto/
│   │   │   │   ├── service/
│   │   │   │   └── UserRepository.java
│   │   │   └── SmartMesquitaApiApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/            # Flyway migrations
│   └── test/                            # Testes
│       └── java/br/com/smartmesquitaapi/
├── docker-compose.yml                   # Redis via Docker
├── pom.xml                              # Dependências Maven
├── README.md                            # Este arquivo
├── API_DOCUMENTATION.md                 # Documentação da API
└── TESTES.md                            # Guia de testes
```

### Módulos Principais

- **api** - Exceções e DTOs globais
- **auth** - Autenticação e autorização
- **organization** - Gerenciamento de organizações (Mosques/Churches)
- **pix** - Sistema de doações PIX
- **ratelimit** - Rate limiting via AOP
- **security** - Configurações de segurança
- **user** - Gerenciamento de usuários

---

## 🔄 Roadmap

### ✅ Implementado

- [x] Autenticação JWT com refresh tokens
- [x] Cadastro de organizações (Mosque/Church)
- [x] Geração de QR Codes PIX
- [x] Validação de CNPJ
- [x] Rate limiting
- [x] Criptografia de dados sensíveis
- [x] Exceções customizadas
- [x] Repositório de organizações
- [x] Validações completas

### 🚧 Em Desenvolvimento

- [ ] Auditoria com timestamps (createdAt, updatedAt)
- [ ] Controller dedicado para organizações
- [ ] Paginação de listagens
- [ ] Testes unitários completos
- [ ] Documentação Swagger completa

### 📋 Planejado

- [ ] Webhooks para notificações de pagamento
- [ ] Dashboard administrativo
- [ ] Relatórios e estatísticas
- [ ] Integração com gateway de pagamento
- [ ] API de reconciliação bancária
- [ ] Multi-tenancy
- [ ] Soft delete
- [ ] GraphQL API

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Siga os passos:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Add: nova feature incrível'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

### Padrões de Commit

- `Add:` - Nova funcionalidade
- `Update:` - Atualização de funcionalidade existente
- `Fix:` - Correção de bug
- `Refactor:` - Refatoração de código
- `Docs:` - Documentação
- `Test:` - Testes

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👥 Autores

- **Seu Nome** - *Desenvolvedor Principal* - [@seu-usuario](https://github.com/seu-usuario)

---

## 📞 Contato

- Email: contato@smartmesquita.com
- GitHub: [@seu-usuario](https://github.com/seu-usuario)
- LinkedIn: [Seu Nome](https://linkedin.com/in/seu-perfil)

---

## 🙏 Agradecimentos

- Spring Team pela excelente documentação
- Comunidade Java por todo o suporte
- ZXing pela biblioteca de QR Codes

---

<div align="center">

**Feito com ❤️ e ☕ por Smart Mesquita Team**

</div>