package com.github.ngodat0103.se347_backend.service;

public interface BaseService<DTO> {

  DTO create(DTO dto);

  DTO update(String id, DTO dto);

  String delete(String  id);
}
