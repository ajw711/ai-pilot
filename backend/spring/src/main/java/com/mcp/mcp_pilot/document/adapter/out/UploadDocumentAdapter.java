package com.mcp.mcp_pilot.document.adapter.out;

import com.mcp.mcp_pilot.document.adapter.out.persistence.entity.DocumentFileJpaEntity;
import com.mcp.mcp_pilot.document.adapter.out.persistence.mapper.DocumentFilePersistenceMapper;
import com.mcp.mcp_pilot.document.adapter.out.persistence.repository.DocumentFileJpaRepository;
import com.mcp.mcp_pilot.document.domain.DocumentFile;
import com.mcp.mcp_pilot.document.port.out.DocumentFileRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadDocumentAdapter implements DocumentFileRepositoryPort {

    private final DocumentFileJpaRepository documentFileJpaRepository;

    @Override
    public List<DocumentFile> saveAll(List<DocumentFile> documents) {
        List<DocumentFileJpaEntity> entities = documents.stream()
                        .map(DocumentFilePersistenceMapper::toEntity)
                                .toList();
        List<DocumentFileJpaEntity> savedEntities =
                documentFileJpaRepository.saveAll(entities);
        return savedEntities.stream()
                .map(DocumentFilePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<DocumentFile> findAll() {
        return documentFileJpaRepository.findAll().stream()
                .map(DocumentFilePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<DocumentFile> findById(Long id) {
        return documentFileJpaRepository.findById(id)
                .map(DocumentFilePersistenceMapper::toDomain);
    }

    @Override
    public List<DocumentFile> findAllById(List<Long> ids) {
        return documentFileJpaRepository.findAllById(ids).stream()
                .map(DocumentFilePersistenceMapper::toDomain).toList();
    }

}