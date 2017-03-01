package com.elegance.feedback.Sql_offline_form;

/**
 * Created by jodiwaljay on 12/7/16.
 */
public class PairDataType {


    public PairDataType(String ColName, int dataType){
        ColumnName = ColName;
        dtType = dataType;
    }


    public String getColumnName() {
        return ColumnName;
    }

    public String getDtType() {

        if(dtType == INT){
            return intData;
        }

        if(dtType == STRING){
            return stringData;
        }

        return stringData;
    }

    private final String ColumnName;
    private final int INT = 1;
    private final int STRING = 2;
    private final int dtType;
    private final String intData = "INT";
    private final String stringData = "VARCHAR";





}
