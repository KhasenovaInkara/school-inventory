package com.school.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Компонент аудита событий безопасности (Security Audit Listener).
 * <p>
 * Класс реализует паттерн Observer, подписываясь на события жизненного цикла
 * аутентификации Spring Security. Обеспечивает регистрацию фактов входа
 * и выхода пользователей в системном логе.
 * <p>
 * Реализация требований:
 * - №9 (Система логирования ключевых событий и аудита безопасности).
 */
@Component
public class SecurityLogger {

    private static final Logger logger = LoggerFactory.getLogger(SecurityLogger.class);

    /**
     * Обработчик события успешной аутентификации пользователя.
     * <p>
     * Метод вызывается автоматически контейнером Spring при публикации события
     * {@link AuthenticationSuccessEvent}. Фиксирует идентификатор пользователя (Principal)
     * и предоставленные ему права доступа (Authorities).
     *
     * @param event объект события, содержащий детали аутентификации.
     */
    @EventListener
    public void onLogin(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String roles = event.getAuthentication().getAuthorities().toString();
        logger.info("🔐 АУДИТ БЕЗОПАСНОСТИ [ВХОД]: Пользователь '{}' идентифицирован. Права доступа: {}", username, roles);
    }

    /**
     * Обработчик события успешного выхода из системы (Logout).
     * <p>
     * Метод вызывается автоматически при публикации события {@link LogoutSuccessEvent}.
     * Регистрирует завершение сеансa работы пользователя.
     *
     * @param event объект события выхода.
     */
    @EventListener
    public void onLogout(LogoutSuccessEvent event) {
        String username = (event.getAuthentication() != null) ? event.getAuthentication().getName() : "Anonymous";
        logger.info("🚪 АУДИТ БЕЗОПАСНОСТИ [ВЫХОД]: Сеанс пользователя '{}' завершен.", username);
    }
}