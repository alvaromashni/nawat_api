CREATE TABLE pix_charges (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Foreign key para users
                             user_id UUID NOT NULL,

    -- Identificadores
                             local_donation_id VARCHAR(100),
                             idempotency_key VARCHAR(100) NOT NULL,
                             txid VARCHAR(35) NOT NULL UNIQUE,

    -- Valores
                             amount_cents INTEGER NOT NULL CHECK (amount_cents > 0),

    -- Dados do QR Code
                             qr_payload TEXT NOT NULL,
                             qr_image_base64 TEXT,

    -- Status e controle
                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                             psp_name VARCHAR(50) DEFAULT 'static-key',

    -- Timestamps
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             expires_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Confirmação manual
                             receipt_image_url VARCHAR(500),
                             confirmed_by_user_id UUID,
                             confirmed_at TIMESTAMP,
                             notes TEXT,

    -- Constraints
                             CONSTRAINT fk_pix_charges_user FOREIGN KEY (user_id)
                                 REFERENCES users(user_id) ON DELETE CASCADE,
                             CONSTRAINT fk_pix_charges_confirmed_by FOREIGN KEY (confirmed_by_user_id)
                                 REFERENCES users(user_id) ON DELETE SET NULL,
                             CONSTRAINT chk_status CHECK (status IN ('PENDING', 'PAID', 'CONFIRMED_MANUAL', 'EXPIRED', 'CANCELLED'))
);

-- Índices para performance
CREATE UNIQUE INDEX idx_user_idempotency ON pix_charges(user_id, idempotency_key);
CREATE INDEX idx_txid ON pix_charges(txid);
CREATE INDEX idx_status_expires ON pix_charges(status, expires_at);
CREATE INDEX idx_local_donation ON pix_charges(local_donation_id);
CREATE INDEX idx_created_at ON pix_charges(created_at DESC);
CREATE INDEX idx_user_created ON pix_charges(user_id, created_at DESC);

-- Comentários
COMMENT ON TABLE pix_charges IS 'Cobranças PIX geradas pelo sistema';
COMMENT ON COLUMN pix_charges.user_id IS 'ID do usuário (mesquita) que receberá o pagamento';
COMMENT ON COLUMN pix_charges.idempotency_key IS 'Chave de idempotência gerada pelo cliente (totem)';
COMMENT ON COLUMN pix_charges.txid IS 'Transaction ID do PIX (máx 35 chars)';
COMMENT ON COLUMN pix_charges.amount_cents IS 'Valor em centavos (ex: 5000 = R$ 50,00)';
COMMENT ON COLUMN pix_charges.qr_payload IS 'Payload EMV completo do QR Code';
COMMENT ON COLUMN pix_charges.qr_image_base64 IS 'Imagem do QR Code em Base64 (PNG)';
COMMENT ON COLUMN pix_charges.expires_at IS 'Data/hora de expiração do QR Code';
COMMENT ON COLUMN pix_charges.psp_name IS 'Nome do PSP/Gateway usado (padrão: static-key)';