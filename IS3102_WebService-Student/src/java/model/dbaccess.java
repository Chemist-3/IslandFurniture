package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class dbaccess {

    // Global path
    private String jbdc_path = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";

    
    /**************************************
     *       StoreentityFacadeREST          *
    ***************************************/
    public int getQuantityWithStoreID(Connection conn, Long storeID, String SKU) throws SQLException {
        int quantity = 0;
        String stmt = "SELECT sum(l.QUANTITY) as sum FROM storeentity s, warehouseentity w, storagebinentity sb, storagebinentity_lineitementity sbli, lineitementity l, itementity i where s.WAREHOUSE_ID=w.ID and w.ID=sb.WAREHOUSE_ID and sb.ID=sbli.StorageBinEntity_ID and sbli.lineItems_ID=l.ID and l.ITEM_ID=i.ID and s.ID=? and i.SKU=?";
        PreparedStatement ps = conn.prepareStatement(stmt);
        ps.setLong(1, storeID);
        ps.setString(2, SKU);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            quantity = rs.getInt("sum");
        }
        return quantity;
    }

    public ResultSet getStoreInfomation(Connection conn, String salesRecordID) throws SQLException {
        String stmt = "SELECT se.* FROM storeentity se, salesrecordentity sre where sre.STORE_ID=se.ID and sre.ID=?;";
        PreparedStatement ps = conn.prepareStatement(stmt);
        ps.setLong(1, Long.parseLong(salesRecordID));
        return ps.executeQuery();
    }

    
    /**************************************
     *     CountryentityFacadeREST        *
    ***************************************/
    public int getQuantityWithCountryID(Connection conn, Long countryID, String SKU) throws SQLException {
        int quantity = 0;
        String stmt = "Select li.QUANTITY from country_ecommerce c, warehouseentity w, storagebinentity sb, storagebinentity_lineitementity sbli, lineitementity li, itementity i where li.ITEM_ID=i.ID and sbli.lineItems_ID=li.ID and sb.ID=sbli.StorageBinEntity_ID and w.id=sb.WAREHOUSE_ID and c.warehouseentity_id=w.id and sb.type<>'Outbound' and c.CountryEntity_ID=? and i.SKU=?";
        PreparedStatement ps = conn.prepareStatement(stmt);
        ps.setString(1, Long.toString(countryID));
        ps.setString(2, SKU);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            quantity = rs.getInt("QUANTITY");
        }
        return quantity;
    }

    
    /**************************************
     *       ECommerceFacadeREST          *
    ***************************************/
    public ResultSet getCountryStoreIDAndCurrency(Connection conn, Long countryID) throws SQLException {
        String stmt = "SELECT c.currency, s.ID as storeID FROM countryentity c,country_ecommerce ce, storeentity s "
                + "where ce.WarehouseEntity_ID=s.WAREHOUSE_ID and c.ID=ce.CountryEntity_ID and c.ID=?;";
        PreparedStatement ps = conn.prepareStatement(stmt);
        ps.setLong(1, countryID);
        ResultSet rs = ps.executeQuery();

        return rs;
    }

    public Long insertSalesrecordentity(Connection conn, Double amountPaid, String currentTime, String currency,
            Long receiptNumber, Long memberID, int storeID) throws SQLException {

        Long generatedKey = 0L;

        String stmt = "INSERT INTO salesrecordentity (`AMOUNTDUE`, `AMOUNTPAID`, `AMOUNTPAIDUSINGPOINTS`, "
                + "`CREATEDDATE`, `CURRENCY`, `LOYALTYPOINTSDEDUCTED`, `POSNAME`, `RECEIPTNO`, "
                + "`SERVEDBYSTAFF`, `MEMBER_ID`, `STORE_ID`) "
                + "VALUES ('" + amountPaid + "', '" + amountPaid + "', '0', '" + currentTime + "', '" + currency + "', "
                + "'0', 'ECommerce', '" + receiptNumber + "', 'ECommerce', '" + memberID + "', '" + storeID + "')";

        //RETURN_GENERATED_KEYS to get ID without running the database again
        PreparedStatement ps = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        while (rs.next()) {
            generatedKey = rs.getLong(1);
        }
        return generatedKey;
    }

    public Long insertLineitementity(Connection conn, int quantity, String itemID) throws SQLException {
        Long lineItemId = 0L;
        String stmt = "INSERT INTO lineitementity (`QUANTITY`, `ITEM_ID`) VALUES (?, ?);";
        PreparedStatement ps = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, quantity);
        ps.setLong(2, Long.parseLong(itemID));
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        while (rs.next()) {
            lineItemId = rs.getLong(1);
        }
        return lineItemId;
    }

    public int insertSalesrecordentity_lineitementity(Connection conn, String salesRecordID, Long lineItemId) throws SQLException {

        String stmt = "INSERT INTO salesrecordentity_lineitementity (`SalesRecordEntity_ID`, `itemsPurchased_ID`) VALUES (?, ?);";
        PreparedStatement ps = conn.prepareStatement(stmt);
        ps.setLong(1, Long.parseLong(salesRecordID));
        ps.setLong(2, lineItemId);
        return ps.executeUpdate();

    }

    public void updateQuantity(Connection conn, String itemID, int quantity, Long countryID) throws SQLException {

        //update quantity of items
        //retrieve lineitem ID and the quantity according to itemID and countryID
        String stmt = "select l.ID, l.QUANTITY, s.ID as storagebinID, i.VOLUME "
                + "from warehouseentity w, storagebinentity s, "
                + "storagebinentity_lineitementity sl, lineitementity l, itementity i, country_ecommerce c "
                + "where i.id=l.ITEM_ID and sl.lineItems_ID=l.ID and s.ID=sl.StorageBinEntity_ID "
                + "and w.ID=s.WAREHOUSE_ID and w.ID=c.WarehouseEntity_ID and c.CountryEntity_ID=? and l.ITEM_ID=?";
        PreparedStatement ps = conn.prepareStatement(stmt);
        ps.setLong(1, countryID);
        ps.setLong(2, Long.parseLong(itemID));
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Long lineItemID = rs.getLong(1);
            int qtyRemaining = rs.getInt(2);
            Long storageBinID = rs.getLong(3);
            int itemVolume = rs.getInt(4);
            if (quantity <= 0) {
                break;
            }
            if (qtyRemaining - quantity >= 0) {
                System.out.println("Quantity fufilled.");
                String updateStmt = "UPDATE lineitementity SET QUANTITY = QUANTITY-? WHERE ID = ?";
                ps = conn.prepareStatement(updateStmt);
                ps.setLong(1, quantity);
                ps.setLong(2, lineItemID);
                ps.executeUpdate();

                updateStmt = "UPDATE storagebinentity SET FREEVOLUME = FREEVOLUME+? WHERE ID = ?";
                ps = conn.prepareStatement(updateStmt);
                ps.setLong(1, quantity * itemVolume);
                ps.setLong(2, storageBinID);
                ps.executeUpdate();

                quantity = 0;
                break;
            } else {
                quantity -= qtyRemaining;
                String updateStmt = "UPDATE lineitementity SET QUANTITY = 0 WHERE ID = ?";
                ps = conn.prepareStatement(updateStmt);
                ps.setLong(1, lineItemID);
                ps.executeUpdate();
                
                System.out.println("Not enough quantity in bin, quantity needed: " + quantity);

                updateStmt = "UPDATE storagebinentity SET FREEVOLUME = VOLUME WHERE ID = ?";
                ps = conn.prepareStatement(updateStmt);
                ps.setLong(1, storageBinID);
                ps.executeUpdate();
            }
        }
    }

    /**************************************
     *       MemberentityFacadeREST       *
    ***************************************/
    public ResultSet getMemberEntity(Connection conn, String email) throws SQLException {
        String stmt = "SELECT * FROM memberentity m WHERE m.EMAIL=?";
        PreparedStatement ps = conn.prepareStatement(stmt);
        ps.setString(1, email);
        return ps.executeQuery();
    }

    public int updateMemberEntity(Connection conn, String name, String phone, String address, int securityQuestion,
            String securityAnswer, int age, int income, String email) throws SQLException {
        String updateStmt = "UPDATE memberentity SET NAME = ?, PHONE = ?, ADDRESS = ?, SECURITYQUESTION = ?, SECURITYANSWER = ?, AGE = ?, INCOME = ? where EMAIL = ?";
        PreparedStatement ps = conn.prepareStatement(updateStmt);
        ps.setString(1, name);
        ps.setString(2, phone);
        ps.setString(3, address);
        ps.setInt(4, securityQuestion);
        ps.setString(5, securityAnswer);
        ps.setInt(6, age);
        ps.setInt(7, income);
        ps.setString(8, email);
        

        return ps.executeUpdate();
    }
    
    public int updateMemberEntityPass(Connection conn, String name, String phone, String address, int securityQuestion,
            String securityAnswer, int age, int income, String passwordHash, String passwordSalt, String email) throws SQLException {
        String updateStmt = "UPDATE memberentity SET NAME = ?, PHONE = ?, ADDRESS = ?, SECURITYQUESTION = ?, SECURITYANSWER = ?, AGE = ?, INCOME = ?, PASSWORDHASH = ?, PASSWORDSALT = ? where EMAIL = ?";
        PreparedStatement ps = conn.prepareStatement(updateStmt);
        ps.setString(1, name);
        ps.setString(2, phone);
        ps.setString(3, address);
        ps.setInt(4, securityQuestion);
        ps.setString(5, securityAnswer);
        ps.setInt(6, age);
        ps.setInt(7, income);
        ps.setString(8, passwordHash);
        ps.setString(9, passwordSalt);
        ps.setString(10, email);


        return ps.executeUpdate();
    }
}
