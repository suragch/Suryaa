package net.studymongolian.suryaa;


import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// adapted from https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/


public class FileUtils {

    private static final String APP_PUBLIC_FOLDER_NAME = "Suryaa";
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    static final String TEMP_AUDIO_FILE_NAME = "temp_recording.3gp";
    static final String AUDIO_FILE_EXTENSION = ".3gp";
    static final String EXPORT_IMPORT_FILE_EXTENSION = ".csv";

    public static boolean exportList(
            Context context,
            List<Vocab> items,
            String listName,
            File sourceAudioFolder) throws Exception {

        if (!isExternalStorageWritable()) {
            throw new IOException("External storage is not available");
        }

        String underscoredListName = listName.replace(' ', '_');

        // make sure the directory exists
        File destFolder = new File(getAppPublicStorageDirectory(), underscoredListName);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        // build the csv text and copy audio files
        StringBuilder content = new StringBuilder();
        for (Vocab item : items) {
            String mongol = item.getMongol().replace("\"", "\"\"");
            content.append("\"").append(mongol).append("\",");
            String definition = item.getDefinition().replace("\"", "\"\"");
            content.append("\"").append(definition).append("\",");
            String pronunciation = item.getPronunciation().replace("\"", "\"\"");
            content.append("\"").append(pronunciation).append("\",");
            String audio = item.getAudioFileName();
            if (!TextUtils.isEmpty(audio)) {
                File source = new File(sourceAudioFolder, audio);
                if (source.exists()) {
                    content.append("\"").append(audio).append("\"");
                    copyFile(source, new File(destFolder, audio));
                }
            }
            content.append("\n");
        }

        File csvFile = new File(destFolder, underscoredListName + EXPORT_IMPORT_FILE_EXTENSION);
        FileOutputStream fileOutput = new FileOutputStream(csvFile);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutput);
        outputStreamWriter.write(content.toString());
        outputStreamWriter.flush();
        fileOutput.getFD().sync();
        outputStreamWriter.close();

        // notify file managers of the change
        MediaScannerConnection.scanFile(
                context,
                new String[]{csvFile.getAbsolutePath()},
                null,
                null);

        return true;
    }

    private static String getAppPublicStorageDirectory() {
        return Environment.getExternalStorageDirectory() + File.separator
                + APP_PUBLIC_FOLDER_NAME;
    }

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    static List<Vocab> importFile(String filePathName, long listId) throws Exception {

        List<Vocab> vocabs = new ArrayList<>();

        Scanner scanner = new Scanner(new File(filePathName));
        while (scanner.hasNext()) {
            List<String> line = parseLine(scanner.nextLine());
            // TODO do error checking on line
            Vocab item = new Vocab();
            item.setList(listId);
            item.setMongol(line.get(0));
            item.setDefinition(line.get(1));
            item.setPronunciation(line.get(2));
            item.setAudioFileName(line.get(3));
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

    static void copyFile(File sourceFilePathName, File destFileFilePathName) {
        InputStream in;
        OutputStream out;
        try {

            //create output directory if it doesn't exist
            if (!destFileFilePathName.exists()) {
                destFileFilePathName.getParentFile().mkdirs();
            }


            in = new FileInputStream(sourceFilePathName);
            out = new FileOutputStream(destFileFilePathName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

}
