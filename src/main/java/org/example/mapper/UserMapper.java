package org.example.mapper;

import org.example.dto.UserRegistrationRequest;
import org.example.dto.UserResponse;
import org.example.model.User;
import org.example.model.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // 1. Základní mapování entity na DTO
    UserResponse toDto(User user);
    /*
    // 2. Mapování seznamů
    List<UserResponse> toDtoList(List<User> users);
    /*
    // 3. Pokročilejší mapování
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + ' ' + user.getLastName())")
    @Mapping(target = "active", source = "enabled") // V DB je 'enabled', v DTO chcete 'active'
    @Mapping(target = "roleNames", source = "roles", qualifiedByName = "rolesToStrings")
    UserResponse toDetailedDto(User user);
    */
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
