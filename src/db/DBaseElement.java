package db;

import java.sql.*;
import java.util.Objects;

public abstract class DBaseElement {
    public abstract void read();
    public abstract void write();
    public abstract boolean remove();
    public DBaseElement(int ID){
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public static void openConnection()throws SQLException{
        System.setProperty("derby.system.home", System.getProperty("user.dir"));
        dbConnection = DriverManager.getConnection("jdbc:derby:WorkTimeDB;create=true");
        DatabaseMetaData dmeta = dbConnection.getMetaData();
        ResultSet resultSet = dmeta.getTables(null,null,"SETTINGS",new String[]{"TABLE"});
        if(!resultSet.next()) initDB();
    }
    public static void closeConnection() throws SQLException{
        if(dbConnection!= null) dbConnection.close();
    }

    public static Connection getDbConnection() {
        return dbConnection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBaseElement dWork = (DBaseElement) o;
        return ID == dWork.ID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }
    private static void initDB(){
        try{

            try (Statement statement = dbConnection.createStatement()){
                statement.executeUpdate("CREATE TABLE SETTINGS(ID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1), NAME CHAR(50), VALUE CHAR(100))");
                statement.executeUpdate("INSERT INTO SETTINGS(NAME, VALUE) VALUES('version', '00.01')");
                statement.executeUpdate("CREATE TABLE CLIENTS(ID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1), NAME CHAR(100), FULL_NAME CHAR(200))");
                statement.executeUpdate("CREATE TABLE WORKS(ID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1), CLIENT_ID INT, DESCRIPTION CHAR(200), START_TIME TIME, END_TIME TIME, WORK_DATE DATE, AMOUNT DECIMAL(3,1))");


            }
        }catch (SQLException e){
            throw new RuntimeException("error init db", e);
        }


    }

    private static Connection dbConnection;
    private int ID;
}
