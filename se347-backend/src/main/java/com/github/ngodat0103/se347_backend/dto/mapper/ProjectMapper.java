package com.github.ngodat0103.se347_backend.dto.mapper;

import com.github.ngodat0103.se347_backend.dto.project.ProjectDto;
import com.github.ngodat0103.se347_backend.persistence.document.project.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectDto toDto(Project project);
    Project toDocument(ProjectDto projectDto);
}
