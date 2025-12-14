-- ============================================================
-- V001: Criação das tabelas base do sistema
-- ============================================================

-- ============================================================
-- 1. ORGANIZATION (tabela base com herança JOINED)
-- ============================================================
CREATE TABLE organization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    foundation_date DATE,
    administrator_name VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14) UNIQUE NOT NULL,
    opening_hours VARCHAR(255),
    is_enabled BOOLEAN NOT NULL DEFAULT true,

    -- BankDetails (Embedded)
    bank_name VARCHAR(100),
    agency VARCHAR(60),
    account_number VARCHAR(500),  -- Criptografado
    account_holder VARCHAR(200),
    ownership_proof_url VARCHAR(500),
    pix_key_type VARCHAR(20),
    verified_at TIMESTAMP,
    pix_key VARCHAR(500),  -- Criptografado
    is_verified BOOLEAN DEFAULT false,

    -- Address (Embedded)
    address_id UUID,
    street VARCHAR(255),
    number VARCHAR(20),
    neighborhood VARCHAR(100),
    zipcode VARCHAR(10),
    complement VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(2),

    CONSTRAINT chk_cnpj_format CHECK (cnpj ~ '^\d{14}$'),
    CONSTRAINT chk_pix_key_type CHECK (pix_key_type IN ('EMAIL', 'CPF', 'CNPJ', 'PHONE', 'RANDOM'))
);

-- Índices para Organization
CREATE INDEX idx_organization_cnpj ON organization(cnpj);
CREATE INDEX idx_organization_enabled ON organization(is_enabled);

-- Comentários
COMMENT ON TABLE organization IS 'Tabela base de organizações (Mesquitas, Igrejas, etc.) com herança JOINED';
COMMENT ON COLUMN organization.cnpj IS 'CNPJ da organização (14 dígitos)';
COMMENT ON COLUMN organization.is_verified IS 'Indica se a chave PIX foi verificada';
COMMENT ON COLUMN organization.account_number IS 'Número da conta bancária (criptografado)';
COMMENT ON COLUMN organization.pix_key IS 'Chave PIX (criptografada)';


-- ============================================================
-- 2. MOSQUE (herda de Organization)
-- ============================================================
CREATE TABLE mosque (
    id UUID PRIMARY KEY,
    ima_name VARCHAR(255),

    CONSTRAINT fk_mosque_organization FOREIGN KEY (id)
        REFERENCES organization(id) ON DELETE CASCADE
);

COMMENT ON TABLE mosque IS 'Mesquitas - herda de Organization';
COMMENT ON COLUMN mosque.ima_name IS 'Nome do Imã responsável';


-- ============================================================
-- 3. CHURCH (herda de Organization)
-- ============================================================
CREATE TABLE church (
    id UUID PRIMARY KEY,
    priest_name VARCHAR(255),

    CONSTRAINT fk_church_organization FOREIGN KEY (id)
        REFERENCES organization(id) ON DELETE CASCADE
);

COMMENT ON TABLE church IS 'Igrejas - herda de Organization';
COMMENT ON COLUMN church.priest_name IS 'Nome do Padre responsável';


-- ============================================================
-- 4. USERS
-- ============================================================
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    organization_id UUID,

    -- Notification (Embedded)
    donation_done BOOLEAN DEFAULT false,
    daily_summary BOOLEAN DEFAULT false,
    totem_maintenance BOOLEAN DEFAULT false,

    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id)
        REFERENCES organization(id) ON DELETE SET NULL,
    CONSTRAINT chk_role CHECK (role IN ('USER', 'ORG_OWNER', 'ADMIN'))
);

-- Índices para Users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_enabled ON users(is_enabled);
CREATE INDEX idx_users_organization ON users(organization_id);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- Comentários
COMMENT ON TABLE users IS 'Usuários do sistema';
COMMENT ON COLUMN users.role IS 'Papel do usuário: USER, ORG_OWNER, ADMIN';
COMMENT ON COLUMN users.is_enabled IS 'Indica se o usuário está ativo';
COMMENT ON COLUMN users.donation_done IS 'Notificação quando doação é realizada';
COMMENT ON COLUMN users.daily_summary IS 'Notificação de resumo diário';
COMMENT ON COLUMN users.totem_maintenance IS 'Notificação de manutenção de totem';


-- ============================================================
-- 5. TOTEM_KEYS (API Keys dos totems)
-- ============================================================
CREATE TABLE totem_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    key_value VARCHAR(255) UNIQUE NOT NULL,
    organization_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_totem_keys_organization FOREIGN KEY (organization_id)
        REFERENCES organization(id) ON DELETE CASCADE
);

-- Índices para TotemKeys
CREATE INDEX idx_totem_keys_key_value ON totem_keys(key_value);
CREATE INDEX idx_totem_keys_organization ON totem_keys(organization_id);
CREATE INDEX idx_totem_keys_active ON totem_keys(is_active);

-- Comentários
COMMENT ON TABLE totem_keys IS 'Chaves de API para autenticação dos totems';
COMMENT ON COLUMN totem_keys.key_value IS 'Valor único da chave de API';
COMMENT ON COLUMN totem_keys.is_active IS 'Indica se a chave está ativa';