package org.geometerplus.zlibrary.core.util;

public class ZLArabicUtils {
	private ZLArabicUtils() {}
	
	static final int ArabicChar = 1;
	static final int LatinChar = 2;
	static final int NumberChar = 2;

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
    
    public static boolean isLatin(char c) {
    	return (0x41 <= c &&  c <= 0x5A) || (0x61 <= c &&  c <= 0x7A);
    }
    
    public static boolean isNumber(char c) {
    	return (0x30 <= c &&  c <= 0x39);
    }
    
    public static int markNum(char c) {
		int ret = 0;
		switch (c) {
		case '(':
		case '{':
		case '[':
		case '«':
			ret = 1;
			break;

		case ')':
		case '}':
		case ']':
		case '»':
			ret = 1;
			break;

		default:
			break;
		}
		
		return ret;
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
    /*
     * 159).
     * .)159
     */
    public static int getCharType(char c) {
		if (isArabic(c))
			return ArabicChar;
		else if (isNumber(c))
			return NumberChar;
		else
			return LatinChar;
	}

	public static void reshape(char[] text, int offset, int len) {
		StringBuffer resheped = new StringBuffer();
		int numOffset = 0;
		int numLen = 0;

		int lastCharType = 0;
		int charType = 0;

		for (int i = len + offset - 1; i >= offset; i--) {
			charType = getCharType(text[i]);
			if(lastCharType == 0)
				lastCharType = charType;
			
			if ((isNumber(text[i]) || isLatin(text[i]) || isArabic(text[i])) && (charType == lastCharType)) {
				if (numOffset == 0)
					numOffset = i;

				numLen++;
				lastCharType = charType;

			} else {
				if (numLen != 0) {
					for (int j = numOffset - numLen + 1; j <= numOffset; j++) {
						resheped.append(text[j]);
					}
										
					numOffset = 0;
					numLen = 0;
					lastCharType = charType;
					++i;
					continue;
				}

				resheped.append(reverseForArabic(text[i]));
			}
			
			lastCharType = charType;
		}

		if (numLen != 0) {
			for (int j = numOffset - numLen + 1; j <= numOffset; j++) {
				resheped.append(text[j]);
			}
			
			numOffset = 0;
			numLen = 0;
		}

		System.arraycopy(resheped.toString().toCharArray(), 0, text, offset,
				len);
	}
}
