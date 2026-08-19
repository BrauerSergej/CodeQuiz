package dev.codequiz.repository;

import dev.codequiz.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Поиск пользователя по email — используется при логине по email.
    // Optional вместо null, так как пользователь может не существовать.
    Optional<User> findByEmail(String email);

    // Поиск пользователя по username — используется, если логин возможен по нику,
    // а не только по email.
    Optional<User> findByUserName(String userName);

    // Проверка занятости email при регистрации — до попытки сохранить нового
    // пользователя. Дешевле и явнее, чем ловить исключение от unique-constraint в БД.
    boolean existsByEmail(String email);

    // Проверка занятости username при регистрации, аналогично existsByEmail.
    boolean existsByUserName(String userName);
}