package com.spring.security.web.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

@JsonPropertyOrder({"content", "page"})
public record PagedModel<T>(Page<@NonNull T> page) {

    public PagedModel(Page<@NonNull T> page) {
        Assert.notNull(page, "Page must not be null");
        this.page = page;
    }

    @JsonProperty("content")
    public List<T> getContent() {
        return page.getContent();
    }

    @JsonProperty("page")
    public PageMetadata getMetadata() {
        return new PageMetadata(
                page.getSize(),
                Math.addExact(page.getNumber(), 1),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext());
    }

    public record PageMetadata(
            long size,
            long number,
            long totalElements,
            long totalPages,
            boolean isFirst,
            boolean isLast,
            boolean hasNext) {

        public PageMetadata {
            Assert.isTrue(size > -1, "Size must not be negative!");
            Assert.isTrue(number > -1, "Number must not be negative!");
            Assert.isTrue(totalElements > -1, "Total elements must not be negative!");
            Assert.isTrue(totalPages > -1, "Total pages must not be negative!");
            Assert.notNull(isFirst, "IsFirst must not be null");
            Assert.notNull(isLast, "IsLast must not be null");
            Assert.notNull(hasNext, "HasNext must not be null");
        }
    }
}
