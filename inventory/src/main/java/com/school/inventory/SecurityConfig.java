package com.school.inventory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Глобальная конфигурация подсистемы безопасности приложения.
 * <p>
 * Класс отмечен аннотациями {@link Configuration} и {@link EnableWebSecurity},
 * что активирует механизмы защиты веб-приложения.
 * <p>
 * Реализация требований:
 * - №4 (Обеспечение безопасного взаимодействия, аутентификация и авторизация).
 * - №6 (Инкапсуляция логики защиты).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Определяет цепочку фильтров безопасности (Security Filter Chain).
     * <p>
     * Метод настраивает правила разграничения доступа (RBAC - Role-Based Access Control)
     * к различным ресурсам приложения по HTTP.
     *
     * @param http объект конфигурации HttpSecurity.
     * @return сконфигурированный бин SecurityFilterChain.
     * @throws Exception в случае ошибок конфигурации безопасности.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                // 👇 ДОБАВИЛ СЮДА "/error", ЧТОБЫ ПРИ ОШИБКАХ НЕ КИДАЛО НА ЛОГИН
                .requestMatchers("/register", "/login", "/error", "/css/**", "/js/**").permitAll()
                
                // Доступ для авторизованных пользователей (любая роль)
                .requestMatchers("/").authenticated()
                
                // Административная зона (доступ только для ROLE_ADMIN)
                // Включает управление инвентарем (CRUD), историю и обработку заявок
                .requestMatchers("/history", "/add", "/save", "/delete/**", "/edit/**", 
                                 "/requests", "/approve/**", "/reject/**", 
                                 "/borrowed", "/return/**").hasRole("ADMIN")
                
                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login")           // Кастомная страница входа
                .defaultSuccessUrl("/", true)  // Перенаправление после успеха
                .permitAll()
            )
            .logout((logout) -> logout
                .permitAll()                   // Разрешить выход всем
            );

        return http.build();
    }

    /**
     * Создает бин кодировщика паролей.
     * <p>
     * Использует алгоритм BCrypt — криптографическую хеш-функцию,
     * обеспечивающую надежное хранение учетных данных пользователей.
     *
     * @return экземпляр {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Настраивает провайдер аутентификации (DAO Authentication Provider).
     * <p>
     * Связывает сервис загрузки пользователей (UserDetailsService)
     * и кодировщик паролей (PasswordEncoder) для проверки учетных данных при входе.
     *
     * @param userDetailsService сервис поиска пользователей в БД.
     * @param passwordEncoder    компонент для сверки хешей паролей.
     * @return настроенный провайдер аутентификации.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder); 
        return provider;
    }
}