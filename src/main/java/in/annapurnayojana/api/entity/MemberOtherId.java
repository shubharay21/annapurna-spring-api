package in.annapurnayojana.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "member_other_ids", schema = "dbt_apy")
@Getter
@Setter
public class MemberOtherId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_id", nullable = false)
    private FamilyMember member;

    @Column(nullable = false)
    private String idType = "";

    private String issueDate;

    @Column(name = "lgd_district_code")
    private Integer district;
}
