package SuperiorPro.SuperiorPOS.Excel.config;

import java.io.File;

public class ExcelPathResolver {

    // Default to Docker path, but allow override via environment variable
    private static final String BASE_DIR =
        System.getenv().getOrDefault("EXCEL_BASE_DIR",
//            "C:/Users/Superior1/OneDrive/Superior Shop/SuperiorPOS-Files/ExcelExportImport-M2SWeb");
        		"C:/Users/Moon/OneDrive/Superior Shop/SuperiorPOS-Files/ExcelExportImport-M2SWeb");
        		
    public static String resolveFixedPath(String type) {
        String folder = BASE_DIR + "/" + type;   // ✅ use forward slashes
        ensureFolderExists(folder);
        return folder + "/" + type + ".xlsx";    // ✅ use forward slashes
    }

    private static void ensureFolderExists(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}
