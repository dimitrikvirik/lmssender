package git.dimitrikvirik.lmssender;

import lombok.Data;
import lombok.ToString;

import java.util.Objects;

@Data
@ToString
public class StudyItem {

    private boolean isDon;

    private String type;

    private String professor;

    private String name;

    private String selectType;

    private Integer credit;

    private Integer score;

    private String scoreComponent;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudyItem)) return false;
        StudyItem studyItem = (StudyItem) o;
        return isDon() == studyItem.isDon() && Objects.equals(getType(), studyItem.getType()) && getProfessor().equals(studyItem.getProfessor()) && getName().equals(studyItem.getName()) && getSelectType().equals(studyItem.getSelectType()) && getCredit().equals(studyItem.getCredit()) && getScore().equals(studyItem.getScore()) && getScoreComponent().equals(studyItem.getScoreComponent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isDon(), getType(), getProfessor(), getName(), getSelectType(), getCredit(), getScore(), getScoreComponent());
    }
}
