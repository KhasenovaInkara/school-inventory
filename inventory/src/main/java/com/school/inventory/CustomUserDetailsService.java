package com.school.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса загрузки пользовательских данных для подсистемы безопасности Spring Security.
 * <p>
 * Класс служит адаптером между слоем хранения данных (UserRepository) и контекстом безопасности.
 * Осуществляет преобразование доменной сущности приложения {@link AppUser} в объект авторизации,
 * требуемый фреймворком.
 * <p>
 * Реализация требований:
 * - №1 (Архитектура программы: слой сервисов).
 * - №4 (Взаимодействие с системой безопасности и аутентификация).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Осуществляет поиск пользователя в базе данных по уникальному имени (логину).
     * <p>
     * Метод вызывается провайдером аутентификации при попытке входа в систему.
     * Выполняет извлечение данных, проверку существования учетной записи и маппинг
     * ролей пользователя в формат, поддерживаемый Spring Security.
     *
     * @param username имя пользователя, подлежащее проверке.
     * @return объект {@link UserDetails}, содержащий учетные данные и список прав доступа.
     * @throws UsernameNotFoundException генерируется, если пользователь с указанным именем отсутствует в БД.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Поиск сущности пользователя в репозитории
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Формирование объекта UserDetails с маппингом ролей
        // Удаление префикса "ROLE_" необходимо для корректной работы метода builder().roles()
        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword())
                .roles(appUser.getRole().replace("ROLE_", ""))
                .build();
    }
}