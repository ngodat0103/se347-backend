package com.github.ngodat0103.se347_backend.dto.mapper;

import com.github.ngodat0103.se347_backend.dto.task.CreateTaskDto;
import com.github.ngodat0103.se347_backend.dto.task.ResponseTaskDto;
import com.github.ngodat0103.se347_backend.dto.task.UpdateTaskDto;
import com.github.ngodat0103.se347_backend.persistence.document.task.Task;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {
  ResponseTaskDto toDto(Task task);

  Task toDocument(CreateTaskDto createTaskDto);

  Task toDocument(UpdateTaskDto updateTaskDto);
}
