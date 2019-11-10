import java.util.concurrent.RecursiveAction;

public class Process extends RecursiveAction {
    private static final int THRESHOLD = 5;
    private int start, end, n_rows_to_read;
    private String[][] data;
    private String[] relations;
    private String[] crns;

    public Process(int start, int end, int n_rows_to_read, String[][] data, String[] relations,String[] crns) {
        this.start = start;
        this.end = end;
        this.n_rows_to_read = n_rows_to_read;
        this.data = data;
        this.relations = relations;
        this.crns = crns;
    }

    private void computeDirectly(){
        int i,j;
        for (i = 0; i < n_rows_to_read; ++i){
            for(j=start;j<end;++j){
                if (data[i][j].contains(" ")||data[i][j].contains("+")||data[i][j].contains("@")) {
                    data[i][j] = "\"" + data[i][j] + "\"";
                } else {
                    data[i][j] = data[i][j].toLowerCase();
                }
                data[i][j]=relations[i]+"("+crns[j]+","+data[i][j]+").";
            }
        }
    }

    @Override
    protected void compute(){
        if (end-start<THRESHOLD){
            computeDirectly();
        }else{
            int mid = (start+end)>>>1;
            invokeAll(new Process(start,mid, n_rows_to_read,data,relations,crns),
                    new Process(mid,end, n_rows_to_read,data,relations,crns));
        }
    }
}
