package com.jammit_be.common.entity;

import com.jammit_be.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NamedEntityGraph(
    name = "BaseUserEntity.withUsers",
    attributeNodes = {
        @NamedAttributeNode("createdBy"),
        @NamedAttributeNode("updatedBy")
    }
)
public class BaseUserEntity extends BaseEntity {

  @CreatedBy
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  protected User createdBy;

  @LastModifiedBy
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  protected User updatedBy;
}
