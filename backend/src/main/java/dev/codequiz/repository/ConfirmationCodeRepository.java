package dev.codequiz.repository;

import dev.codequiz.domain.ConfirmationCode;
import dev.codequiz.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfirmationCodeRepository extends JpaRepository<ConfirmationCode, Long> {

    // Поиск кода по аккаунту и его хешу — используется при подтверждении email:
    // пользователь вводит код, вы хешируете введённое значение тем же алгоритмом
    // и ищете совпадение именно для этого аккаунта (а не по всей таблице).
    Optional<ConfirmationCode> findByAccountAndCodeHash(User account, String codeHash);

    // Последний код, выданный аккаунту (сортировка по дате создания, берём самый свежий).
    // Нужен, чтобы при повторной отправке кода не плодить активные коды бесконтрольно —
    // можно проверить/инвалидировать предыдущий перед выдачей нового.
    Optional<ConfirmationCode> findTopByAccountOrderByCreatedAtDesc(User account);
}