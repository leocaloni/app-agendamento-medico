package com.pi.agendamento.dto.response;

import org.springframework.core.io.Resource;

// arquivo do exame pronto para download
public record ExamFileResponse(Resource resource, String fileName, String contentType, long sizeBytes) {
}
