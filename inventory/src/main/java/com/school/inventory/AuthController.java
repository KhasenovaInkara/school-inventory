package com.school.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Контроллер, отвечающий за процессы аутентификации и регистрации.
 * <p>
 * Управляет отображением страниц входа и регистрации, а также
 * логикой создания новых пользователей с шифрованием паролей.
 * <p>
 * Реализует требования:
 * - №4 (Взаимодействие с пользователем через веб-интерфейс).
 * - №5 (Инициализация параметров пользователя).
 */
@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Отображает страницу входа в систему.
     * Саму обработку входа (POST /login) берет на себя Spring Security.
     *
     * @return Имя шаблона страницы входа ("login")
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Отображает форму регистрации нового пользователя.
     *
     * @param model Модель для передачи пустого объекта пользователя в форму
     * @return Имя шаблона страницы регистрации ("register")
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "register";
    }

    /**
     * Обрабатывает данные формы регистрации и сохраняет пользователя в БД.
     * <p>
     * Выполняет следующие действия:
     * 1. Шифрует пароль с помощью BCrypt (безопасность).
     * 2. Назначает роль по умолчанию (ROLE_USER).
     * 3. Проверяет исключение для создания первого администратора.
     *
     * @param user Объект пользователя, заполненный данными из формы
     * @return Перенаправление на страницу входа после успеха
     */
    @PostMapping("/register")
    public String registerUser(AppUser user) {
        // Шифруем пароль перед сохранением (Требование безопасности)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // По умолчанию все новые пользователи получают права обычного ЮЗЕРА
        user.setRole("ROLE_USER");
        
        // ЛОГИКА ИНИЦИАЛИЗАЦИИ АДМИНИСТРАТОРА:
        // Если регистрируется пользователь с логином "admin", выдаем ему права администратора.
        // Это позволяет создать первого администратора системы без прямого доступа к базе данных.
        if (user.getUsername().equals("admin")) {
            user.setRole("ROLE_ADMIN");
        }

        userRepository.save(user);
        return "redirect:/login";
    }
}