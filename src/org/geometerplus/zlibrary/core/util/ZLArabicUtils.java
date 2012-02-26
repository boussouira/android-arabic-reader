package org.geometerplus.zlibrary.core.util;

public class ZLArabicUtils {
	private ZLArabicUtils() {}
	

    public static boolean isAlef(char c) {
        return (c == 0x0622
                || c == 0x0623
                || c == 0x0625
                || c == 0x0627);
    }

    public static boolean isHah(char c) {
    	return (c == 0x0629
    			|| c == 0x0647);
    }

    public static boolean isYeh(char c) {
    	return (c == 0x0649
    			|| c == 0x0649);
    }

    public static boolean isTashekil(char c) {
        return (0x064B <= c && c <= 0x0653);
    }

    public static boolean isTatweel(char c) {
    	return (c == 0x0640);
    }
    
    public static boolean isArabic(char c) {
    	return (0x0621 <= c &&  c <= 0x06ED);
	}

    public static char reverseForArabic(char c) {
		char ret = c;
		switch (c) {
		case '(':
			ret = ')';
			break;

		case ')':
			ret = '(';
			break;

		case '{':
			ret = '}';
			break;
			
		case '}':
			ret = '{';
			break;
			
		case '[':
			ret = ']';
			break;
			
		case ']':
			ret = '[';
			break;
		
		case '»':
			ret = '«';
			break;
			
		case '«':
			ret = '»';
			break;
			
		default:
			break;
		}
		
		return ret;
	}
}
