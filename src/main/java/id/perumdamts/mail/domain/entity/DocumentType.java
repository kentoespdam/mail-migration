package id.perumdamts.mail.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "jenis_dokumen")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "jenis_dokumen")
    private String name;

    private Integer status;
}
