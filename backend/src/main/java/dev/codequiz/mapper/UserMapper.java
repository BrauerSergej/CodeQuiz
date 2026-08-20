package dev.codequiz.mapper;

import dev.codequiz.domain.User;
import dev.codequiz.dto.user.UserDto;
import dev.codequiz.dto.user.UserSaveDto;
import dev.codequiz.dto.user.UserUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

// componentModel = "spring" — MapStruct сгенерирует реализацию как Spring Bean
// (UserMapperImpl с @Component), чтобы можно было внедрить его через
// @Autowired/конструктор в сервис, как обычный bean.
@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entity -> DTO. Поля сопоставляются автоматически по совпадению имён
    // (id, userName, email, phone, accountRole, xp, accountStatus, createdAt).
    // passwordHash в UserDto просто нет как поля, поэтому MapStruct его
    // не переносит — компилятор сам не даст утечь чувствительным данным.
    UserDto toDto(User user);

    // UserSaveDto -> Entity. Используется при регистрации, когда пароль
    // уже захеширован и собран UserSaveDto (см. пояснение в самом DTO).
    User toEntity(UserSaveDto saveDto);

    // Частичное обновление уже существующей сущности данными из UserUpdateDto.
    // @MappingTarget означает "не создавай новый объект, а измени переданный" —
    // MapStruct сгенерирует user.setUserName(dto.getUserName()) и
    // user.setPhone(dto.getPhone()), остальные поля entity останутся нетронутыми.
    void updateEntityFromDto(UserUpdateDto dto, @MappingTarget User user);
}