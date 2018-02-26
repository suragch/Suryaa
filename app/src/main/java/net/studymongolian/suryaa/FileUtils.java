package net.studymongolian.suryaa;


import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.text.TextUtils;

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

    static boolean exportList(
            Context context,
            List<Vocab> items,
            String listName,
            File sourceAudioFolder) throws Exception {

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
            String row = convertToCvsRow(item);
            content.append(row);
            content.append("\n");
            copyAudioFileOver(context, item, sourceAudioFolder, destFolder);
        }
        String csvFileName = underscoredListName + EXPORT_IMPORT_FILE_EXTENSION;
        copyCsvFileOver(context, destFolder, csvFileName, content.toString());
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

    private static void copyAudioFileOver(Context context, Vocab item, File sourceAudioFolder, File destFolder) throws IOException {
        String audio = item.getAudioFileName();
        if (TextUtils.isEmpty(audio)) return;
        File source = new File(sourceAudioFolder, audio);
        File dest = new File(destFolder, audio);
        copyFile(source, dest);
        scanFile(context, dest);
    }

    private static String convertToCvsRow(Vocab item) {
        String mongol = item.getMongol().replace("\"", "\"\"");
        String definition = item.getDefinition().replace("\"", "\"\"");
        String pronunciation = item.getPronunciation().replace("\"", "\"\"");
        String audio = item.getAudioFileName();
        String content = "\"" + mongol + "\"," +
                "\"" + definition + "\"," +
                "\"" + pronunciation + "\"," +
                "\"" + audio + "\"";
        return content;
    }

    private static void scanFile(Context context, File file) {
        // this registers the file so that file explorers can find it more quickly
        MediaScannerConnection
                .scanFile(context, new String[] {file.getAbsolutePath()},
                        null, null);
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

    static void copyFile(File sourceFile, File destFile) throws IOException {
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
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

//    static void copyFile(File sourceFilePathName, File destFileFilePathName) {
//        Log.i(TAG, "copyFile: starting to copy " + sourceFilePathName + " to " + destFileFilePathName);
//        InputStream in;
//        OutputStream out;
//        try {
//
//            //create output directory if it doesn't exist
//            if (!destFileFilePathName.exists()) {
//                destFileFilePathName.getParentFile().mkdirs();
//            }
//
//
//            in = new FileInputStream(sourceFilePathName);
//            out = new FileOutputStream(destFileFilePathName);
//
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = in.read(buffer)) != -1) {
//                out.write(buffer, 0, read);
//            }
//            in.close();
//            in = null;
//
//            // write the output file (You have now copied the file)
//            out.flush();
//            out.close();
//            out = null;
//            Log.i(TAG, "copyFile: copy seemed to finish OK");
//
//        } catch (FileNotFoundException fnfe1) {
//            Log.i(TAG, "copyFile: FileNotFoundException");
//            Log.e("tag", fnfe1.getMessage());
//        } catch (Exception e) {
//            Log.i(TAG, "copyFile: FileNotFoundException");
//            Log.e("tag", e.getMessage());
//        }
//    }

}
