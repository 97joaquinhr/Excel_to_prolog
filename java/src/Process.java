/*
 * Joaquin Herrera Ramos A01207504
 * November 2019
 * Programming Languages - Final project
 * */
import java.util.concurrent.RecursiveAction;

public class Process extends RecursiveAction {
    private static final int THRESHOLD = 5;
    private int start, end, n_rows_to_read;
    private String[][] data;
    private String[] relations;
    private String[] id_list;

    /**
     *
     * @param start integer that marks the start
     * @param end integer that marks the end
     * @param n_rows_to_read number of rows to be read
     * @param data 2D array containing the data from the Excel file
     * @param relations id+row_name
     * @param id_list ids to be matched with the data and relations
     */
    Process(int start, int end, int n_rows_to_read, String[][] data, String[] relations, String[] id_list) {
        this.start = start;
        this.end = end;
        this.n_rows_to_read = n_rows_to_read;
        this.data = data;
        this.relations = relations;
        this.id_list = id_list;
    }


    /**
     * Iterate through data to process the string and create the relation.
     */
    private void computeDirectly(){
        int i,j;
        String aux="";
        for (i = 0; i < n_rows_to_read; ++i){
            for(j=start;j<end;++j){
                if(data[i][j].matches("^[a-zA-Z0-9]+$")){
                    data[i][j] = data[i][j].toLowerCase();
                }else{
                    data[i][j] = "\"" + data[i][j] + "\"";
                }
                data[i][j]=relations[i]+"("+ id_list[j]+","+data[i][j]+").";
            }
        }
    }

    /**
     * Divide in subproblems.
     */
    @Override
    protected void compute(){
        if (end-start<THRESHOLD){
            computeDirectly();
        }else{
            int mid = (start+end)>>>1;
            invokeAll(new Process(start,mid, n_rows_to_read,data,relations, id_list),
                    new Process(mid,end, n_rows_to_read,data,relations, id_list));
        }
    }
}
