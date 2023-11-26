package tele.crypt.RSA;
import java.util.HashMap;
import java.util.Map;

public class Alphabet {
    private final Map<Character, Integer> DICT = new HashMap<>();
    private final Map<Integer, Character> REVERSE_DICT = new HashMap<>();

    public Map<Character, Integer> getDICT() {
        return this.DICT;
    }
    public Map<Integer, Character> getREVERSE_DICT() {
        return this.REVERSE_DICT;
    }


    public Alphabet() {
        createDictionary();
    }

    private int fillDict(int l, int r, int cur) {
        for (int j = l; j < r; j++) {
            DICT.put((char) j, cur);
            REVERSE_DICT.put(cur, (char) j);
            cur++;
        }
        return cur;
    }

    private void createDictionary() {
        int i = 100;
        i = fillDict(0, 128, i);
        i = fillDict(1040, 1104, i);
        DICT.put('Ё', i);
         REVERSE_DICT.put(i++, 'Ё');
        DICT.put('ё', i);
        REVERSE_DICT.put(i, 'ё');
    }
}
