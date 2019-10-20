public class Utils {

    public static String normalizeToken(String token) {
        return token.toLowerCase()
                .replaceAll("[-|.|,|?|'|\"|(|)|;|:|!]", "");
    }

    static double log2(double d) {
        return (Math.log(d) / Math.log(2) + 1e-10);
    }

    static double round2dp(double number) {
        return Math.round(number * 100.0) / 100.0;
    }
}
