package com.github.ngodat0103.authsvc.dto.mapper;

import com.github.ngodat0103.authsvc.dto.permission.PermissionDto;
import com.github.ngodat0103.authsvc.persistence.entity.Permission;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-11-13T22:22:29+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.13 (Ubuntu)"
)
@Component
public class PermissionMapperImpl implements PermissionMapper {

    @Override
    public PermissionDto toDto(Permission entity) {
        if ( entity == null ) {
            return null;
        }

        PermissionDto.PermissionDtoBuilder permissionDto = PermissionDto.builder();

        permissionDto.id( entity.getId() );
        permissionDto.name( entity.getName() );

        return permissionDto.build();
    }

    @Override
    public Permission toEntity(PermissionDto dto) {
        if ( dto == null ) {
            return null;
        }

        Permission permission = new Permission();

        permission.setId( dto.getId() );
        permission.setName( dto.getName() );

        return permission;
    }
}
