package com.bankingsystem.app.repository;

import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// создание репозитория для работы метода класса LimitRepository с БД используя методы интерфейса
// JpaRepository которые включают в себя CRUD(create, read, update, delete)
@Repository
public interface LimitRepository extends JpaRepository<LimitEntity, Long> {
    List<LimitEntity> findByAccountId(Long accountId);
    // FIXME: возможно неправильная работа метода getLimitById т.к. непонятна ситуация с id'шниками в классах сущностей и моделей
    LimitEntity getLimitById(Long DBId);
    LimitEntity getLimitByAccountIdAndCategory(Long accountId, Category category);

    //метод JPQL(метод запросов работающий с сущностями Java)
    //Метод findFirstByAccountIdAndCategory предназначен для поиска лимита (LimitEntity) по двум критериям:
    // accountId (идентификатор аккаунта) и category (категория расходов).
    // При этом мы хотим получить самый свежий лимит, сортируя результаты по полю limitDateTime в порядке убывания.
    // l — это псевдоним, который мы используем для обращения к полям LimitEntity в запросе.
    //DESC сортирует от большего к меньшему
    //аналог запроса в SQL
    //SELECT * FROM limits
    //WHERE account_id = ? AND category = ?
    //ORDER BY limit_date_time DESC
//    @Query("SELECT l FROM LimitEntity l WHERE l.account = :account AND l.category = :category ORDER BY l.limitDateTime DESC")
//    Optional<LimitEntity> findFirstByAccountIdAndCategory(Long accountId, Category category);
//этот метод можно заменить текущим методом
    Optional<LimitEntity> findFirstByAccountIdAndCategoryOrderByLimitDateTimeDesc(Long accountId, Category category);

}
