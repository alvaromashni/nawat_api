package br.com.smartmesquitaapi.apikey.dto;

public class TotemKeyResponse {

    private String name;
    private String keyValue;

    public TotemKeyResponse(String name, String keyValue) {
        this.name = name;
        this.keyValue = keyValue;
    }

    public String getName() {
        return name;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }
}
