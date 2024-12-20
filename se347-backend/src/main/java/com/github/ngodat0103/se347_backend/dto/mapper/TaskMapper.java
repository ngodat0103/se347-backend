package com.github.ngodat0103.se347_backend.dto.mapper;

import com.github.ngodat0103.se347_backend.dto.task.TaskDto;
import com.github.ngodat0103.se347_backend.persistence.document.task.Task;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {
  TaskDto toDto(Task task);

  Task toDocument(TaskDto taskDto);
}
