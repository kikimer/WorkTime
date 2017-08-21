package db;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

public class DWork extends DBaseElement{
    public DWork(int ID) {
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

            try (PreparedStatement ps = getDbConnection().prepareStatement("SELECT CLIENT_ID, DESCRIPTION, START_TIME, END_TIME, WORK_DATE, AMOUNT FROM WORKS WHERE ID=?")){
                ps.setInt(1, getID());
                ResultSet recordSet= ps.executeQuery();
                if (recordSet.next()){
                    client = new DClient(recordSet.getInt(1));
                    description = recordSet.getString(2);
                    if(description!=null)description = description.trim();
                    startTime = recordSet.getTime(3).toLocalTime();
                    endTime = recordSet.getTime(4).toLocalTime();
                    workDate = recordSet.getDate(5).toLocalDate();
                    amount = recordSet.getBigDecimal(6);

                }

            }
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    @Override
    public void write() {
        try {
            if(getID() == 0) // новый элемент
                try (PreparedStatement ps = getDbConnection().prepareStatement("INSERT INTO WORKS(CLIENT_ID, DESCRIPTION, START_TIME, END_TIME, WORK_DATE, AMOUNT) VALUES(?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS)){
                    ps.setInt(1, client==null?0:client.getID());
                    ps.setString(2, description);
                    ps.setTime(3, Time.valueOf(startTime));
                    ps.setTime(4, Time.valueOf(endTime));
                    ps.setDate(5, Date.valueOf(workDate));
                    ps.setBigDecimal(6, amount);
                    ps.executeUpdate();
                    ResultSet resultSet = ps.getGeneratedKeys();
                    if(!resultSet.next()) throw new RuntimeException("ID INCREMENT ERROR");
                    setID(resultSet.getInt(1));
                    updateProperty();
                }
            else
                try (PreparedStatement ps = getDbConnection().prepareStatement("UPDATE WORKS SET CLIENT_ID=?, DESCRIPTION=?, START_TIME=?, END_TIME=?, WORK_DATE=?, AMOUNT=? WHERE ID=?")){
                    ps.setInt(1, client.getID());
                    ps.setString(2, description);
                    ps.setTime(3, Time.valueOf(startTime));
                    ps.setTime(4, Time.valueOf(endTime));
                    ps.setDate(5, Date.valueOf(workDate));
                    ps.setBigDecimal(6, amount);
                    ps.setInt(7, getID());
                    ps.executeUpdate();
                    updateProperty();
                }
        }catch (SQLException e){
            e.printStackTrace();
        }
        isRead = true;
    }

    @Override
    public boolean remove() {
        if (getID() == 0) throw new RuntimeException("ID == 0");
        try{
            try(PreparedStatement ps = getDbConnection().prepareStatement("DELETE FROM WORKS WHERE ID=?")) {
                ps.setInt(1, getID());
                ps.executeUpdate();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        isRead =false;
        return true;
    }

    public static List<DWork> getAll(){
        List result = new LinkedList();;
        try {
            try (Statement statement = getDbConnection().createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT ID FROM WORKS ORDER BY WORK_DATE, START_TIME");
                while (resultSet.next()) result.add(new DWork(resultSet.getInt(1)));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return  result;
    }

    public DClient getClient() {
        if(!isRead) read();
        return client;
    }
    public void setClient(DClient client) {
        this.client = client;
    }
    public Property<DClient> clientProperty(){
        if(clientProperty==null)clientProperty = new SimpleObjectProperty<>(getClient());
        return clientProperty;
    }

    public String getDescription() {
        if(!isRead) read();
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Property<String> descriptionProperty(){
        if(descriptionProperty==null)descriptionProperty = new SimpleStringProperty(getDescription());
        return descriptionProperty;
    }

    public LocalTime getStartTime() {
        if(!isRead) read();
        return startTime;
    }
    public void setStartTime(LocalTime start) {
        this.startTime = start;
    }
    public Property<LocalTime> startTimeProperties(){
        if(startTimeProperty == null)startTimeProperty = new SimpleObjectProperty<>(getStartTime());
        return startTimeProperty;
    }

    public LocalTime getEndTime() {
        if(!isRead) read();
        return endTime;
    }
    public void setEndTime(LocalTime end) {
        this.endTime = end;
    }
    public Property<LocalTime> endTimeProperty(){
        if(endTimeProperty==null)endTimeProperty = new SimpleObjectProperty<>(getEndTime());
        return endTimeProperty;
    }

    public LocalDate getWorkDate() {
        if(!isRead) read();
        return workDate;
    }
    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }
    public Property<LocalDate> workDateProperty(){
        if(workDateProperty==null)workDateProperty=new SimpleObjectProperty<>(getWorkDate());
        return workDateProperty;
    }

    public BigDecimal getAmount() {
        if(!isRead) read();
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public Property<BigDecimal> amountProperty(){
        if(amountProperty==null)amountProperty = new SimpleObjectProperty<>(getAmount());
        return amountProperty;
    }

    private void updateProperty(){
        if(clientProperty!=null)clientProperty.setValue(client);
        if(descriptionProperty!=null)descriptionProperty.setValue(description);
        if(startTimeProperty!=null)startTimeProperty.setValue(startTime);
        if(endTimeProperty!=null)endTimeProperty.setValue(endTime);
        if(workDateProperty!=null)workDateProperty.setValue(workDate);
        if(amountProperty!=null)amountProperty.setValue(amount);
    }
    private boolean isRead = false;
    private DClient client;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate workDate;
    private BigDecimal amount;

    private Property<DClient> clientProperty;
    private Property<String> descriptionProperty;
    private Property<LocalTime> startTimeProperty;
    private Property<LocalTime> endTimeProperty;
    private Property<LocalDate> workDateProperty;
    private Property<BigDecimal> amountProperty;
}
