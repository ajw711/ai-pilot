package com.mcp.mcp_pilot.ai.vector.entity;


import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.common.entitiy.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
        name = "vector_source",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_vector_source",
                        columnNames = {"source_type,", "source_id"}
                )
        }
)
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VectorSourceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private VectorTargetType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    public static VectorSourceEntity of(
            VectorTargetType sourceType,
            Long sourceId
    ) {
        return new VectorSourceEntity(
                null,
                sourceType,
                sourceId
        );
    }
}
