package com.github.ngodat0103.authsvc.dto.mapper;

import com.github.ngodat0103.authsvc.dto.role.RoleDto;
import com.github.ngodat0103.authsvc.persistence.entity.role.Role;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-11-13T22:22:29+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.13 (Ubuntu)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public RoleDto toDto(Role entity) {
        if ( entity == null ) {
            return null;
        }

        RoleDto.RoleDtoBuilder roleDto = RoleDto.builder();

        roleDto.id( entity.getId() );
        roleDto.name( entity.getName() );

        return roleDto.build();
    }

    @Override
    public Role toEntity(RoleDto dto) {
        if ( dto == null ) {
            return null;
        }

        Role role = new Role();

        role.setId( dto.getId() );
        role.setName( dto.getName() );

        return role;
    }
}
