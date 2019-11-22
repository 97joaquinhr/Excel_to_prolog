/*
* Joaquin Herrera Ramos A01207504
* 22 November 2019
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
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;

/**
 * I used https://www.callicoder.com/java-read-excel-file-apache-poi/
 * as reference for the Apache POI usage.
 */
public class Main {
    private static Workbook workbook;
    private static List<String> lines= new ArrayList<String>();
    private static DataFormatter dataFormatter = new DataFormatter();

    private static Sheet sheet;
    private static int n_rows;

    public static void main(String[] args) throws IOException, InvalidFormatException {
        String output_path;
        //crn with group information
        String id_crn="crn";
        Scanner scanner = new Scanner(System.in);
        System.out.println("Path of Excel file to read?");
        String excel_file_path = scanner.next();
        try {
            workbook = WorkbookFactory.create(new File(excel_file_path));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        sheet = workbook.getSheetAt(0);
        n_rows  = sheet.getLastRowNum();
        System.out.println("Path of PL file to write?");
        output_path = scanner.next();
        System.out.println("How many IDs do you want to process?");
        int ids = scanner.nextInt();
        int i,j;
        int[] id_rows = new int[ids];
        String[] id_names = new String[ids];
        int[] n_rows_to_read = new int[ids];
        int MAX = 1000;
        int [][] rows_to_read = new int[ids][MAX];
        for (i = 0;i<ids;i++){
            System.out.println("Row of ID #"+i);
            id_rows[i] = scanner.nextInt();
            System.out.println("Name of ID #"+i);
            id_names[i] = scanner.next();
            System.out.println("N of Rows to read of ID #"+i);
            n_rows_to_read[i] = scanner.nextInt();
            System.out.println("Rows to read of ID #"+i);
            for (j = 0;j<n_rows_to_read[i];j++){
                rows_to_read[i][j]= scanner.nextInt();
            }
        }
        String [][][] data = new String[ids][MAX][n_rows];
        String [][] relations = new String[ids][MAX];
        String [][] ids_list = new String[ids][n_rows];

        lines.add("/*Joaquin Herrera Ramos A01207504 \n November 2019 " +
                "\n Programming Languages - Final Project */");
        lines.add(":- encoding(utf8).");

        System.out.println("Reading data");
        for (i = 0;i<ids;i++) {
            create_relations(rows_to_read[i],relations[i],n_rows_to_read[i],id_names[i]);
            add_headers(n_rows_to_read[i], relations[i]);
            read_data(rows_to_read[i], n_rows_to_read[i], data[i], ids_list[i], id_rows[i]);
        }

        //process data
        System.out.println("Processing data");
        Process[] p = new Process[ids];
        for (i = 0;i<ids;i++) {
            p[i] = new Process(0,n_rows,n_rows_to_read[i],data[i],relations[i],ids_list[i]);
        }
        ForkJoinPool pool = new ForkJoinPool();
        for (i = 0;i<ids;i++) {
            pool.invoke(p[i]);
        }
        //Create ForkJoinPool
        System.out.println("Preparing data");
        pool.shutdown();
        for (i = 0;i<ids;i++) {
            prepare_data(n_rows_to_read[i], data[i]);
        }
        System.out.println("Writing data");
        write_data(output_path);

    }

    /**
     * Copies from data to lines, skipping repeatitions.
     * @param n_rows_to_read integer used to iterate
     * @param data 2D array of strings to be added into lines
     */
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

    /**
     * Prints the content of lines into the path provided.
     * @param path path of file for writing
     * @throws IOException if write goes wrong
     */
    private static void write_data(String path) throws IOException {

        Path file = Paths.get(path);
        Files.write(file, lines, StandardCharsets.UTF_8);
        workbook.close();
    }

    /**
     * Adds discontiguous header in case the prolog file is not ordered.
     * @param n_rows_to_read integer used to iterate
     * @param relations id+column
     */
    private static void add_headers(int n_rows_to_read, String[] relations) {
        int i=0,j;
        for(i=0; i<n_rows_to_read;++i){
            lines.add(":- discontiguous "+relations[i]+"/2.");
        }
    }

    /**
     * Reads data and ids. Ids must be lower case for Prolog.
     * @param rows_to_read array of integers of rows to be read
     * @param n_rows_to_read integer to iterate
     * @param data 2D array of strings to store the information from Excel
     * @param Ids array of strings that stores the ids
     * @param id_row the row that contains the ids
     */
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

    /**
     * Converts a string into Camel Case.
     * @param in string to be formatted
     * @return formatted string
     */
    private static String camelCase(String in){
        return WordUtils.capitalizeFully(in, ' ').replaceAll(" ", "");
    }

    /**
     * Joins the ID name with the row name. Example: crn_NombreMateria
     * @param rows_to_read array of integers of rows to be read
     * @param relations array of strings to store the relations (id+row_name)
     * @param l length of rows_to_read
     * @param id name of the id
     */
    private static void create_relations(int[] rows_to_read,String[] relations,int l,String id){
        int i;
        String aux="";
        for(i=0;i<l;++i){
            aux = dataFormatter.formatCellValue(sheet.getRow(0).getCell(rows_to_read[i]));
            relations[i]= id+"_"+camelCase(aux);
        }
    }


}