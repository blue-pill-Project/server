package bluepill.server.util;

import bluepill.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NicknameGenerator {

    private final UserRepository userRepository;

    private static final List<String> ADJECTIVES = List.of("뻘쭘한", "킹받는", "하찮은", "꼬질꼬질한", "뽀짝한", "멍청한", "쭈굴쭈굴한", "깜찍한",
            "어리바리한", "얄미운", "능글맞은", "허접한", "오염된", "엉뚱한", "어설픈", "처량한",
            "뒤죽박죽한", "쪼잔한", "옹졸한", "까다로운", "골때리는", "귀여운", "불쌍한", "징그러운",
            "짭쪼름한", "유영하는", "텁텁한", "멍때리는", "우스꽝스러운", "엉성한", "시시콜콜한",
            "구질구질한", "너덜너덜한", "후줄근한", "울퉁불퉁한", "삐딱한", "어수선한", "시끄러운",
            "시큰둥한", "요란한", "폭신한", "꾸덕한", "바삭한", "눅눅한", "흐물흐물한", "뾰루퉁한",
            "뚱한", "삐진", "따수운", "따듯한", "심술궂은", "투덜거리는", "틱틱대는", "새침한",
            "달달한", "찰진", "신박한", "웃픈", "짠내나는", "춤추는", "노래하는", "낭만있는",
            "나사빠진", "어리석은", "깐깐한", "의기소침한", "처참한", "싸늘한", "초췌한", "까칠한",
            "뻔뻔한", "무해한", "순수한", "방랑하는", "자유로운", "몽환적인", "둠칫둠칫하는", "신난",
            "세상편한", "베베꼬인", "당당한", "영악한", "느끼한", "리듬타는");

    private static final List<String> NOUNS = List.of(
            "라쿤", "수달", "고양이", "강아지", "시바견", "쿼카", "해파리", "반달가슴곰",
            "마카롱", "밀크티", "아메리카노", "꿀단지", "밤하늘", "오로라", "푸딩", "타자기",
            "펭귄", "햄스터", "고슴도치", "관종", "직장인", "학생", "사람", "애주가",
            "모험가", "일기장", "사차원", "확성기", "불도저", "포크레인", "만년필", "나침반",
            "병아리", "돋보기", "우주", "사막여우", "알파카", "돌고래", "웰시코기", "기니피그",
            "카피바라", "타조", "앵무조개", "바다거북", "존재", "따오기", "지구", "키조개",
            "낙타", "달팽이", "양배추", "아스파라거스", "파프리카", "감자", "고구마", "생강",
            "고수", "왈라비", "대파", "완두콩", "마라탕", "공기밥", "안경", "탕후루",
            "은둔고수", "개척자", "방랑자", "수호자", "파괴자", "추적자", "국밥", "수면양말",
            "꼬마유령", "외계인", "텔레파시", "효자손", "슬리퍼", "뻥튀기", "뽁뽁이", "시간여행자",
            "블랙홀", "우주인", "물만두", "도토리", "비눗방울", "그림자", "바코드", "종이비행기",
            "시한폭탄", "스노우볼", "허수아비", "인공눈물", "치즈스틱", "만화책", "누룽지",
            "삼각김밥", "메추리알", "연금술사", "유리구슬", "길치", "행인", "스파이", "츄러스",
            "달고나", "바람개비", "야광별"
    );

    private static final Random RANDOM = new Random();

    public String generate(){
        for (int i = 0; i < 5; i++) {
            String adjective = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
            String noun = NOUNS.get(RANDOM.nextInt(NOUNS.size()));
            int number = RANDOM.nextInt(9000) + 1000;
            String nickname = adjective + noun + number;

            if(!userRepository.existsByNickname(nickname)){
                return nickname;
            }
        }
        //5회 모두 중복 시 fallback
        return ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()))
                + NOUNS.get(RANDOM.nextInt(NOUNS.size()))
                + UUID.randomUUID().toString().substring(0, 5);

    }
}
