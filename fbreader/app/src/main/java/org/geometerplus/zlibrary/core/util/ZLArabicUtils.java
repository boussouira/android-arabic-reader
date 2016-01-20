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
    
    public static double arabicCharCount(String text) {
    	double arabicCount = 0;
    	double otherCount = 0;
    	
    	for (int i = 0; i< text.length(); i++) {
    		char c = text.charAt(i);
    		if (isArabic(c)) {
    			++arabicCount;
    		} else if (isLatin(c)) {
    		} else {
    			++otherCount;
    		}
    	}
    	
    	return arabicCount > 0 ? arabicCount / (text.length() - otherCount) : 0;
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
			ret = -1;
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

	public static boolean compare(char firstChar, char secondChar) {
		return ((firstChar == secondChar)
				|| (isAlef(firstChar) && isAlef(secondChar))
				|| (isHah(firstChar) && isHah(secondChar))
				|| (isYeh(firstChar) && isYeh(secondChar)));
	}

    public static int getCharType(char c) {
		if (isArabic(c))
			return ArabicChar;
		else if (isNumber(c))
			return NumberChar;
		else
			return LatinChar;
	}

    public static void mayReshape(char[] text, int offset, int length) {
		int arabicCount = 0;
		int numberCount = 0;
		boolean haveMark = false;
		
		for(int i=offset; i<offset+length; i++) {
			if(ZLArabicUtils.isArabic(text[i])) {
				++arabicCount;
			} else if(ZLArabicUtils.isNumber(text[i])) {
				++numberCount;
			}
			
			if(!haveMark)
				haveMark = (ZLArabicUtils.markNum(text[i]) != 0);
		}
		
		boolean wordIsArabic = (arabicCount == length); // The word contains only Arabic chars
		if(!wordIsArabic && numberCount > 0 && (haveMark || arabicCount < 0)) {
			reshape(text, offset, length);
		} else {
			for(int i=offset; i<offset+length; i++) {
				text[i] = reverseForArabic(text[i]);
			}
		}
    }
    
	public static void reshape(char[] text, int offset, int len) {
		StringBuffer resheped = new StringBuffer();
		int numOffset = 0;
		int numLen = 0;

		int lastCharType = 0;
		int charType = 0;

		for (int i = len + offset - 1; i >= offset; i--) {
			char c = text[i];
			charType = getCharType(c);
			if(lastCharType == 0)
				lastCharType = charType;
			
			if (((isNumber(c) || isLatin(c) || isArabic(c)) && (charType == lastCharType))) {
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
					lastCharType = 0;
					++i;
					continue;
				}
				
				resheped.append(reverseForArabic(c));
				
				if(i-1 > 0) {
					if(!isArabic(c) && isArabic(text[i-1])) {
						lastCharType = ArabicChar;
						numOffset = i-1;
						continue;
					}
				}
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

		System.arraycopy(resheped.toString().toCharArray(), 0, text, offset, len);
	}
}
