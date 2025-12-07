package com.school.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Основной контроллер приложения, реализующий бизнес-логику управления школьным инвентарем.
 * <p>
 * Класс обеспечивает обработку входящих HTTP-запросов, взаимодействие со слоем данных (Repositories)
 * и управление жизненным циклом заявок на выдачу имущества.
 * <p>
 * Реализация функциональных требований:
 * - №4 (Взаимодействие с системой через веб-интерфейс).
 * - №9 (Система логирования ключевых операций).
 * - №10 (Документирование программного кода).
 */
@Controller
public class InventoryController {

    // Инициализация логгера для записи системных событий (SLF4J)
    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ActionLogRepository logRepository;

    @Autowired
    private ItemRequestRepository requestRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Обрабатывает запрос к корневому маршруту приложения.
     * Осуществляет выборку списка инвентаря с возможностью фильтрации по ключевому слову.
     *
     * @param model   объект модели Spring MVC для передачи данных в представление.
     * @param keyword строка поиска для фильтрации товаров (опционально).
     * @return логическое имя представления ("index") для рендеринга главной страницы.
     */
    @GetMapping("/")
    public String home(Model model, String keyword) {
        List<InventoryItem> items;
        
        // Реализация логики условного логирования поисковых запросов
        if (keyword != null && !keyword.isEmpty()) {
            logger.info("🔍 Пользователь инициировал поиск предмета: {}", keyword);
            items = inventoryRepository.findByTitleContainingIgnoreCase(keyword);
        } else {
            logger.info("🏠 Загрузка полного реестра инвентаря.");
            items = inventoryRepository.findAll();
        }
        
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword);
        return "index";
    }

    /**
     * Формирует список предметов, находящихся в данный момент в эксплуатации (выданных пользователям).
     * Выполняет фильтрацию заявок по статусу "APPROVED".
     *
     * @param model объект модели для передачи списка займов.
     * @return логическое имя представления ("borrowed").
     */
    @GetMapping("/borrowed")
    public String showBorrowedItems(Model model) {
        logger.info("Запрос списка выданного инвентаря.");
        List<ItemRequest> activeLoans = requestRepository.findByStatus("APPROVED");
        model.addAttribute("loans", activeLoans);
        return "borrowed";
    }

    /**
     * Реализует процедуру возврата инвентарной единицы на склад.
     * <p>
     * Выполняет следующие транзакционные действия:
     * 1. Увеличение остатка товара на складе.
     * 2. Изменение статуса заявки на "RETURNED".
     * 3. Регистрация события возврата в журнале аудита.
     *
     * @param requestId уникальный идентификатор заявки.
     * @return перенаправление на страницу списка выданных предметов.
     */
    @GetMapping("/return/{requestId}")
    public String returnItem(@PathVariable("requestId") long requestId) {
        logger.info("Инициализация процедуры возврата по заявке ID: {}", requestId);
        ItemRequest request = requestRepository.findById(requestId).orElse(null);
        
        if (request != null && "APPROVED".equals(request.getStatus())) {
            // Восстановление количественного учета
            InventoryItem item = request.getItem();
            item.setQuantity(item.getQuantity() + 1);
            inventoryRepository.save(item);

            // Закрытие жизненного цикла заявки
            request.setStatus("RETURNED");
            requestRepository.save(request);

            // Аудит операции
            String msg = "Вернул: " + request.getRequester().getFullName() + " -> " + item.getTitle();
            logEvent(msg); 
            logger.info("Успешный возврат инвентаря: {}", msg);
        } else {
            logger.warn("Ошибка процедуры возврата: заявка ID {} не найдена или имеет неверный статус.", requestId);
        }
        return "redirect:/borrowed";
    }

    /**
     * Отображает список входящих заявок, ожидающих обработки администратором.
     * Фильтрует заявки по статусу "PENDING".
     *
     * @param model объект модели.
     * @return логическое имя представления ("requests").
     */
    @GetMapping("/requests")
    public String showRequests(Model model) {
        logger.info("Запрос списка входящих заявок на рассмотрении.");
        List<ItemRequest> requests = requestRepository.findByStatus("PENDING");
        model.addAttribute("requests", requests);
        return "requests";
    }

    /**
     * Инициирует создание новой заявки на получение инвентаря текущим пользователем.
     * <p>
     * Метод извлекает текущий контекст безопасности для определения пользователя,
     * создает запись заявки со статусом "PENDING" и фиксирует время создания.
     *
     * @param itemId             идентификатор запрашиваемого предмета.
     * @param redirectAttributes атрибуты для передачи flash-сообщений после перенаправления.
     * @return перенаправление на главную страницу.
     */
    @GetMapping("/request/{id}")
    public String makeRequest(@PathVariable("id") long itemId, RedirectAttributes redirectAttributes) {
        InventoryItem item = inventoryRepository.findById(itemId).orElse(null);
        if (item != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            AppUser user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                ItemRequest request = new ItemRequest();
                request.setItem(item);
                request.setRequester(user);
                request.setStatus("PENDING");
                request.setCreatedAt(LocalDateTime.now());
                requestRepository.save(request);
                
                logger.info("Регистрация новой заявки: Пользователь [{}] -> Предмет [{}]", username, item.getTitle());
                redirectAttributes.addFlashAttribute("message", "Заявка успешно отправлена на рассмотрение.");
            }
        } else {
            logger.error("Попытка создания заявки для несуществующего предмета ID: {}", itemId);
        }
        return "redirect:/";
    }

    /**
     * Выполняет утверждение заявки администратором.
     * <p>
     * Проверяет наличие товара на складе. В случае успеха уменьшает доступное количество,
     * переводит заявку в статус "APPROVED" и создает запись в логе.
     *
     * @param requestId          идентификатор утверждаемой заявки.
     * @param redirectAttributes атрибуты для передачи сообщений об ошибках.
     * @return перенаправление на список заявок.
     */
    @GetMapping("/approve/{requestId}")
    public String approveRequest(@PathVariable("requestId") long requestId, RedirectAttributes redirectAttributes) {
        ItemRequest request = requestRepository.findById(requestId).orElse(null);

        if (request != null && request.getItem().getQuantity() > 0) {
            InventoryItem item = request.getItem();
            item.setQuantity(item.getQuantity() - 1);
            inventoryRepository.save(item);

            request.setStatus("APPROVED");
            requestRepository.save(request);

            String msg = "Выдано: " + item.getTitle() + " -> " + request.getRequester().getFullName();
            logEvent(msg);
            logger.info("Заявка ID {} утверждена. {}", requestId, msg);
        } else {
            logger.warn("Отказ в утверждении заявки ID {}: недостаточно товара на складе.", requestId);
            redirectAttributes.addFlashAttribute("error", "Ошибка выполнения операции: товар отсутствует на складе.");
        }
        return "redirect:/requests";
    }

    /**
     * Выполняет отклонение заявки администратором.
     * Переводит заявку в статус "REJECTED" без изменения товарных остатков.
     *
     * @param requestId идентификатор отклоняемой заявки.
     * @return перенаправление на список заявок.
     */
    @GetMapping("/reject/{requestId}")
    public String rejectRequest(@PathVariable("requestId") long requestId) {
        ItemRequest request = requestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus("REJECTED");
            requestRepository.save(request);
            logger.info("Заявка ID {} отклонена администратором.", requestId);
        }
        return "redirect:/requests";
    }
    
    /**
     * Отображает журнал аудита системы (историю операций).
     *
     * @param model объект модели.
     * @return логическое имя представления ("history").
     */
    @GetMapping("/history")
    public String history(Model model) {
        logger.info("Запрос журнала истории операций.");
        model.addAttribute("logs", logRepository.findAllByOrderByTimestampDesc()); 
        return "history"; 
    }
    
    /**
     * Инициализирует форму создания новой инвентарной единицы.
     *
     * @param model объект модели.
     * @return логическое имя представления формы ("item-form").
     */
    @GetMapping("/add")
    public String showAddForm(Model model) { 
        logger.info("Инициализация формы добавления нового товара.");
        model.addAttribute("item", new InventoryItem()); 
        return "item-form"; 
    }

    /**
     * Осуществляет сохранение (создание или обновление) инвентарной единицы в базе данных.
     * В случае создания новой записи автоматически устанавливает дату добавления.
     *
     * @param item сущность товара, полученная из формы.
     * @return перенаправление на главную страницу.
     */
    @PostMapping("/save")
    public String saveItem(@ModelAttribute InventoryItem item) {
        if (item.getId() == null) { 
            // Логика создания нового объекта
            item.setDateAdded(LocalDate.now()); 
            String msg = "Добавлен в реестр: " + item.getTitle();
            logEvent(msg); 
            logger.info(msg);
        } else { 
            // Логика обновления существующего объекта
            InventoryItem old = inventoryRepository.findById(item.getId()).orElse(null); 
            if (old != null) {
                item.setDateAdded(old.getDateAdded()); 
            }
            logger.info("Обновление данных предмета ID: {}", item.getId());
        }
        inventoryRepository.save(item); 
        return "redirect:/";
    }

    /**
     * Инициализирует форму редактирования существующего предмета.
     *
     * @param id    идентификатор редактируемого предмета.
     * @param model объект модели.
     * @return логическое имя представления формы ("item-form").
     */
    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) { 
        logger.info("Запрос интерфейса редактирования товара ID: {}", id);
        model.addAttribute("item", inventoryRepository.findById(id).orElse(null)); 
        return "item-form"; 
    }

    /**
     * Удаляет инвентарную единицу из базы данных.
     * Перед удалением производит запись события в журнал аудита.
     *
     * @param id идентификатор удаляемого предмета.
     * @return перенаправление на главную страницу.
     */
    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable("id") long id) {
        InventoryItem item = inventoryRepository.findById(id).orElse(null);
        if (item != null) {
            String msg = "Удален из реестра: " + item.getTitle();
            logEvent(msg);
            logger.warn("ВНИМАНИЕ: Произведено удаление предмета '{}' (ID: {})", item.getTitle(), id);
            inventoryRepository.delete(item); 
        }
        return "redirect:/";
    }

    /**
     * Вспомогательный метод инкапсуляции логики записи событий в БД.
     * Используется для формирования пользовательской истории операций.
     *
     * @param message текстовое содержание события.
     */
    private void logEvent(String message) {
        ActionLog log = new ActionLog(); 
        log.setMessage(message); 
        log.setTimestamp(LocalDateTime.now()); 
        logRepository.save(log);
    }
}