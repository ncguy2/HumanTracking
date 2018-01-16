package net.ncguy.api.data;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.ncguy.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TrackedPoint {

    protected List<Hint> hints;

    protected final Vector2 screenPos = new Vector2();
    public float angle;

    public boolean isDirty = true;
    public boolean useWorldCoords = false;
    public Vector3 worldPos = new Vector3();
    public Vector3 worldDir = new Vector3();

    public TrackedPoint(TrackedPoint other) {
        this.hints = new ArrayList<>();
        this.hints.addAll(other.hints);
        this.screenPos.set(other.screenPos);
    }

    public Quaternion WorldDirQuat() {
        return new Quaternion().setEulerAnglesRad(-worldDir.y, worldDir.x, -worldDir.z);
    }

    public Hint GetPrimaryHint() {
        return hints.get(0);
    }

    public TrackedPoint() {
        this.hints = new ArrayList<>();
    }

    public TrackedPoint(Hint... hints) {
        this.hints = Arrays.asList(hints);
    }

    public Vector2 GetScreenPos() {
        return screenPos;
    }

    public String GetCurrentGuess() {
        Optional<Hint> best = hints.stream().sorted(this::CompareHints).findFirst();

        String res = best.orElse(Hint.UNKNOWN).toString();

        String[] sections = res.split("\\.");

        StringBuilder sb = new StringBuilder();

        // index 0 == "HINT"
        for (int i = sections.length - 1; i >= 1; i--)
            sb.append(StringUtils.ToTitleCase(sections[i])).append(" ");

        return sb.toString();
    }

    public int CountOccurance(Hint str, String target) {
        return str.toString().length() - str.toString().replace(target, "").length();
    }

    public int CompareHints(Hint s1, Hint s2) {
        int s1Count = CountOccurance(s1, ".");
        int s2Count = CountOccurance(s2, ".");
        return Integer.compare(s1Count, s2Count);
    }

    public void AddHint(Hint hint) {
        this.hints.add(hint);
    }

    public enum Hint {
        UNKNOWN("HINT.UNKNOWN"),
        GLOBAL("HINT.GLOBAL"),

        HAND("HINT.HAND"),
        LEFT_HAND("HINT.HAND.LEFT"),
        RIGHT_HAND("HINT.HAND.RIGHT"),

        HEAD("HINT.HEAD"),
        JAW("HINT.HEAD.JAW.BOTTOM"),
        MOUTH("HINT.HEAD.JAW.TOP"),
        MOUTH_LEFT("HINT.HEAD.JAW.LEFT"),
        MOUTH_RIGHT("HINT.HEAD.JAW.RIGHT"),

        EYE("HINT.EYE"),
        LEFT_EYE("HINT.EYE.LEFT"),
        LEFT_EYE_CORNER("HINT.EYE.LEFT.CORNER"),
        TOP_LEFT_EYE("HINT.EYE.LEFT.TOP"),
        BOT_LEFT_EYE("HINT.EYE.LEFT.BOTTOM"),

        RIGHT_EYE("HINT.EYE.RIGHT"),
        RIGHT_EYE_CORNER("HINT.EYE.RIGHT.CORNER"),
        TOP_RIGHT_EYE("HINT.EYE.RIGHT.TOP"),
        BOT_RIGHT_EYE("HINT.EYE.RIGHT.BOTTOM"),

        SHOULDER("HINT.SHOULDER"),
        LEFT_SHOULDER("HINT.SHOULDER.LEFT"),
        RIGHT_SHOULDER("HINT.SHOULDER.RIGHT"),

        TORSO("HINT.TORSO"),
        HIPS("HINT.HIPS"),

        KNEE("HINT.KNEE"),
        LEFT_KNEE("HINT.KNEE.LEFT"),
        RIGHT_KNEE("HINT.KNEE.RIGHT"),

        FOOT("HINT.FOOT"),
        LEFT_FOOT("HINT.FOOT.LEFT"),
        RIGHT_FOOT("HINT.FOOT.RIGHT"),
        ;

        private final String hintStr;
        Hint(String hintStr) {
            this.hintStr = hintStr;
        }

        public byte ToByte() {
            return (byte) (ordinal() & 0xFF);
        }

        public int Score() {
            return (hintStr.length() - hintStr.replace(".", "").length()) + 1;
        }

        @Override
        public String toString() {
            return hintStr;
        }
    }

}
