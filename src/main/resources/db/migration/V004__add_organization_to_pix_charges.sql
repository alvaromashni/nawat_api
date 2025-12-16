-- 1. Adicionar a coluna organization_id permitindo NULL inicialmente
-- (Precisamos disso para não dar erro nas linhas que já existem)
ALTER TABLE pix_charges ADD COLUMN organization_id UUID;

-- 2. Backfill (Migração de Dados):
-- Para cada PixCharge existente, olhamos quem é o User dono dele,
-- descobrimos a Organization desse User e copiamos para a nova coluna.
UPDATE pix_charges
SET organization_id = u.organization_id
    FROM users u
WHERE pix_charges.user_id = u.id;

-- 3. Agora que garantimos que ninguém está com organization_id nulo,
-- podemos adicionar a restrição NOT NULL.
ALTER TABLE pix_charges ALTER COLUMN organization_id SET NOT NULL;

-- 4. Adicionar a Foreign Key para garantir integridade
-- (Ajuste o nome da tabela 'organization' caso no seu banco esteja 'organizations')
ALTER TABLE pix_charges
    ADD CONSTRAINT fk_pix_charges_organization
        FOREIGN KEY (organization_id) REFERENCES organization(id);

-- 5. O Pulo do Gato para o Totem:
-- Tornamos a coluna user_id opcional (NULLable).
-- Assim o Totem pode criar cobranças sem estar vinculado a um User específico.
ALTER TABLE pix_charges ALTER COLUMN user_id DROP NOT NULL;