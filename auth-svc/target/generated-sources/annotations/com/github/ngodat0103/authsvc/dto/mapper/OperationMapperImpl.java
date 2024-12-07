package com.github.ngodat0103.authsvc.dto.mapper;

import com.github.ngodat0103.authsvc.dto.OperationDto;
import com.github.ngodat0103.authsvc.persistence.entity.Operation;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-11-13T22:22:29+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.13 (Ubuntu)"
)
@Component
public class OperationMapperImpl implements OperationMapper {

    @Override
    public OperationDto toDto(Operation entity) {
        if ( entity == null ) {
            return null;
        }

        OperationDto.OperationDtoBuilder operationDto = OperationDto.builder();

        operationDto.id( entity.getId() );
        operationDto.name( entity.getName() );

        return operationDto.build();
    }

    @Override
    public Operation toEntity(OperationDto dto) {
        if ( dto == null ) {
            return null;
        }

        Operation operation = new Operation();

        operation.setId( dto.getId() );
        operation.setName( dto.getName() );

        return operation;
    }
}
