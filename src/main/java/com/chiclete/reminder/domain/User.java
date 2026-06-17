package com.chiclete.reminder.domain;

import jakarta.persistence.*;

/**
 * Entidade que representa um usuário do sistema.
 * O papel ({@code role}) define o nível de acesso: COMUM, REMETENTE ou ADMIN.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    /** Número WhatsApp com DDI, só dígitos (ex.: 5511999998888). */
    @Column(length = 20)
    private String whatsapp;

    @Column(nullable = false)
    private boolean notifyEmail = true;

    @Column(nullable = false)
    private boolean notifyWhatsapp = false;

    @Column(nullable = false)
    private boolean notifyInApp = true;

    public User() {}

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }

    public boolean isNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(boolean notifyEmail) { this.notifyEmail = notifyEmail; }

    public boolean isNotifyWhatsapp() { return notifyWhatsapp; }
    public void setNotifyWhatsapp(boolean notifyWhatsapp) { this.notifyWhatsapp = notifyWhatsapp; }

    public boolean isNotifyInApp() { return notifyInApp; }
    public void setNotifyInApp(boolean notifyInApp) { this.notifyInApp = notifyInApp; }
}