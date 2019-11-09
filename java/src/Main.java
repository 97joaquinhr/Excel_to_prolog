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

//https://www.callicoder.com/java-read-excel-file-apache-poi/
public class Main {
    private static int CRN_row = 5;
    private static String CRN = "crn";
    private static final String SAMPLE_XLSX_FILE_PATH = "C:\\Users\\joaqu\\Documents\\Codigo\\Excel_to_prolog\\data\\Proyecto.xlsx";
    // Creating a Workbook from an Excel file (.xls or .xlsx)
    private static Workbook workbook;
    private static DataFormatter dataFormatter = new DataFormatter();
    static {
        try {
            workbook = WorkbookFactory.create(new File(SAMPLE_XLSX_FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static Sheet sheet = workbook.getSheetAt(0);

    private static String decapitalize(String string){
        if (string == null || string.length() == 0) {
            return string;
        }
        char c[] = string.toCharArray();
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
    public static void main(String[] args) throws IOException, InvalidFormatException {


        int[] rows_to_read = new int[]{7,19,20};
        int n_rows_to_read =rows_to_read.length;
        String[] relations = new String[n_rows_to_read];
        //String[] data = new String[n_rows_to_read];
        create_relations(rows_to_read,relations,n_rows_to_read);

        List<String> lines= new ArrayList<String>();
        String relation="";
        int i=0,j;
        String aux;
        lines.add(":- encoding(utf8).");
        for(i=0; i<n_rows_to_read;++i){
            lines.add(":- discontiguous "+relations[i]+"/2.");
        }
        i=0;
        for(Row row: sheet){
            if(i>1){
                String crn1 = dataFormatter.formatCellValue(row.getCell(CRN_row));
                for(j=0;j<n_rows_to_read;++j) {
                    aux = dataFormatter.formatCellValue(row.getCell(rows_to_read[j]));
                    if (aux.contains(" ")) {
                        aux = "\"" + aux + "\"";
                    } else {
                        aux = aux.toLowerCase();
                    }
                    lines.add(relations[j] + "("+crn1 + "," + aux + ").");
                }
            }
            i++;
        }
        workbook.close();
        /*writing to file*/
        Path file = Paths.get("C:\\Users\\joaqu\\Documents\\Codigo\\Excel_to_prolog\\data\\data.pl");
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}