package com.school.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Репозиторий доступа к данным учетных записей пользователей.
 * <p>
 * Интерфейс расширяет {@link JpaRepository}, предоставляя абстракцию для выполнения
 * операций чтения и записи с сущностью {@link AppUser} без написания SQL-кода.
 * Является частью слоя доступа к данным (DAO) подсистемы безопасности.
 * <p>
 * Реализация требований:
 * - №5 (Хранение данных и доступ к ним).
 * - №4 (Поддержка механизмов аутентификации).
 */
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Выполняет поиск сущности пользователя по уникальному логину.
     * <p>
     * Метод используется сервисом {@link CustomUserDetailsService} в процессе
     * аутентификации для проверки существования пользователя и получения его пароля/ролей.
     *
     * @param username уникальное имя пользователя (логин).
     * @return контейнер {@link Optional}, содержащий найденного пользователя или пустой,
     * если пользователь с указанным логином отсутствует в БД.
     */
    Optional<AppUser> findByUsername(String username);
}