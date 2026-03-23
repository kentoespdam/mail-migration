package id.perumdamts.mail.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ArchiveLocation {

    @Column(name = "ma_rack", length = 32)
    private String rack;

    @Column(name = "ma_shelf", length = 32)
    private String shelf;

    @Column(name = "ma_box", length = 32)
    private String box;

    @Column(name = "ma_folder_pos", length = 32)
    private String folderPosition;
}
