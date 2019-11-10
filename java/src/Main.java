import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.commons.lang3.text.WordUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

//https://www.callicoder.com/java-read-excel-file-apache-poi/
public class Main {
    private static int CRN_row = 5;
    private static String CRN = "crn";
    private static final String SAMPLE_XLSX_FILE_PATH = "C:\\Users\\joaqu\\Documents\\Codigo\\Excel_to_prolog\\data\\Proyecto.xlsx";
    // Creating a Workbook from an Excel file (.xls or .xlsx)
    private static Workbook workbook;
    private static List<String> lines= new ArrayList<String>();

    private static DataFormatter dataFormatter = new DataFormatter();
    static {
        try {
            workbook = WorkbookFactory.create(new File(SAMPLE_XLSX_FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static Sheet sheet = workbook.getSheetAt(0);
    private static String[] CRNs = new String[sheet.getLastRowNum()];

    private static String decapitalize(String string){
        if (string == null || string.length() == 0) {
            return string;
        }
        char[] c = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
    private static String camelCase(String in){
        return WordUtils.capitalizeFully(in, ' ').replaceAll(" ", "");
    }
    private static void create_relations(int[] rows_to_read,String[] relations,int l){
        int i;
        String aux="";
        for(i=0;i<l;++i){
            aux = dataFormatter.formatCellValue(sheet.getRow(0).getCell(rows_to_read[i]));
            relations[i]= CRN +"_"+camelCase(aux);
        }
    }

    //TODO: Recursive Action
//    private static void process(String[][] data,int n,int m,String[] relations){
//        int i,j;
//        for (i = 0; i < n; ++i){
//            for(j=0;j<m;++j){
//                if (data[i][j].contains(" ")||data[i][j].contains("+")||data[i][j].contains("@")) {
//                    data[i][j] = "\"" + data[i][j] + "\"";
//                } else {
//                    data[i][j] = data[i][j].toLowerCase();
//                }
//                data[i][j]=relations[i]+"("+CRNs[j]+","+data[i][j]+").";
//            }
//        }
//    }
    public static void main(String[] args) throws IOException, InvalidFormatException {
        int[] rows_to_read = new int[]{7,19,20};
        int n_rows_to_read =rows_to_read.length;
        String[] relations = new String[n_rows_to_read];
        String[][] data = new String[n_rows_to_read][sheet.getLastRowNum()];
        create_relations(rows_to_read,relations,n_rows_to_read);

        String relation="";
        int i=0,j;
        lines.add(":- encoding(utf8).");
        for(i=0; i<n_rows_to_read;++i){
            lines.add(":- discontiguous "+relations[i]+"/2.");
        }

        //read
        i=0;
        for(Row row: sheet){
            if(i>=1){
                CRNs[i-1] = dataFormatter.formatCellValue(row.getCell(CRN_row));
                for(j=0;j<n_rows_to_read;++j) {
                    data[j][i-1] = dataFormatter.formatCellValue(row.getCell(rows_to_read[j]));
                }
            }
            i++;
        }

        //process data
        Process p = new Process(0,sheet.getLastRowNum(),n_rows_to_read,data,relations,CRNs);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(p);
        pool.shutdown();
        //process(data,n_rows_to_read,sheet.getLastRowNum(),relations);

        //write
        for (i = 0; i < n_rows_to_read; ++i){
            for(j=0;j<sheet.getLastRowNum();++j) {
                lines.add(data[i][j]);
            }
        }
        Path file = Paths.get("C:\\Users\\joaqu\\Documents\\Codigo\\Excel_to_prolog\\data\\data.pl");
        Files.write(file, lines, StandardCharsets.UTF_8);

        //close
        workbook.close();
    }
}