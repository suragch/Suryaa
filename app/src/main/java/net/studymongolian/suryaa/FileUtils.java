package net.studymongolian.suryaa;


import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


// adapted from https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/


class FileUtils {

    private static final String APP_PUBLIC_FOLDER_NAME = "Suryaa";
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    static final String TEMP_AUDIO_FILE_NAME = "temp_recording.3gp";
    static final String AUDIO_FILE_EXTENSION = ".3gp";
    static final String EXPORT_IMPORT_FILE_EXTENSION = ".csv";
    private static final String TAG = "LOGTAG";

    static String getAudioPathName(Context context, long listId, String fileName) {
        File externalDir = context.getExternalFilesDir(null);
        if (externalDir == null) return null;
        return externalDir.getAbsolutePath() + File.separator + listId + File.separator + fileName;
    }

    static boolean exportList(
            Context context,
            List<Vocab> items,
            String listName,
            File sourceAudioFolder) {

        String underscoredListName = listName.replace(' ', '_');

        // make sure the directory exists
        File destFolder = new File(getAppPublicStorageDirectory(), underscoredListName);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
            scanFile(context, destFolder);
        }

        // build the csv text and copy audio files
        StringBuilder content = new StringBuilder();
        for (Vocab item : items) {

            boolean successfullyCopied = copyAudioFileOver(context, item, sourceAudioFolder, destFolder);
            if (!successfullyCopied) {
                if (!TextUtils.isEmpty(item.getAudioFilename()))
                    Log.e(TAG, "exportList: copy Audio File failed for "
                            + item.getMongol() + ", "
                            + item.getAudioFilename());
                item.setAudioFilename("");
            }

            String row = convertToCvsRow(item);
            content.append(row);
            content.append("\n");
        }

        String csvFileName = underscoredListName + EXPORT_IMPORT_FILE_EXTENSION;
        try {
            copyCsvFileOver(context, destFolder, csvFileName, content.toString());
        } catch (IOException e) {
            Log.e(TAG, "exportList: copyCsvFileOver failed");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void copyCsvFileOver(Context context, File destFolder, String fileName, String content) throws IOException {
        File csvFile = new File(destFolder, fileName);
        FileOutputStream fileOutput = new FileOutputStream(csvFile);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutput);
        outputStreamWriter.write(content);
        outputStreamWriter.flush();
        fileOutput.getFD().sync();
        outputStreamWriter.close();
        scanFile(context, csvFile);
    }

    /**
     * @return whether file was copied successfully
     */
    private static boolean copyAudioFileOver(Context context, Vocab item, File sourceAudioFolder, File destFolder) {
        String audio = item.getAudioFilename();
        if (TextUtils.isEmpty(audio)) return false;
        File source = new File(sourceAudioFolder, audio);
        File dest = new File(destFolder, audio);
        try {
            copyFile(source, dest);
            scanFile(context, dest);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String convertToCvsRow(Vocab item) {
        String mongol = item.getMongol().replace("\"", "\"\"");
        String definition = item.getDefinition().replace("\"", "\"\"");
        String pronunciation = item.getPronunciation().replace("\"", "\"\"");
        String audio = item.getAudioFilename();
        String example = item.getExampleSentence();
        String content = "\"" + mongol + "\"," +
                "\"" + definition + "\"," +
                "\"" + pronunciation + "\"," +
                "\"" + audio + "\"," +
                "\"" + example + "\"";
        return content;
    }

    private static void scanFile(Context context, File file) {
        // this registers the file so that file explorers can find it more quickly
        MediaScannerConnection
                .scanFile(context, new String[]{file.getAbsolutePath()},
                        null, null);
    }

    private static String getAppPublicStorageDirectory() {
        return Environment.getExternalStorageDirectory() + File.separator
                + APP_PUBLIC_FOLDER_NAME;
    }

    static List<Vocab> importFile(String filePathName, long listId) throws Exception {

        List<Vocab> vocabs = new ArrayList<>();

        Scanner scanner = new Scanner(new File(filePathName));
        while (scanner.hasNext()) {
            List<String> line = parseLine(scanner.nextLine());
            // TODO do error checking on line
            int size = line.size();
            Vocab item = new Vocab(null);
            item.setListId(listId);
            item.setMongol(line.get(0));
            if (size > 1)
                item.setDefinition(line.get(1));
            if (size > 2)
                item.setPronunciation(line.get(2));
            if (size > 3)
                item.setAudioFilename(line.get(3));
            if (size > 4)
                item.setExampleSentence(line.get(4));
            vocabs.add(item);
        }
        scanner.close();
        return vocabs;
    }

    static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    private static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    private static List<String> parseLine(String cvsLine, char separators, char customQuote) {

        List<String> result = new ArrayList<>();

        if (cvsLine == null || cvsLine.isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    continue;
                } else if (ch == '\n') {
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }

    /**
     * @return whether file was successfully copied
     */
    static boolean copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (IOException e) {
            return false;
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }

        return true;
    }

    public static void moveFile(File oldFile, File newFile) {
        try {
            copyFile(oldFile, newFile);
            oldFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void moveAudioFile(Context context,
                                     String audioFileName,
                                     long oldListId,
                                     long newListId) {

        String oldPath = FileUtils.getAudioPathName(context, oldListId, audioFileName);
        String newPath = FileUtils.getAudioPathName(context, newListId, audioFileName);
        if (oldPath == null || newPath == null) return;
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        FileUtils.moveFile(oldFile, newFile);
    }

}
