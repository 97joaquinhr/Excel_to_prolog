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
    private static final String SAMPLE_XLSX_FILE_PATH = "C:\\Users\\joaqu\\Documents\\Codigo\\Excel_to_prolog\\data\\Proyecto.xlsx";
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
    //CRNs are the id for each group
    private static String[] CRNs = new String[sheet.getLastRowNum()];

    public static void main(String[] args) throws IOException, InvalidFormatException {
        int i,j;
        int[] rows_to_read = new int[]{7,19,20,21,24,29};
        int n_rows_to_read =rows_to_read.length;
        String[][] data = new String[n_rows_to_read][sheet.getLastRowNum()];
        String[] relations = new String[n_rows_to_read];
        String id_name="crn";

        create_relations(rows_to_read,relations,n_rows_to_read,id_name);
        add_headers(n_rows_to_read, relations);
        read_data(rows_to_read, n_rows_to_read, data);

        //process data
        Process p = new Process(0,sheet.getLastRowNum(),n_rows_to_read,data,relations,CRNs);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(p);
        pool.shutdown();

        write_data(n_rows_to_read, data);

    }

    private static void write_data(int n_rows_to_read, String[][] data) throws IOException {
        int i;
        int j;
        for (i = 0; i < n_rows_to_read; ++i){
            for(j=0;j<sheet.getLastRowNum();++j) {
                lines.add(data[i][j]);
            }
        }
        Path file = Paths.get("C:\\Users\\joaqu\\Documents\\Codigo\\Excel_to_prolog\\data\\data.pl");
        Files.write(file, lines, StandardCharsets.UTF_8);
        workbook.close();
    }

    private static void add_headers(int n_rows_to_read, String[] relations) {
        int i=0,j;
        lines.add(":- encoding(utf8).");
        for(i=0; i<n_rows_to_read;++i){
            lines.add(":- discontiguous "+relations[i]+"/2.");
        }
    }

    private static void read_data(int[] rows_to_read, int n_rows_to_read, String[][] data) {
        int i=0,j;
        int CRN_row = 5; //for this spreadsheet
        for(Row row: sheet){
            if(i>=1){
                CRNs[i-1] = dataFormatter.formatCellValue(row.getCell(CRN_row));
                for(j=0;j<n_rows_to_read;++j) {
                    data[j][i-1] = dataFormatter.formatCellValue(row.getCell(rows_to_read[j]));
                }
            }
            i++;
        }
    }

    private static String camelCase(String in){
        return WordUtils.capitalizeFully(in, ' ').replaceAll(" ", "");
    }

    private static void create_relations(int[] rows_to_read,String[] relations,int l,String id){
        int i;
        String aux="";
        for(i=0;i<l;++i){
            aux = dataFormatter.formatCellValue(sheet.getRow(0).getCell(rows_to_read[i]));
            relations[i]= id+"_"+camelCase(aux);
        }
    }


}