package br.com.smartmesquitaapi.apikey.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class TotemKeyListResponse {

    private UUID id;
    private String name;
    private String keyValueMasked;
    private boolean isActive;
    private LocalDateTime createdAt;

    public TotemKeyListResponse() {}

    public TotemKeyListResponse(UUID id, String name, String keyValueMasked, boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.keyValueMasked = keyValueMasked;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyValueMasked() {
        return keyValueMasked;
    }

    public void setKeyValueMasked(String keyValueMasked) {
        this.keyValueMasked = keyValueMasked;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
