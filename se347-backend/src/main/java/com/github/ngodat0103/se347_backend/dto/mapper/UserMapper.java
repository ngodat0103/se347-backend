package com.github.ngodat0103.se347_backend.dto.mapper;

import com.github.ngodat0103.se347_backend.dto.user.UserDto;
import com.github.ngodat0103.se347_backend.persistence.document.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
  UserDto toDto(User user);

  User toDocument(UserDto dto);
}
