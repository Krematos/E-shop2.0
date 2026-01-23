package krematos.mapper;

import krematos.dto.user.UserResponse;
import krematos.model.User;
import krematos.model.enums.Role;
import krematos.dto.user.UserRegistrationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // 1. Základní mapování entity na DTO
    UserResponse toDto(User user);

    // Inverzní mapování (např. při registraci)
    @Mapping(target = "id", ignore = true) // ID generuje databáze, ignoruje ho
    @Mapping(target = "roles", ignore = true) // Role nastavuje v servise, ne z JSONu
    User toEntity(UserRegistrationRequest request);

    @Named("rolesToStrings")
    static Set<String> rolesToStrings(Set<Role> roles) {
        return roles.stream()
                .map(Role::name)
                .collect(java.util.stream.Collectors.toSet());
    }
}
