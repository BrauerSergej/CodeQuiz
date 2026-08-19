package dev.codequiz.repository;

import dev.codequiz.domain.QuizAttempt;
import dev.codequiz.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // История попыток пользователя, от самой новой к старой —
    // используется для экрана "мои прошлые прохождения".
    List<QuizAttempt> findByAccountOrderByStartedAtDesc(User account);

    // Поиск попытки по id, но только если она принадлежит конкретному аккаунту —
    // это защита от IDOR (Insecure Direct Object Reference): без такой проверки
    // пользователь мог бы подставить в URL/запрос чужой id попытки и получить
    // доступ к чужим результатам квиза.
    Optional<QuizAttempt> findByIdAndAccount(Long id, User account);
}