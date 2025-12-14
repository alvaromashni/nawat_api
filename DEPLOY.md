# Guia de Deploy no Render

Este guia explica como fazer deploy da aplicação Smart Mesquita API no Render.

## Pré-requisitos

1. Conta no [Render](https://render.com)
2. Repositório Git (GitHub, GitLab ou Bitbucket)
3. Código commitado e enviado para o repositório

## Passo 1: Preparar o Repositório

1. Certifique-se de que todos os arquivos estão commitados:
```bash
git add .
git commit -m "Preparar para deploy no Render"
git push origin main
```

## Passo 2: Criar os Serviços no Render

### Opção A: Deploy Automático com render.yaml

1. Acesse [Render Dashboard](https://dashboard.render.com)
2. Clique em "New +" → "Blueprint"
3. Conecte seu repositório Git
4. O Render detectará automaticamente o arquivo `render.yaml`
5. Revise os serviços que serão criados:
   - **smart-mesquita-api** (Web Service)
   - **smart-mesquita-db** (PostgreSQL)
   - **smart-mesquita-redis** (Redis)
6. Clique em "Apply"

### Opção B: Deploy Manual

#### 2.1. Criar o Banco de Dados PostgreSQL

1. No Dashboard do Render, clique em "New +" → "PostgreSQL"
2. Configure:
   - **Name**: `smart-mesquita-db`
   - **Database**: `smartmesquita`
   - **User**: `smartmesquita`
   - **Region**: Escolha o mais próximo (ex: Oregon - US West)
   - **Plan**: Free
3. Clique em "Create Database"
4. **Importante**: Copie a **Internal Connection String** (formato: `postgresql://user:pass@host:port/db`)

#### 2.2. Criar o Redis (Opcional mas Recomendado)

1. Clique em "New +" → "Redis"
2. Configure:
   - **Name**: `smart-mesquita-redis`
   - **Region**: Mesmo do banco de dados
   - **Plan**: Free
3. Clique em "Create Redis"
4. Copie as informações de conexão (Host, Port, Password)

#### 2.3. Criar o Web Service

1. Clique em "New +" → "Web Service"
2. Conecte seu repositório Git
3. Configure:
   - **Name**: `smart-mesquita-api`
   - **Region**: Mesmo do banco de dados
   - **Branch**: `main`
   - **Runtime**: Java
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -Dserver.port=$PORT -jar target/smartMesquitaApi-0.0.1-SNAPSHOT.jar`
   - **Plan**: Free

## Passo 3: Configurar Variáveis de Ambiente

No painel do Web Service, vá em "Environment" e adicione:

### Variáveis Obrigatórias:

```
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=<cole a Internal Connection String do PostgreSQL>
JWT_SECRET=<gere uma string aleatória segura de 256 bits>
CORS_ALLOWED_ORIGINS=https://seu-frontend.com
```

### Variáveis Opcionais (se estiver usando Redis):

```
REDIS_HOST=<host do redis>
REDIS_PORT=<porta do redis>
REDIS_PASSWORD=<senha do redis>
```

### Gerando JWT_SECRET Seguro:

No terminal, execute um destes comandos:

**Linux/Mac:**
```bash
openssl rand -base64 32
```

**Ou use um gerador online confiável:**
- https://www.random.org/strings/

O resultado deve ter pelo menos 32 caracteres.

## Passo 4: Deploy

1. Após configurar as variáveis, clique em "Manual Deploy" → "Deploy latest commit"
2. Acompanhe os logs do build
3. Aguarde o deploy completar (pode levar 5-10 minutos na primeira vez)

## Passo 5: Verificar a Aplicação

1. Acesse a URL fornecida pelo Render (ex: `https://smart-mesquita-api.onrender.com`)
2. Teste o health check: `https://seu-app.onrender.com/actuator/health`
3. Deve retornar:
```json
{
  "status": "UP"
}
```

## Passo 6: Configurar o Frontend

No seu frontend, atualize a URL da API para:
```javascript
const API_URL = 'https://smart-mesquita-api.onrender.com';
```

E adicione a URL do frontend nas variáveis de ambiente `CORS_ALLOWED_ORIGINS` no Render.

## Troubleshooting

### Erro: "Application failed to start"
- Verifique os logs no Render Dashboard
- Confirme que todas as variáveis de ambiente estão configuradas
- Verifique se a connection string do banco está correta

### Erro: "Connection refused" ou timeout
- Certifique-se de usar a **Internal Connection String** do PostgreSQL
- Verifique se o banco de dados está na mesma região que a aplicação

### Erro de CORS
- Adicione a URL do seu frontend em `CORS_ALLOWED_ORIGINS`
- Exemplo: `https://meu-app.vercel.app,https://www.meu-dominio.com`

### Aplicação fica em sleep (plano Free)
- No plano Free, a aplicação hiberna após 15 minutos de inatividade
- A primeira requisição após hibernação pode levar 30-60 segundos
- Considere upgrade para plano pago ou use um serviço de ping (ex: UptimeRobot)

## Atualizações Futuras

Para fazer deploy de novas versões:

1. Faça commit das alterações:
```bash
git add .
git commit -m "Descrição das alterações"
git push origin main
```

2. O Render fará deploy automático (se configurado) ou:
   - Acesse o Dashboard → Seu Web Service
   - Clique em "Manual Deploy" → "Deploy latest commit"

## Comandos Úteis

### Ver logs em tempo real:
No dashboard do Render, vá em "Logs" do seu Web Service

### Acessar o banco de dados:
1. No dashboard do PostgreSQL, vá em "Connect"
2. Use a External Connection String com um cliente PostgreSQL (DBeaver, pgAdmin, etc.)

### Executar migrations manualmente:
As migrations do Flyway rodam automaticamente ao iniciar a aplicação.

## Links Úteis

- [Render Documentation](https://render.com/docs)
- [Render Java Guide](https://render.com/docs/deploy-spring-boot)
- [Dashboard do Render](https://dashboard.render.com)

## Suporte

Para problemas:
1. Verifique os logs no Render Dashboard
2. Consulte a documentação do Render
3. Entre em contato com o time de desenvolvimento
