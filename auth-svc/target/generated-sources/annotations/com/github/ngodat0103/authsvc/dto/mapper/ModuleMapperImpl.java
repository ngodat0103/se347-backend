package com.github.ngodat0103.authsvc.dto.mapper;

import com.github.ngodat0103.authsvc.dto.ModuleDto;
import com.github.ngodat0103.authsvc.persistence.entity.Module;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-11-13T22:22:29+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.13 (Ubuntu)"
)
@Component
public class ModuleMapperImpl implements ModuleMapper {

    @Override
    public ModuleDto toDto(Module entity) {
        if ( entity == null ) {
            return null;
        }

        ModuleDto.ModuleDtoBuilder moduleDto = ModuleDto.builder();

        moduleDto.id( entity.getId() );
        moduleDto.name( entity.getName() );

        return moduleDto.build();
    }

    @Override
    public Module toEntity(ModuleDto dto) {
        if ( dto == null ) {
            return null;
        }

        Module module = new Module();

        module.setId( dto.getId() );
        module.setName( dto.getName() );

        return module;
    }
}
