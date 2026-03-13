package com.spring.security.domain.model.entity.base;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@Schema(title = "基础信息字段")
public abstract class BaseEntity {

    @Id
    @Schema(title = "主键ID")
    @Column(comment = "主键ID", name = "id", nullable = false)
    private Long id;
}
