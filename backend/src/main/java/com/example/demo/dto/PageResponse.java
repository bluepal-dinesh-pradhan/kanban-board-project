package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import java.util.List;

@Schema(description = "Generic paginated response wrapper.")
public class PageResponse<T> {
    @Schema(description = "List of items for the current page.")
    private List<T> content;
    @Schema(description = "Current page number (0-based).")
    private int page;
    @Schema(description = "Page size.")
    private int size;
    @Schema(description = "Total number of elements across all pages.")
    private long totalElements;
    @Schema(description = "Total number of pages.")
    private int totalPages;
    @Schema(description = "Whether there is a next page.")
    private boolean hasNext;
    @Schema(description = "Whether there is a previous page.")
    private boolean hasPrevious;

    public PageResponse() {}

    public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }
}
