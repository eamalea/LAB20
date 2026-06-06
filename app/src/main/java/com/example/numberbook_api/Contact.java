package com.example.numberbook_api;

import com.google.gson.annotations.SerializedName;

public class Contact {
    private int id;
    private String name;
    private String phone;
    private String source;
    @SerializedName("created_at")
    private String createdAt;

    public Contact() {}

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}