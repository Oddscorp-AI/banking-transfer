package com.example.banking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.banking.model.Setting;

public interface SettingRepository extends JpaRepository<Setting, String> {
}
