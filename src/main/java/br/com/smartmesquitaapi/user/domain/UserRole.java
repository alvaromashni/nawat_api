package br.com.smartmesquitaapi.user.domain;

public enum UserRole {

    /**
     * Administrador do sistema (acesso total)
     */
    ADMIN,

    /**
     * Staff/funcionário (pode validar comprovantes, gerenciar cobranças)
     */
    STAFF,

    /**
     * Responsável pela instituição (recebe doações)
     */
    ORG_OWNER,


    /**
     * Usuário comum (acesso limitado)
     */
    USER;

    /**
     * Verifica se é um papel administrativo
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Verifica se é staff ou admin
     */
    public boolean isStaffOrAdmin() {
        return this == ADMIN || this == STAFF;
    }

    /**
     * Verifica se pode gerenciar cobranças PIX
     */
    public boolean canManageCharges() {
        return this == ADMIN || this == STAFF;
    }

    /**
     * Verifica se pode receber pagamentos
     */
    public boolean canReceivePayments() {
        return this == ORG_OWNER;
    }

}
