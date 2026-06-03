package id.perumdamts.mail.entity.core;

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

    @Column(name = "ma_loc_building")
    private Integer building;

    @Column(name = "ma_loc_floor")
    private Integer floor;

    @Column(name = "ma_loc_room")
    private Integer room;

    @Column(name = "ma_loc_rack", length = 32)
    private String rack;

    @Column(name = "ma_loc_tier", length = 32)
    private String shelf; // Using Java field name 'shelf' but mapping to 'ma_loc_tier'

    @Column(name = "ma_loc_box", length = 32)
    private String box;
}
