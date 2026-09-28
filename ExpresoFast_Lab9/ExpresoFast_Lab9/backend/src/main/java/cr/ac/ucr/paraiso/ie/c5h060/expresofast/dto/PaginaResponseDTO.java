package cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PaginaResponseDTO<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty) {

    public static <T> PaginaResponseDTO<T> from(Page<T> page) {
        return new PaginaResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty());
    }
}
