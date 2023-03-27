import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        String sourcePath = "C:\\Users\\rahul\\Downloads\\takeout-20230310T071059Z-001";
        String destinationPath = "D:\\MovedFiles";

        File sourceFolder = new File(sourcePath);
        File[] files = sourceFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
                    String dateStr = json.getJSONObject("photoTakenTime").getString("formatted");
                    Date date = parseDate(dateStr);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH) + 1;
                    Path yearPath = new File(destinationPath, String.valueOf(year)).toPath();
                    Path monthPath = new File(yearPath.toFile(), String.format("%02d", month)).toPath();
                    Files.createDirectories(monthPath);
                    Files.move(file.toPath(), monthPath.resolve(file.getName()));

                    // Move matching media files
                    String fileName = file.getName().substring(0, file.getName().lastIndexOf(".json"));
                    File[] mediaFiles = sourceFolder.listFiles((dir, name) -> {
                        return name.startsWith(fileName) && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".mp4"));
                    });
                    moveMatchingMediaFiles(mediaFiles, monthPath);
                }
            }
        }
    }

    private static void moveMatchingMediaFiles(File[] mediaFiles, Path destination) throws IOException {
        if (mediaFiles != null) {
            for (File mediaFile : mediaFiles) {
                Files.move(mediaFile.toPath(), destination.resolve(mediaFile.getName()));
            }
        }
    }

    private static Date parseDate(String dateStr) throws ParseException {
        DateFormat format = new SimpleDateFormat("MMM dd, yyyy, hh:mm:ss a z");
        return format.parse(dateStr.replace(" ", " "));
    }
}
