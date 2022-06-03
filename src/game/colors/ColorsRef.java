package game.colors;

public enum ColorsRef {

    RESET(0, "\033[0m"),

    RED(1, "\033[0;31m"), //0
    GREEN(2, "\033[0;32m"), //1
    YELLOW(3, "\033[0;33m"),//2
    BLUE(4, "\033[0;34m"),//3
    MAGENTA(5, "\033[0;35m"),//4
    CYAN(6, "\033[0;36m"),//5
    WHITE(7, "\033[0;37m"),//6

    RED_UNDERLINED(8, "\033[4;31m"), //7
    GREEN_UNDERLINED(9, "\033[4;32m"),//8
    YELLOW_UNDERLINED(10, "\033[4;33m"),//9
    BLUE_UNDERLINED(11, "\033[4;34m"),//10
    MAGENTA_UNDERLINED(12, "\033[4;35m");//11

    private final int REF;
    private final String code;

    ColorsRef(int REF, String code) {
        this.REF = REF;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
