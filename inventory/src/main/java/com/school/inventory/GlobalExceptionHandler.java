package com.school.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Глобальный обработчик исключений (Global Exception Handler).
 * <p>
 * Класс перехватывает ошибки, возникающие в процессе работы приложения,
 * логирует их и перенаправляет пользователя на понятную страницу ошибки
 * вместо отображения технического стека вызовов (Stack Trace).
 * <p>
 * Реализация требований:
 * - №11 (Система обработки исключений и информирования пользователя).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Обрабатывает общие исключения, возникающие в контроллерах.
     *
     * @param e     возникшее исключение.
     * @param model модель для передачи сообщения об ошибке на страницу.
     * @return имя шаблона страницы ошибки ("error").
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        // Логируем техническую ошибку для админа
        logger.error("⚠️ ПРОИЗОШЛА ОШИБКА: ", e);

        // Формируем сообщение для пользователя
        String errorMessage = "Произошла непредвиденная ошибка. Пожалуйста, обратитесь к администратору.";
        
        // Если ошибка связана с ненайденной страницей (404), пишем понятнее
        if (e instanceof NoResourceFoundException) {
             errorMessage = "Запрашиваемая страница или ресурс не найдены.";
        }

        model.addAttribute("errorMessage", errorMessage);
        
        // Показываем красивую страницу error.html
        return "error";
    }
}