/*
* Joaquin Herrera Ramos A01207504
* November 2019
* Programming Languages - Final project
* */
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
    private static final String SAMPLE_XLSX_FILE_PATH = "C:\\Users\\joaqu\\Documents\\Codigo\\Excel_to_prolog\\info\\Proyecto.xlsx";
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
    private static int n_rows = sheet.getLastRowNum();
    //CRNs are the id for each group


    public static void main(String[] args) throws IOException, InvalidFormatException {
        //crn with group information
        String id_crn="crn";
        int crn_row = 5;
        int[] crn_rows_to_read = new int[]{7,19,20,21,24,29};
        int crn_n_rows_to_read =crn_rows_to_read.length;
        String[][] crn_data = new String[crn_n_rows_to_read][n_rows];
        String[] crn_relations = new String[crn_n_rows_to_read];
        String[] CRNs = new String[n_rows];

        //nomina with teacher's info
        String id_nomina="nomina";
        int nomina_row = 29;
        int[] nomina_rows_to_read = new int[]{31,32,33,34,36};
        int nomina_n_rows_to_read =nomina_rows_to_read.length;
        String[][] nomina_data = new String[nomina_n_rows_to_read][n_rows];
        String[] nomina_relations = new String[nomina_n_rows_to_read];
        String[] NOMINAs = new String[n_rows];

        lines.add("/*Joaquin Herrera Ramos A01207504 \n November 2019 " +
                "\n Programming Languages - Final Project */");
        lines.add(":- encoding(utf8).");

        create_relations(crn_rows_to_read,crn_relations,crn_n_rows_to_read,id_crn);
        add_headers(crn_n_rows_to_read, crn_relations);
        read_data(crn_rows_to_read, crn_n_rows_to_read, crn_data, CRNs, crn_row);

        create_relations(nomina_rows_to_read,nomina_relations,nomina_n_rows_to_read,id_nomina);
        add_headers(nomina_n_rows_to_read, nomina_relations);
        read_data(nomina_rows_to_read, nomina_n_rows_to_read, nomina_data, NOMINAs, nomina_row);

        //process data
        Process p_crns = new Process(0,n_rows,crn_n_rows_to_read,crn_data,crn_relations,CRNs);
        Process p_nominas = new Process(0,n_rows,nomina_n_rows_to_read,nomina_data,nomina_relations,NOMINAs);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(p_crns);
        pool.invoke(p_nominas);
        pool.shutdown();



        prepare_data(crn_n_rows_to_read, crn_data);
        prepare_data(nomina_n_rows_to_read, nomina_data);
        write_data();

    }
    private static void prepare_data(int n_rows_to_read, String[][] data){
        int i;
        int j;
        for (i = 0; i < n_rows_to_read; ++i){
            for(j=0;j<n_rows;++j) {
                if(!lines.contains(data[i][j])){
                    lines.add(data[i][j]);
                }
            }
        }
    }
    private static void write_data() throws IOException {

        Path file = Paths.get("C:\\Users\\joaqu\\Documents\\Codigo\\Excel_to_prolog\\info\\data.pl");
        Files.write(file, lines, StandardCharsets.UTF_8);
        workbook.close();
    }

    private static void add_headers(int n_rows_to_read, String[] relations) {
        int i=0,j;
        for(i=0; i<n_rows_to_read;++i){
            lines.add(":- discontiguous "+relations[i]+"/2.");
        }
    }

    private static void read_data(int[] rows_to_read, int n_rows_to_read, String[][] data, String[] Ids,int id_row) {
        int i=0,j;
        for(Row row: sheet){
            if(i>=1){
                Ids[i-1] = dataFormatter.formatCellValue(row.getCell(id_row)).toLowerCase();
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