package db;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class DClient  extends DBaseElement{
    public DClient(int ID) {
        super(ID);
    }

    @Override
    public void setID(int ID){
        super.setID(ID);
        isRead = false;
    }
    @Override
    public void read() {
        if (getID() == 0) throw new RuntimeException("ID == 0");
        try {

            try (PreparedStatement ps = getDbConnection().prepareStatement("SELECT NAME, FULL_NAME FROM CLIENTS WHERE ID=?")){
                ps.setInt(1, getID());
                ResultSet recordSet= ps.executeQuery();
                if (recordSet.next()){
                    name = recordSet.getString(1);
                    fullName = recordSet.getString(2);

                    if(name!=null)name = name.trim();
                    if(fullName!=null)fullName = fullName.trim();
                }
                isRead = true;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    @Override
    public void write() {
        try {
            if(getID() == 0) // новый элемент
                try (PreparedStatement ps = getDbConnection().prepareStatement("INSERT INTO CLIENTS(NAME, FULL_NAME) VALUES(?,?)", PreparedStatement.RETURN_GENERATED_KEYS)){
                    ps.setString(1, name);
                    ps.setString(2, fullName);
                    ps.executeUpdate();
                    ResultSet resultSet = ps.getGeneratedKeys();
                    if(!resultSet.next()) throw new RuntimeException("ID INCREMENT ERROR");
                    setID(resultSet.getInt(1));
                    updateProperties();
                }
            else
                try (PreparedStatement ps = getDbConnection().prepareStatement("UPDATE CLIENTS SET NAME=?, FULL_NAME=? WHERE ID=?")){
                    ps.setString(1, name);
                    ps.setString(2, fullName);
                    ps.setInt(3, getID());
                    ps.executeUpdate();
                    updateProperties();
                }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean remove() {
        boolean canRemove = true;
        if (getID() == 0) throw new RuntimeException("ID == 0");
        try{
            try(PreparedStatement ps = getDbConnection().prepareStatement("SELECT COUNT(ID) FROM WORKS WHERE CLIENT_ID=?")) {
                ps.setInt(1, getID());
                ResultSet resultSet = ps.executeQuery();
                if(resultSet.next() && resultSet.getInt(1) > 0) canRemove = false;
            }
            if (canRemove)
                try(PreparedStatement ps = getDbConnection().prepareStatement("DELETE FROM CLIENTS WHERE ID=?")) {
                    ps.setInt(1, getID());
                    ps.executeUpdate();
                    isRead=false;
                }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return canRemove;
    }

    public String getName() {
        if(!isRead) read();
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Property<String> nameProperty(){
        if(nameProperty == null) nameProperty = new SimpleStringProperty(getName());
        return nameProperty;
    }

    public String getFullName() {
        if(!isRead) read();
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public Property<String> fullNameProperty(){
        if(nameProperty == null) fullNameProperty = new SimpleStringProperty(fullName);
        return fullNameProperty;
    }

    public static List<DClient> getAll(){
        List result = new LinkedList();;
        try {
            try (Statement statement = getDbConnection().createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT ID FROM CLIENTS ORDER BY NAME");
                while (resultSet.next()) result.add(new DClient(resultSet.getInt(1)));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return  result;
    }

    @Override
    public String toString() {
        if(getID()==0) return "<NO NAME>";
        return getName();
    }

    private void updateProperties(){
        if(nameProperty!=null)nameProperty.setValue(name);
        if(fullNameProperty!=null)fullNameProperty.setValue(fullName);
    }
    private boolean isRead = false;
    private String name;
    private String fullName;
    private Property<String> nameProperty;
    private Property<String> fullNameProperty;
}
