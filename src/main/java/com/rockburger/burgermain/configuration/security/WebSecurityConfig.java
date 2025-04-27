package com.rockburger.burgermain.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter.security.BCryptPasswordAdapter;
import com.rockburger.burgermain.domain.spi.IPasswordEncryptionPort;

@Configuration
public class WebSecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return bCryptPasswordEncoder();
    }

    @Bean
    public IPasswordEncryptionPort passwordEncryptionPort(BCryptPasswordEncoder passwordEncoder) {
        return new BCryptPasswordAdapter(passwordEncoder);
    }
}