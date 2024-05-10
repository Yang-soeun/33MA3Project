package softeer.be33ma3.domain;

import jakarta.persistence.*;
import lombok.Getter;
import softeer.be33ma3.dto.request.PostCreateDto;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Post extends BaseTimeEntity{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private String modelName;

    private String carType;

    private boolean done;       //낙찰 시 or 마감 기한 끝난 경우 true

    private int deadline;

    private String contents;

    private String repairService;

    private String tuneUpService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Offer> offers = new ArrayList<>();

    //셍성 메소드
    public static Post createPost(PostCreateDto postCreateDto, Region region, Member member) {
        Post post = new Post();

        post.modelName = postCreateDto.getModelName();
        post.carType = postCreateDto.getCarType();
        post.deadline = postCreateDto.getDeadline();
        post.contents = postCreateDto.getContents();
        post.repairService = postCreateDto.getRepairService();
        post.tuneUpService = postCreateDto.getTuneUpService();
        post.region = region;
        post.member = member;

        return post;
    }

    public void setDone() {
        this.done = true;
    }

    public void editPost(PostCreateDto postEditDto, Region region) {
        this.carType = postEditDto.getCarType();
        this.deadline = postEditDto.getDeadline();
        this.contents = postEditDto.getContents();
        this.repairService = postEditDto.getRepairService();
        this.tuneUpService = postEditDto.getTuneUpService();
        this.region = region;
    }

    public boolean isWriter(Long memberId) {
        return member.getMemberId().equals(memberId);
    }
}
