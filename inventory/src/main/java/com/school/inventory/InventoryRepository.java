package com.school.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Репозиторий доступа к данным реестра инвентаря.
 * <p>
 * Интерфейс расширяет спецификацию {@link JpaRepository}, предоставляя высокоуровневую
 * абстракцию над базой данных для выполнения CRUD-операций (Create, Read, Update, Delete)
 * с сущностями {@link InventoryItem}.
 * <p>
 * Реализация требований:
 * - №5 (Организация хранения и доступа к данным).
 * - №7 (Реализация алгоритмов поиска и фильтрации).
 */
@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * Выполняет поиск инвентарных единиц по частичному совпадению наименования.
     * <p>
     * Реализует алгоритм регистронезависимого поиска (Case-Insensitive Search).
     * Используется контроллером для фильтрации списка товаров на главной странице.
     * <p>
     * Spring Data JPA автоматически транслирует сигнатуру метода в SQL-запрос вида:
     * {@code SELECT * FROM items WHERE LOWER(title) LIKE LOWER('%keyword%')}
     *
     * @param keyword ключевое слово или его фрагмент для поиска.
     * @return список найденных инвентарных единиц, соответствующих критерию поиска.
     */
    List<InventoryItem> findByTitleContainingIgnoreCase(String keyword);
}