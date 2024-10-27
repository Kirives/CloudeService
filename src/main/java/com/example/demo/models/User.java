package com.example.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.io.Serializable;

@Entity
@Table(schema = "public",name = "users")
@Builder
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Size(min = 2, max = 50,message = "Логин должен содержать от 2х до 50ти символов")
    @NotBlank
    @Column(name = "login")
    private String username;
    @Size(min = 2, max = 500,message = "Пароль должен содержать больше 2х символов")
    @NotNull
    private String password;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public @Size(min = 2, max = 50,message = "Логин должен содержать от 2х до 50ти символов") @NotBlank String getUsername() {
        return username;
    }

    public void setUsername(@Size(min = 2, max = 50,message = "Логин должен содержать от 2х до 50ти символов") @NotBlank String username) {
        this.username = username;
    }

    public @Size(min = 2, max = 500,message = "Пароль должен содержать больше 2х символов") @NotNull String getPassword() {
        return password;
    }

    public void setPassword(@Size(min = 2, max = 500,message = "Пароль должен содержать больше 2х символов") @NotNull String password) {
        this.password = password;
    }

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public User() {
    }
}

