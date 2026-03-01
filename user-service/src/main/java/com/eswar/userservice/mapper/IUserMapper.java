package com.eswar.userservice.mapper;

import com.eswar.userservice.dto.UserRequestDto;
import com.eswar.userservice.dto.UserResponseDto;
import com.eswar.userservice.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IUserMapper {
    UserEntity toEntity(UserRequestDto request);
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "lastSeen", source = "lastSeen")
    UserResponseDto toResponse(UserEntity entity);
}
