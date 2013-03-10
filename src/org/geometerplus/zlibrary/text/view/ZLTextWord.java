/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.text.view;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.zlibrary.core.util.ZLArabicUtils;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;

public final class ZLTextWord extends ZLTextElement { 
	public final char[] Data;
	public final int Offset;
	public final int Length;
	private int myWidth = -1;
	private Mark myMark;
	private int myParagraphOffset;

	class Mark {
		public final int Start;
		public final int Length;
		private Mark myNext;

		private Mark(int start, int length) {
			Start = start;
			Length = length;
			myNext = null;
		}

		public Mark getNext() {
			return myNext;
		}

		private void setNext(Mark mark) {
			myNext = mark;
		}
	}
	
	public ZLTextWord(char[] data, int offset, int length, int paragraphOffset) {
		Data = data;
		Offset = offset;
		Length = length;
		myParagraphOffset = paragraphOffset;
		
		int arabicCount = 0;
		int numberCount = 0;
		boolean haveMark = false;
		
		for(int i=offset; i<offset+length; i++) {
			if(ZLArabicUtils.isArabic(Data[i])) {
				++arabicCount;
			} else if(ZLArabicUtils.isNumber(Data[i])) {
				++numberCount;
			}
			
			if(!haveMark)
				haveMark = (ZLArabicUtils.markNum(Data[i]) != 0);
		}
		
		boolean wordIsArabic = (arabicCount == length); // The word contains only Arabic chars
		if(!wordIsArabic && numberCount > 0 && (haveMark || arabicCount < 0)) {
			ZLArabicUtils.reshape(Data, offset, length);
		} else {
			for(int i=offset; i<offset+length; i++) {
				Data[i] = ZLArabicUtils.reverseForArabic(Data[i]);
			}
		}
	}

	public boolean isASpace() {
		for (int i = Offset; i < Offset + Length; ++i) {
			if (!Character.isWhitespace(Data[i])) {
				return false;
			}
		}
		return true;
	}

	public Mark getMark() {
		return myMark;
	}

	public int getParagraphOffset() {
		return myParagraphOffset;
	}
	
	public void addMark(int start, int length) {
		/*
		int i = 0;
		for(i=start; i>= 0; i--) {
			if(!ZLArabicUtils.isArabic(Data[Offset + i])) {
				break;
			}
		}

		length += start - Math.max(0, i);
		start = Math.max(0, i);

		int j = Length;
		for(j=length; j<Data.length; j++) {
			if(!ZLArabicUtils.isArabic(Data[Offset + j])) {
				break;
			}
		}

		length = Math.min(j, length);
		*/

		// Mark the hole word
		start = 0;
		length = Data.length;

		Mark existingMark = myMark;
		Mark mark = new Mark(start, length);
		if ((existingMark == null) || (existingMark.Start > start)) {
			mark.setNext(existingMark);
			myMark = mark;
		} else {
			while ((existingMark.getNext() != null) && (existingMark.getNext().Start < start)) {
				existingMark = existingMark.getNext();
			}
			mark.setNext(existingMark.getNext());
			existingMark.setNext(mark);
		}		
	}
	
	public int getWidth(ZLPaintContext context) {
		int width = myWidth;
		if (width <= 1) {
			width = context.getStringWidth(Data, Offset, Length);	
			myWidth = width;
		}
		return width;
	}

	@Override
	public String toString() {
		return new String(Data, Offset, Length);
	}
}
