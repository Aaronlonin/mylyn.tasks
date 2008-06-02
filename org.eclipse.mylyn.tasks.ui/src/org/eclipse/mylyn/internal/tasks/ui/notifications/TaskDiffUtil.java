/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.ui.notifications;

import java.util.List;

import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Steffen Pingel
 */
public class TaskDiffUtil {

	private static final String ELLIPSIS = "..."; //$NON-NLS-1$ // could use the ellipsis glyph on some platforms "\u2026"

	private static int DRAW_FLAGS = SWT.DRAW_MNEMONIC | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER;

	public static String commentToString(ITaskComment comment) {
		StringBuilder sb = new StringBuilder();
		sb.append("Comment by ");
		sb.append(personToString(comment.getAuthor()));
		sb.append(": ");
		sb.append(cleanCommentText(comment.getText()));
		return sb.toString();
	}

	private static String personToString(IRepositoryPerson author) {
		if (author == null) {
			return "Unknown";
		} else if (author.getName() != null) {
			return author.getName();
		}
		return author.getPersonId();
	}

	public static String cleanCommentText(String value) {
		String text = "";
		String[] lines = value.split("\n");
		boolean attachment = false;
		boolean needSeparator = false;
		for (String line : lines) {
			// skip comments and info lines
			if (attachment) {
				text += " attachment: " + line;
				needSeparator = true;
				attachment = false;
			} else if (line.startsWith(">") //
					|| line.matches("^\\s*\\(In reply to comment.*")) {
				needSeparator = true;
				continue;
			} else if (line.startsWith("Created an attachment (id=")) {
				attachment = true;
			} else {
				if (needSeparator) {
					if (!text.matches(".*\\p{Punct}\\s*")) {
						text = text.trim();
						if (text.length() > 0) {
							text += ".";
						}
					}
				}
				text += " " + line;
				attachment = false;
				needSeparator = false;
			}
		}
		return foldSpaces(text);

	}

	public static String listToString(List<String> values) {
		if (values == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String value : values) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(foldSpaces(value));
		}
		return sb.toString();
	}

	public static String foldSpaces(String value) {
		return value.replaceAll("\\s", " ").trim();
	}

	public static String trim(String value, int length) {
		if (value.length() > length) {
			value = value.substring(0, length - 3) + "...";
		}
		return value;
	}

	/**
	 * Note: Copied from CLabel.
	 * 
	 * Shorten the given text <code>t</code> so that its length doesn't exceed the given width. The default
	 * implementation replaces characters in the center of the original string with an ellipsis ("..."). Override if you
	 * need a different strategy.
	 * 
	 * @param gc
	 *            the gc to use for text measurement
	 * @param t
	 *            the text to shorten
	 * @param width
	 *            the width to shorten the text to, in pixels
	 * @return the shortened text
	 */
	public static final String shortenText2(Composite composite, String t, int width) {
		GC gc = new GC(composite);

		if (t == null) {
			return null;
		}
		int w = gc.textExtent(ELLIPSIS, DRAW_FLAGS).x;
		if (width <= w) {
			return t;
		}
		int l = t.length();
		int max = l / 2;
		int min = 0;
		int mid = (max + min) / 2 - 1;
		if (mid <= 0) {
			return t;
		}
		while (min < mid && mid < max) {
			String s1 = t.substring(0, mid);
			String s2 = t.substring(l - mid, l);
			int l1 = gc.textExtent(s1, DRAW_FLAGS).x;
			int l2 = gc.textExtent(s2, DRAW_FLAGS).x;
			if (l1 + w + l2 > width) {
				max = mid;
				mid = (max + min) / 2;
			} else if (l1 + w + l2 < width) {
				min = mid;
				mid = (max + min) / 2;
			} else {
				min = max;
			}
		}

		gc.dispose();

		if (mid == 0) {
			return t;
		}
		return t.substring(0, mid) + ELLIPSIS + t.substring(l - mid, l);
	}

	// From PerspectiveBarContributionItem
	public static String shortenText(Drawable composite, String text, int maxWidth) {
		String returnText = text;
		GC gc = new GC(composite);
		if (gc.textExtent(text).x > maxWidth) {
			for (int i = text.length(); i > 0; i--) {
				String subString = text.substring(0, i);
				subString = subString + "...";
				if (gc.textExtent(subString).x < maxWidth) {
					returnText = subString;
					break;
				}
			}
		}
		gc.dispose();
		return returnText;
	}

}
