package dev.codequiz.service;

import dev.codequiz.domain.User;
import dev.codequiz.dto.user.PasswordChangeDto;
import dev.codequiz.dto.user.UserDto;
import dev.codequiz.dto.user.UserStatusUpdateDto;
import dev.codequiz.dto.user.UserUpdateDto;
import dev.codequiz.exception.InvalidPasswordException;
import dev.codequiz.exception.UserNotFoundException;
import dev.codequiz.exception.UsernameAlreadyExistsException;
import dev.codequiz.mapper.UserMapper;
import dev.codequiz.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // readOnly = true — подсказка Hibernate не отслеживать изменения полей
    // сущности (dirty checking) для этой транзакции, раз мы точно ничего
    // не будем сохранять. Небольшой, но бесплатный выигрыш в производительности
    // на чтении.
    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        User user = findUserOrThrow(id);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateProfile(Long id, UserUpdateDto dto) {
        User user = findUserOrThrow(id);

        // Проверяем занятость нового userName, только если он реально
        // меняется — иначе пользователь, сохраняющий профиль без изменения
        // имени, каждый раз получал бы ложное "имя уже занято" (существующая
        // запись в БД — это же его собственная строка).
        boolean userNameChanged = !user.getUserName().equals(dto.getUserName());
        if (userNameChanged && userRepository.existsByUserName(dto.getUserName())) {
            throw new UsernameAlreadyExistsException("Имя пользователя уже занято: " + dto.getUserName());
        }

        userMapper.updateEntityFromDto(dto, user);
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Transactional
    public void changePassword(Long id, PasswordChangeDto dto) {
        User user = findUserOrThrow(id);

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Текущий пароль указан неверно");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // Отдельный метод, а не часть updateProfile — доступ к нему в контроллере
    // будет закрыт отдельной проверкой прав (только ADMIN), в отличие от
    // updateProfile, который пользователь вызывает сам для себя. Смешивать
    // их в один метод означало бы либо дать всем менять чужой статус, либо
    // городить условную проверку роли прямо внутри сервисного метода.
    @Transactional
    public UserDto updateStatus(Long id, UserStatusUpdateDto dto) {
        User user = findUserOrThrow(id);

        user.setAccountStatus(dto.getAccountStatus());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        return userMapper.toDto(user);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: id=" + id));
    }
}