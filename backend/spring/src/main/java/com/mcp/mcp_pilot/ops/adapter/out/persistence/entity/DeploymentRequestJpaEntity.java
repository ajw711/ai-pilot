package com.mcp.mcp_pilot.ops.adapter.out.persistence.entity;

import com.mcp.mcp_pilot.common.entitiy.BaseEntity;
import com.mcp.mcp_pilot.ops.port.in.dto.DeploymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@SQLRestriction("deleted_at IS NULL")
@Table(name = "deployment_request")
@Entity
@Getter
@NoArgsConstructor
public class DeploymentRequestJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String trackingId;

    @Column(nullable = false, length = 100)
    private String appName;

    @Column(nullable = false, length = 200)
    private String image;

    @Column(length = 100)
    private String tag;

    @Column(nullable = false)
    private Integer replicas;

    @Column(nullable = false, length = 100)
    private String namespace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeploymentStatus status;

    private LocalDateTime deletedAt;

    private DeploymentRequestJpaEntity(String trackingId,
                                       String appName,
                                       String image,
                                       String tag,
                                       Integer replicas,
                                       String namespace,
                                       DeploymentStatus status,
                                       LocalDateTime deletedAt) {
        this.trackingId = trackingId;
        this.appName = appName;
        this.image = image;
        this.tag = tag;
        this.replicas = replicas;
        this.namespace = namespace;
        this.status = status;
        this.deletedAt = deletedAt;
    }

    public static DeploymentRequestJpaEntity create(String trackingId,
                                                    String appName,
                                                    String image,
                                                    String tag,
                                                    Integer replicas,
                                                    String namespace,
                                                    DeploymentStatus status,
                                                    LocalDateTime deletedAt) {
        return new DeploymentRequestJpaEntity(
                trackingId,
                appName,
                image,
                tag,
                replicas,
                namespace,
                status,
                deletedAt
        );
    }


    public void markPublished() {
        this.status = DeploymentStatus.PUBLISHED;
    }

    public void markDeploying() {
        this.status = DeploymentStatus.DEPLOYING;
    }

    public void markFailed() {
        this.status = DeploymentStatus.FAILED;
    }
}