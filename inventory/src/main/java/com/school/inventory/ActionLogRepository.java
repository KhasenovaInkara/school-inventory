package com.school.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Репозиторий для доступа к данным журнала событий (ActionLog).
 * <p>
 * Расширяет стандартный интерфейс JpaRepository, автоматически предоставляя
 * методы для создания, чтения, обновления и удаления записей (CRUD).
 * <p>
 * Соответствует требованиям:
 * - №5 (Хранение данных и доступ к ним).
 * - №9 (Реализация системы логирования).
 */
@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {

    /**
     * Извлекает из базы данных полную историю событий и сортирует её по дате.
     * <p>
     * Используется на странице "История" для показа последних действий сверху.
     * Spring Data JPA автоматически генерирует SQL-запрос на основе имени метода
     * (ORDER BY timestamp DESC).
     *
     * @return Список всех логов, отсортированный от новых к старым.
     */
    List<ActionLog> findAllByOrderByTimestampDesc();
}