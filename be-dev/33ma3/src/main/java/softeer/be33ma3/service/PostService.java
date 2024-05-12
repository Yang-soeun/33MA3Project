package softeer.be33ma3.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import softeer.be33ma3.domain.*;
import softeer.be33ma3.dto.request.PostCreateDto;
import softeer.be33ma3.dto.response.*;
import softeer.be33ma3.exception.BusinessException;
import softeer.be33ma3.repository.*;
import softeer.be33ma3.repository.offer.OfferRepository;
import softeer.be33ma3.repository.post.PostRepository;
import softeer.be33ma3.repository.review.ReviewRepository;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

import static softeer.be33ma3.exception.ErrorCode.*;
import static softeer.be33ma3.utils.StringParser.stringCommaParsing;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final OfferRepository offerRepository;
    private final CenterRepository centerRepository;
    private final PostRepository postRepository;
    private final RegionRepository regionRepository;
    private final PostPerCenterRepository postPerCenterRepository;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    // 게시글 목록 조회
    public List<PostThumbnailDto> showPosts(Boolean mine, Boolean done, String region, String repair, String tuneUp, Member member) {
        List<String> regions = stringCommaParsing(region);
        List<String> repairs = stringCommaParsing(repair);
        List<String> tuneUps = stringCommaParsing(tuneUp);
        List<Long> postIds = null;
        if(member != null && member.isCenter()) {   //센터인 경우
            Center center = centerRepository.findByMember_MemberId(member.getMemberId()).orElseThrow(() -> new BusinessException(NOT_FOUND_CENTER));
            postIds = postPerCenterRepository.findPostIdsByCenterId(center.getCenterId());
        }
        Long writerId = null;
        if(Boolean.TRUE.equals(mine) && member != null && member.isClient()) {
            writerId = member.getMemberId();
        }
        List<Post> posts = postRepository.findAllByConditions(writerId, done, regions, repairs, tuneUps, postIds);
        return fromPostList(posts);
    }

    private List<PostThumbnailDto> fromPostList(List<Post> posts) {
        return posts.stream()
                .map(PostThumbnailDto::fromEntity).toList();
    }

    @Transactional
    public Long createPost(Member currentMember, PostCreateDto postCreateDto, List<MultipartFile> multipartFiles) {
        if(currentMember.isCenter()){   //센터인 경우 글 작성 불가능
            throw new BusinessException(POST_CREATION_DISABLED);
        }

        Post post = Post.createPost(postCreateDto, getRegion(postCreateDto.getLocation()), currentMember);
        Post savedPost = postRepository.save(post);
        centerAndPostMapping(postCreateDto, savedPost);        //정비소랑 게시물 매핑

        if(multipartFiles != null){     //게시물에 이미지가 있는 경우
            List<Image> images = imageService.saveImages(multipartFiles);            //이미지 저장
            images.forEach(image -> image.setPost(savedPost));            //이미지랑 게시물 매핑하기
        }

        return savedPost.getPostId();
    }

    @Transactional
    public void editPost(Member member, Long postId, PostCreateDto postEditDto) {
        Post post = validPostAndMember(member, postId);
        Region region = getRegion(postEditDto.getLocation());
        post.editPost(postEditDto, region);
    }

    private Region getRegion(String location) {
        Pattern pattern = Pattern.compile("\\b([^\\s]+구)\\b");
        Matcher matcher = pattern.matcher(location);

        if (matcher.find()) {
            return regionRepository.findByRegionName(matcher.group()).orElseThrow(() -> new BusinessException(NOT_FOUND_REGION));
        }

        throw new BusinessException(NO_DISTRICT_IN_ADDRESS);
    }

    @Transactional
    public void deletePost(Member member, Long postId) {
        Post post = validPostAndMember(member, postId);
        //이미지 삭제
        List<Image> images = post.getImages();
        imageService.deleteImage(images);   //S3에서 이미지 삭제
        imageRepository.deleteAll(images);  //db에 저장된 이미지 삭제

        postRepository.delete(post);    //게시글 삭제
    }

    private Post validPostAndMember(Member member, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(NOT_FOUND_POST));

        if (!post.isWriter(member.getMemberId())) {     //작성자가 아닌 경우
            throw new BusinessException(AUTHOR_ONLY_ACCESS);
        }
        if (!post.getOffers().isEmpty()) { //댓글이 있는 경우(경매 시작 후)
            throw new BusinessException(PRE_AUCTION_ONLY);
        }
        return post;
    }

    public Object showPost(Long postId, Member member) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(NOT_FOUND_POST));
        if (member == null && !post.isDone()) {     //진행중인 경매는 로그인 한 사용자만 볼 수 있다.
            throw new BusinessException(LOGIN_REQUIRED);
        }

        PostDetailDto postDetailDto = PostDetailDto.fromEntity(post);
        if(post.isDone() || (member!=null && post.isWriter(member.getMemberId()))) {    //경매가 완료 되었거나, 작성자인 경우
            List<Long> centerIds = offerRepository.findCenterMemberIdsByPost_PostId(postId);
            List<OfferDetailDto> offerDetailDtos = offerRepository.findOfferAndAvgPriceByCenterId(centerIds);
            return new PostWithOffersDto(postDetailDto, offerDetailDtos);
        }

        //경매가 진행 중이고 작성자가 아닌 유저의 접근일 경우
        return createPostWithPriceDto(postId, member, postDetailDto);
    }

    private PostWithAvgPriceDto createPostWithPriceDto(Long postId, Member member, PostDetailDto postDetailDto) {
        Double avgPrice = offerRepository.findAvgPriceByPostId(postId).orElse(0.0);
        PostWithAvgPriceDto postWithAvgPriceDto = new PostWithAvgPriceDto(postDetailDto, Math.round( avgPrice * 10 ) / 10.0);
        // 견적을 작성한 이력이 있는 서비스 센터인 경우 작성한 견적 가져오기
        OfferDetailDto offerDetailDto = getCenterOffer(postId, member);
        postWithAvgPriceDto.setOfferDetailDto(offerDetailDto);

        return postWithAvgPriceDto;
    }

    // 견적을 작성한 이력이 있는 서비스 센터일 경우 작성한 견적 반환
    // 해당사항 없을 경우 null 반환(클라이언트 또는 견적을 작성한적 없는 경우)
    private OfferDetailDto getCenterOffer(Long postId, Member member) {
        if (!member.isClient()) {
            return offerRepository.findByPost_PostIdAndCenter_MemberId(postId, member.getMemberId())
                    .map(offer -> {
                        Double score = reviewRepository.findAvgScoreByCenterId(offer.getCenter().getMemberId()).orElse(0.0);
                        return OfferDetailDto.fromEntity(offer, score);
                    })
                    .orElse(null);
        }

        return null;
    }

    private void centerAndPostMapping(PostCreateDto postCreateDto, Post savedPost) {
        List<Center> centers = centerRepository.findAllById(postCreateDto.getCenters());

        List<PostPerCenter> postPerCenters = centers.stream()
                .map(center -> new PostPerCenter(center, savedPost))
                .toList();

        postPerCenterRepository.saveAll(postPerCenters);
    }
}
