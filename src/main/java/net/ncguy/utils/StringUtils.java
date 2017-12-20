package net.ncguy.utils;

public class StringUtils {

    public static String ToTitleCase(String str) {
        StringBuilder sb = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : str.toCharArray()) {
            if(Character.isSpaceChar(c))
                nextTitleCase = true;
            else if(nextTitleCase) {
                c = Character.toUpperCase(c);
                nextTitleCase = false;
            }else if(!nextTitleCase) {
                c = Character.toLowerCase(c);
            }

            sb.append(c);
        }

        return sb.toString();
    }

}
