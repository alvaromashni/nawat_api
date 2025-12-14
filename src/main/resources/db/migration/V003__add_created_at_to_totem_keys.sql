-- Adiciona coluna created_at na tabela totem_keys
ALTER TABLE totem_keys ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Adiciona comentário explicativo
COMMENT ON COLUMN totem_keys.created_at IS 'Data e hora de criação da chave do totem';
