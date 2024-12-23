package com.github.ngodat0103.se347_backend.service.task;

import com.github.ngodat0103.se347_backend.dto.task.CreateTaskDto;
import com.github.ngodat0103.se347_backend.dto.task.ResponseTaskDto;
import java.util.Set;
import org.springframework.data.domain.Sort;

public interface TaskService {
  ResponseTaskDto createTask(String workspaceId, String projectId, CreateTaskDto createTaskDto);

  Set<ResponseTaskDto> getTasks(String workspaceId, String projectId);
}
