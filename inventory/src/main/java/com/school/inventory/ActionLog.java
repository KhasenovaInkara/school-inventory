package com.school.inventory;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Сущность, представляющая запись в журнале событий (лог).
 * <p>
 * Используется для выполнения требования №9 (Система логирования).
 * Хранит информацию о важных действиях пользователей (добавление, удаление, выдача предметов)
 * для отображения на странице "История" и аудита.
 */
@Entity
@Data
@Table(name = "logs")
public class ActionLog {

    /**
     * Уникальный идентификатор записи лога.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Текстовое сообщение о событии.
     * Содержит описание действия, например: "Удален стул" или "Выдан мяч пользователю Admin".
     */
    @Column(nullable = false) // (Опционально) Гарантируем, что сообщение не пустое
    private String message;

    /**
     * Точная дата и время возникновения события.
     * Используется для сортировки истории от новых к старым.
     */
    private LocalDateTime timestamp;
}