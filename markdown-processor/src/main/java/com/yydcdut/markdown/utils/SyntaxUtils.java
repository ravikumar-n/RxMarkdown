/*
 * Copyright (C) 2018 yydcdut (yuyidong2015@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yydcdut.markdown.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.view.View;

import com.yydcdut.markdown.callback.OnTodoClickListener;
import com.yydcdut.markdown.live.EditToken;
import com.yydcdut.markdown.span.MDCodeBlockSpan;
import com.yydcdut.markdown.span.MDImageSpan;
import com.yydcdut.markdown.syntax.SyntaxKey;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yuyidong on 2018/4/29.
 */
public class SyntaxUtils {

    /**
     * parse bold and italic
     *
     * @param key      {@link SyntaxKey#KEY_BOLD_ASTERISK} or {@link SyntaxKey#KEY_BOLD_UNDERLINE} or
     *                 {@link SyntaxKey#KEY_ITALIC_ASTERISK} or {@link SyntaxKey#KEY_ITALIC_UNDERLINE}
     * @param ssb      the original content
     * @param callback span callback
     * @return the content after parsing
     */
    public static SpannableStringBuilder parseBoldAndItalic(@NonNull String key, @NonNull SpannableStringBuilder ssb, @NonNull OnWhatSpanCallback callback) {
        if (callback == null) {
            return ssb;
        }
        String text = ssb.toString();
        int keyLength = key.length();
        SpannableStringBuilder tmp = new SpannableStringBuilder();
        String tmpTotal = text;
        while (true) {
            int positionHeader = SyntaxUtils.findPosition(key, tmpTotal, ssb, tmp);
            if (positionHeader == -1) {
                tmp.append(tmpTotal);
                break;
            }
            tmp.append(tmpTotal.substring(0, positionHeader));
            int index = tmp.length();
            tmpTotal = tmpTotal.substring(positionHeader + keyLength, tmpTotal.length());
            int positionFooter = SyntaxUtils.findPosition(key, tmpTotal, ssb, tmp);
            if (positionFooter != -1) {
                ssb.delete(tmp.length(), tmp.length() + keyLength);
                tmp.append(tmpTotal.substring(0, positionFooter));
                ssb.setSpan(callback.whatSpan(), index, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.delete(tmp.length(), tmp.length() + keyLength);
            } else {
                tmp.append(key);
                tmp.append(tmpTotal);
                break;
            }
            tmpTotal = tmpTotal.substring(positionFooter + keyLength, tmpTotal.length());
        }
        return ssb;
    }

    /**
     * find the position of next key
     * ignore the key and key in (inline) code syntax,
     *
     * @param tmpTotal the original content, the class type is {@link String}
     * @param ssb      the original content, the class type is {@link SpannableStringBuilder}
     * @param tmp      the content that has parsed
     * @return the next position of key
     */
    private static int findPosition(@NonNull String key, @NonNull String tmpTotal, @NonNull SpannableStringBuilder ssb, @NonNull SpannableStringBuilder tmp) {
        String tmpTmpTotal = tmpTotal;
        int position = tmpTmpTotal.indexOf(key);
        if (position == -1) {
            return -1;
        } else {
            if (existCodeSyntax(ssb, tmp.length() + position, key.length())) {//key是否在code中
                StringBuilder sb = new StringBuilder(tmpTmpTotal.substring(0, position))
                        .append("$$").append(tmpTmpTotal.substring(position + key.length(), tmpTmpTotal.length()));
                return findPosition(key, sb.toString(), ssb, tmp);
            } else {
                return position;
            }
        }
    }

    /**
     * check whether contains (inline) code syntax
     *
     * @param ssb       the content
     * @param position  start position
     * @param keyLength the checking words' length
     * @return TRUE: contains
     */
    public static boolean existCodeSyntax(SpannableStringBuilder ssb, int position, int keyLength) {
        TypefaceSpan[] spans = ssb.getSpans(position, position + keyLength, TypefaceSpan.class);
        return spans.length != 0;
    }

    /**
     * check whether contains hyper link syntax
     *
     * @param ssb       the content
     * @param position  start position
     * @param keyLength the checking words' length
     * @return TRUE: contains
     */
    public static boolean existHyperLinkSyntax(SpannableStringBuilder ssb, int position, int keyLength) {
        URLSpan[] spans = ssb.getSpans(position, position + keyLength, URLSpan.class);
        return spans.length != 0;
    }

    /**
     * check whether contains image syntax
     *
     * @param ssb       the content
     * @param position  start position
     * @param keyLength the checking words' length
     * @return TRUE: contains
     */
    public static boolean existImageSyntax(SpannableStringBuilder ssb, int position, int keyLength) {
        MDImageSpan[] spans = ssb.getSpans(position, position + keyLength, MDImageSpan.class);
        return spans.length != 0;
    }

    /**
     * check whether exists code block span
     *
     * @param ssb   the text
     * @param start the start position
     * @param end   the end position
     * @return if exists, return true
     */
    public static boolean existCodeBlockSpan(@NonNull SpannableStringBuilder ssb, int start, int end) {
        MDCodeBlockSpan[] mdCodeBlockSpans = ssb.getSpans(start, end, MDCodeBlockSpan.class);
        return mdCodeBlockSpans != null && mdCodeBlockSpans.length > 0;
    }

    /**
     * parse the editable(spannable string) by pattern string
     *
     * @param editable   the spannable string
     * @param pattern    the pattern string
     * @param ignoreText the replace string
     * @param callback   the callback to get span
     * @return
     */
    public static List<EditToken> parse(@NonNull Editable editable, @NonNull String pattern, String ignoreText, OnWhatSpanCallback callback) {
        StringBuilder content = new StringBuilder(editable.toString().replace(ignoreText, TextHelper.getPlaceHolder(ignoreText)));
        return parse(content, pattern, callback);
    }

    /**
     * parse the editable(spannable string) by pattern string
     *
     * @param editable the spannable string
     * @param pattern  the pattern string
     * @param callback the callback to get span
     * @return the list of edit token
     */
    @NonNull
    public static List<EditToken> parse(@NonNull Editable editable, @NonNull String pattern, OnWhatSpanCallback callback) {
        StringBuilder content = new StringBuilder(editable);
        return parse(content, pattern, callback);
    }

    /**
     * parse the content by pattern string
     *
     * @param content  the content
     * @param pattern  the pattern string
     * @param callback the callback to get span
     * @return the list of edit token
     */
    public static List<EditToken> parse(@NonNull StringBuilder content, @NonNull String pattern, OnWhatSpanCallback callback) {
        List<EditToken> editTokenList = new ArrayList<>();
        Matcher m = Pattern.compile(pattern, Pattern.MULTILINE).matcher(content);
        List<String> matchList = new ArrayList<>();//找到的
        while (m.find()) {
            matchList.add(m.group());
        }
        for (String match : matchList) {
            int index = content.indexOf(match);
            int length = match.length();
            editTokenList.add(new EditToken(callback.whatSpan(), index, index + length));
            content.replace(index, index + length, TextHelper.getPlaceHolder(match));
        }
        return editTokenList;
    }

    /**
     * the interface of getting span object
     */
    public interface OnWhatSpanCallback {
        /**
         * get span
         *
         * @return the span
         */
        Object whatSpan();
    }

    /**
     * set content margin left.
     *
     * @param ssb   the content
     * @param every the distance that margin left
     */
    public static void marginSSBLeft(SpannableStringBuilder ssb, int every) {
        marginSSBLeft(ssb, every, 0, ssb.length());
    }

    /**
     * set content margin left.
     *
     * @param ssb   the content
     * @param every the distance that margin left
     * @param start the start position
     * @param end   the end position
     */
    public static void marginSSBLeft(SpannableStringBuilder ssb, int every, int start, int end) {
        ssb.setSpan(new LeadingMarginSpan.Standard(every), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * set _todo or done click callback
     *
     * @param endPosition         the end position of span
     * @param ssb                 the text
     * @param onTodoClickListener {@link OnTodoClickListener}
     */
    public static void setTodoOrDoneClick(int endPosition, final SpannableStringBuilder ssb, final OnTodoClickListener onTodoClickListener) {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
        ssb.setSpan(new ImageSpan(bitmap), 0, endPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (onTodoClickListener != null) {
                    onTodoClickListener.onTodoClicked(widget, ssb);
                }
            }
        }, 0, SyntaxKey.KEY_TODO_HYPHEN.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * remove spans
     *
     * @param editable Editable, the text
     * @param start    int, the selection position
     * @param clazz    class
     * @param <T>      span
     */
    public static <T> void removeSpans(Editable editable, int start, Class<T> clazz) {
        int startPosition = TextHelper.findBeforeNewLineChar(editable, start) + 1;
        int endPosition = TextHelper.findNextNewLineCharCompat(editable, start);
        T[] ts = editable.getSpans(startPosition, endPosition, clazz);
        if (clazz.isAssignableFrom(MDCodeBlockSpan.class)) {
            for (T t : ts) {
                MDCodeBlockSpan mdCodeBlockSpan = ((MDCodeBlockSpan) t);
                while (mdCodeBlockSpan != null) {
                    editable.removeSpan(mdCodeBlockSpan);
                    mdCodeBlockSpan = mdCodeBlockSpan.getNext();
                }
            }
        } else {
            for (T t : ts) {
                editable.removeSpan(t);
            }
        }
    }

    /**
     * set spans
     *
     * @param editable      Editable, the text
     * @param editTokenList List, the edit token collection
     */
    public static void setSpans(Editable editable, List<EditToken> editTokenList) {
        for (EditToken editToken : editTokenList) {
            editable.setSpan(editToken.getSpan(), editToken.getStart(), editToken.getEnd(), editToken.getFlag());
        }
    }

    /**
     * set spans for  code span
     *
     * @param editable      Editable, the text
     * @param editTokenList List, the edit token collection
     */
    public static void setCodeSpan(Editable editable, List<EditToken> editTokenList) {
        for (EditToken editToken : editTokenList) {
            Object[] spans = editable.getSpans(editToken.getStart(), editToken.getEnd(), Object.class);
            for (Object o : spans) {
                if (editToken.getStart() <= editable.getSpanStart(o) && editToken.getEnd() >= editable.getSpanEnd(o)) {
                    editable.removeSpan(o);
                }
            }
        }
        setSpans(editable, editTokenList);
    }

    /**
     * get matched edit token list
     *
     * @param editable Editable, the text
     * @param allList  List, the edit token collection
     * @param start    the selection position
     * @return the matched edit token list
     */
    public static List<EditToken> getMatchedEditTokenList(Editable editable, List<EditToken> allList, int start) {
        List<EditToken> matchEditTokenList = new ArrayList<>();
        int startPosition = TextHelper.findBeforeNewLineChar(editable, start) + 1;
        int endPosition = TextHelper.findNextNewLineCharCompat(editable, start);
        for (EditToken editToken : allList) {
            if (editToken.getStart() >= startPosition && editToken.getEnd() <= endPosition) {
                matchEditTokenList.add(editToken);
            }
        }
        return matchEditTokenList;
    }

}
