package se.smhi.sharkadm.verifydata;

/*
 * This class represents the row and the tag for the missing MPROG.
 */
public class ColumnCode {
  
    private String colCode;
    public String getColCode() {
        return colCode;
    }
    public void setColCode(String colCode) {
        this.colCode = colCode;
    }


    private int rowNo;
    public int getRowNo() {
        return rowNo;
    }
    public void setRowNo(int rowNo) {
        this.rowNo = rowNo;
    }

}
