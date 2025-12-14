#!/bin/bash

echo "🔍 Verificando configuração para deploy no Render..."
echo ""

# Cores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verifica se render.yaml existe
if [ -f "render.yaml" ]; then
    echo -e "${GREEN}✓${NC} render.yaml encontrado"
else
    echo -e "${RED}✗${NC} render.yaml NÃO encontrado"
fi

# Verifica se application-prod.properties existe
if [ -f "src/main/resources/application-prod.properties" ]; then
    echo -e "${GREEN}✓${NC} application-prod.properties encontrado"
else
    echo -e "${RED}✗${NC} application-prod.properties NÃO encontrado"
fi

# Verifica se system.properties existe
if [ -f "system.properties" ]; then
    echo -e "${GREEN}✓${NC} system.properties encontrado"
else
    echo -e "${RED}✗${NC} system.properties NÃO encontrado"
fi

# Verifica se .gitignore tem .env
if grep -q "^\.env$" .gitignore 2>/dev/null; then
    echo -e "${GREEN}✓${NC} .gitignore configurado para ignorar .env"
else
    echo -e "${YELLOW}⚠${NC}  .env não está no .gitignore"
fi

# Verifica dependências no pom.xml
echo ""
echo "📦 Verificando dependências no pom.xml..."

if grep -q "flyway-core" pom.xml; then
    echo -e "${GREEN}✓${NC} Flyway configurado"
else
    echo -e "${RED}✗${NC} Flyway NÃO configurado"
fi

if grep -q "spring-boot-starter-actuator" pom.xml; then
    echo -e "${GREEN}✓${NC} Actuator configurado"
else
    echo -e "${RED}✗${NC} Actuator NÃO configurado"
fi

# Verifica migrations do Flyway
echo ""
echo "🗄️  Verificando migrations do Flyway..."
migration_count=$(find src/main/resources/db/migration -name "V*.sql" 2>/dev/null | wc -l)
if [ $migration_count -gt 0 ]; then
    echo -e "${GREEN}✓${NC} $migration_count migration(s) encontrada(s)"
else
    echo -e "${YELLOW}⚠${NC}  Nenhuma migration encontrada"
fi

echo ""
echo "📋 Checklist antes do deploy:"
echo ""
echo "1. [ ] Commit e push de todas as alterações"
echo "2. [ ] Criar conta no Render (render.com)"
echo "3. [ ] Criar PostgreSQL Database no Render"
echo "4. [ ] Criar Redis no Render (opcional)"
echo "5. [ ] Criar Web Service no Render"
echo "6. [ ] Configurar variáveis de ambiente:"
echo "       - SPRING_PROFILES_ACTIVE=prod"
echo "       - DATABASE_URL (da conexão PostgreSQL)"
echo "       - JWT_SECRET (gerar string aleatória segura)"
echo "       - CORS_ALLOWED_ORIGINS (URL do frontend)"
echo "7. [ ] Fazer deploy manual ou automático"
echo "8. [ ] Testar /actuator/health"
echo ""
echo "📖 Leia DEPLOY.md para instruções detalhadas!"
