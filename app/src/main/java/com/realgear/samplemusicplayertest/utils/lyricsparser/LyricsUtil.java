package com.realgear.samplemusicplayertest.utils.lyricsparser;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class LyricsUtil {
    private static final String TAG = LyricsUtil.class.getSimpleName();

    public static Lyric parseLyric(InputStream inputStream, String Encoding) {
        Lyric lyric = new Lyric();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, Encoding));
            String line;
            while ((line = br.readLine()) != null) {
                parseLine(line, lyric);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lyric;
    }

    private static boolean parseLine(String line, Lyric lyric) {
        int lineLength = line.length();
        line = line.trim();
        int openBracketIndex, closedBracketIndex;
        openBracketIndex = line.indexOf('[', 0);

        while (openBracketIndex != -1) {
            closedBracketIndex = line.indexOf(']', openBracketIndex);
            // (1) ']' does not exist, (2) is the first character
            if (closedBracketIndex < 1)
                return false;
            String closedTag = line.substring(openBracketIndex + 1, closedBracketIndex);
            String[] colonSplited = closedTag.split(":", 2);
            if (colonSplited.length < 2)
                return false;

            if (colonSplited[0].equalsIgnoreCase(Constants.ID_TAG_TITLE)) {
                lyric.mTitle = colonSplited[1].trim();
            } else if (colonSplited[0].equalsIgnoreCase(Constants.ID_TAG_ARTIST)) {
                lyric.mArtist = colonSplited[1].trim();
            } else if (colonSplited[0].equalsIgnoreCase(Constants.ID_TAG_ALBUM)) {
                lyric.mAlbum = colonSplited[1].trim();
            } else if (colonSplited[0].equalsIgnoreCase(Constants.ID_TAG_CREATOR_LRCFILE)) {
                lyric.mBy = colonSplited[1].trim();
            } else if (colonSplited[0].equalsIgnoreCase(Constants.ID_TAG_CREATOR_SONGTEXT)) {
                lyric.mAuthor = colonSplited[1].trim();
            } else if (colonSplited[0].equalsIgnoreCase(Constants.ID_TAG_LENGTH)) {
                lyric.mLength = parseTime(colonSplited[1].trim(), lyric);
            } else if (colonSplited[0].equalsIgnoreCase(Constants.ID_TAG_OFFSET)) {
                lyric.mOffset = parseOffset(colonSplited[1].trim());
            } else {
                if (Character.isDigit(colonSplited[0].charAt(0))) {
                    List<Long> timestampList = new LinkedList<Long>();
                    long time = parseTime(closedTag, lyric);
                    if (time != -1) {
                        timestampList.add(time);
                    }
                    //Log.d(TAG, line);
                    // We may have line like [01:38.33][01:44.01][03:22.05]Test Test
                    // [03:55.00]
                    while ((lineLength > closedBracketIndex + 2)
                            && (line.charAt(closedBracketIndex + 1) == '[')) {
                        //Log.d(TAG, String.valueOf(closedBracketIndex));
                        int nextOpenBracketIndex = closedBracketIndex + 1;
                        int nextClosedBracketIndex = line.indexOf(']', nextOpenBracketIndex + 1);
                        time = parseTime(line.substring(nextOpenBracketIndex + 1, nextClosedBracketIndex), lyric);
                        if (time != -1) {
                            timestampList.add(time);
                        }
                        closedBracketIndex = nextClosedBracketIndex;
                    }

                    String content = line.substring(closedBracketIndex + 1, line.length());
                    for (long timestamp : timestampList) {
                        lyric.addLine(timestamp, content);
                    }
                } else {
                    // Ignore unknown tag
                    return true;
                }
            }
            // We may have line like [00:53.60]On a dark [00:54.85]desert highway
            openBracketIndex = line.indexOf('[', closedBracketIndex + 1);
        }
        return true;
    }

    private static long parseTime(String time, Lyric lyric) {
        String[] ss = time.split("\\:|\\.");
        // 如果 是两位以后，就非法了
        if (ss.length < 2) {
            return -1;
        } else if (ss.length == 2) {// 如果正好两位，就算分秒
            try {
                // 先看有没有一个是记录了整体偏移量的
                if (lyric.mOffset == 0 && ss[0].equalsIgnoreCase("offset")) {
                    lyric.mOffset = Integer.parseInt(ss[1]);
                    System.err.println("整体的偏移量：" + lyric.mOffset);
                    return -1;
                }
                int min = Integer.parseInt(ss[0]);
                int sec = Integer.parseInt(ss[1]);
                if (min < 0 || sec < 0 || sec >= 60) {
                    throw new RuntimeException("数字不合法!");
                }
                // System.out.println("time" + (min * 60 + sec) * 1000L);
                return (min * 60 + sec) * 1000L;
            } catch (Exception exe) {
                return -1;
            }
        } else if (ss.length == 3) {// 如果正好三位，就算分秒，十毫秒
            try {
                int min = Integer.parseInt(ss[0]);
                int sec = Integer.parseInt(ss[1]);
                int mm = Integer.parseInt(ss[2]);
                if (min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 99) {
                    throw new RuntimeException("数字不合法!");
                }
                // System.out.println("time" + (min * 60 + sec) * 1000L + mm * 10);
                return (min * 60 + sec) * 1000L + mm * 10;
            } catch (Exception exe) {
                return -1;
            }
        } else {// 否则也非法
            return -1;
        }
    }

    private static int parseOffset(String str) {
        if (str.equalsIgnoreCase("0"))
            return 0;
        String[] ss = str.split("\\:");
        if (ss.length == 2) {
            if (ss[0].equalsIgnoreCase("offset")) {
                int os = Integer.parseInt(ss[1]);
                Log.i(TAG, "total offset：" + os);
                return os;
            } else {
                return Integer.MAX_VALUE;
            }
        } else {
            return Integer.MAX_VALUE;
        }
    }
}

