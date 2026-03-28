package id.perumdamts.mail.entity.master;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "allowed_file_type")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AllowedFileType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String context;

    @Column(nullable = false, length = 20)
    private String extension;

    @Column(name = "max_size_mb", nullable = false)
    private Integer maxSizeMb;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
